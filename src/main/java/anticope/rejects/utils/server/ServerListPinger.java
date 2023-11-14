package anticope.rejects.utils.server;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.listener.ClientQueryPacketListener;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket;
import net.minecraft.server.ServerMetadata;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.PerformanceLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ServerListPinger {
    private static final Splitter ZERO_SPLITTER = Splitter.on('\u0000').limit(6);
    private static final Logger LOGGER = LogManager.getLogger();
    private final List<ClientConnection> clientConnections = Collections.synchronizedList(Lists.newArrayList());
    private final ArrayList<IServerFinderDisconnectListener> disconnectListeners = new ArrayList<>();
    private boolean notifiedDisconnectListeners = false;
    private boolean failedToConnect = true;

    private static String getPlayerCountLabel(int i, int j) {
        return i + "/" + j;
    }

    public void addServerFinderDisconnectListener(IServerFinderDisconnectListener listener) {
        disconnectListeners.add(listener);
    }

    private void notifyDisconnectListeners() {
        synchronized (this) {
            if (!notifiedDisconnectListeners) {
                notifiedDisconnectListeners = true;
                for (IServerFinderDisconnectListener l : disconnectListeners) {
                    if (l != null) {
                        if (failedToConnect) {
                            l.onServerFailed();
                        } else {
                            l.onServerDisconnect();
                        }
                    }
                }
            }
        }
    }


    public void add(final MServerInfo entry, final Runnable runnable) throws UnknownHostException {
        Timer timeoutTimer = new Timer();
        ServerAddress serverAddress = ServerAddress.parse(entry.address);
        timeoutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                notifyDisconnectListeners();
            }
        }, 20000);
        final ClientConnection clientConnection = ClientConnection.connect(new InetSocketAddress(InetAddress.getByName(serverAddress.getAddress()), serverAddress.getPort()), false, (PerformanceLog) null);
        failedToConnect = false;
        this.clientConnections.add(clientConnection);
        entry.label = "multiplayer.status.pinging";
        entry.ping = -1L;
        entry.playerListSummary = null;
        ClientQueryPacketListener clientQueryPacketListener = new ClientQueryPacketListener() {
            private boolean sentQuery;
            private boolean received;
            private long startTime;

            public void onResponse(QueryResponseS2CPacket packet) {
                if (this.received) {
                    clientConnection.disconnect(Text.translatable("multiplayer.status.unrequested"));
                } else {
                    this.received = true;
                    ServerMetadata serverMetadata = packet.metadata();
                    entry.label = serverMetadata.description().getString();
                    serverMetadata.version().ifPresentOrElse((version) -> {
                        entry.version = version.gameVersion();
                        entry.protocolVersion = version.protocolVersion();
                    }, () -> {
                        entry.version = "multiplayer.status.old";
                        entry.protocolVersion = 0;
                    });
                    serverMetadata.players().ifPresentOrElse((players) -> {
                        entry.playerCountLabel = getPlayerCountLabel(players.online(), players.max());
                        entry.playerCount = players.online();
                        if (!players.sample().isEmpty()) {
                            List<Text> list = new ArrayList(players.sample().size());
                            Iterator var3 = players.sample().iterator();

                            while (var3.hasNext()) {
                                GameProfile gameProfile = (GameProfile) var3.next();
                                list.add(Text.literal(gameProfile.getName()));
                            }

                            if (players.sample().size() < players.online()) {
                                list.add(Text.translatable("multiplayer.status.and_more", players.online() - players.sample().size()));
                            }

                            entry.playerListSummary = list;
                        } else {
                            entry.playerListSummary = List.of();
                        }

                    }, () -> entry.playerCountLabel = "multiplayer.status.unknown");
                    serverMetadata.favicon().ifPresent((favicon) -> {
                        if (!Arrays.equals(favicon.iconBytes(), entry.getIcon())) {
                            entry.setIcon(favicon.iconBytes());
                            runnable.run();
                        }

                    });
                    this.startTime = Util.getMeasuringTimeMs();
                    clientConnection.send(new QueryPingC2SPacket(this.startTime));
                    this.sentQuery = true;
                    notifyDisconnectListeners();
                }
            }

            public void onPingResult(PingResultS2CPacket packet) {
                long l = this.startTime;
                long m = Util.getMeasuringTimeMs();
                entry.ping = m - l;
                clientConnection.disconnect(Text.translatable("multiplayer.status.finished"));
            }

            public void onDisconnected(Text reason) {
                if (!this.sentQuery) {
                    ServerListPinger.LOGGER.error("Can't ping {}: {}", entry.address, reason.getString());
                    entry.label = "multiplayer.status.cannot_connect";
                    entry.playerCountLabel = "";
                    entry.playerCount = 0;
                    entry.playercountMax = 0;
                    ServerListPinger.this.ping(entry);
                }
                notifyDisconnectListeners();
            }

            public boolean isConnectionOpen() {
                return clientConnection.isOpen();
            }
        };

        clientConnection.setPacketListener(clientQueryPacketListener);

        try {
            clientConnection.connect(serverAddress.getAddress(), serverAddress.getPort(), clientQueryPacketListener);
            clientConnection.send(new QueryRequestC2SPacket());
        } catch (Throwable var8) {
            LOGGER.error("Failed to ping server {}", serverAddress, var8);
        }

    }

    private void ping(final MServerInfo serverInfo) {
        final ServerAddress serverAddress = ServerAddress.parse(serverInfo.address);
        (new Bootstrap()).group(ClientConnection.CLIENT_IO_GROUP.get()).handler(new ChannelInitializer<>() {
            protected void initChannel(Channel channel) {
                try {
                    channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                } catch (ChannelException ignored) {
                }

                channel.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                    public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
                        super.channelActive(channelHandlerContext);
                        ByteBuf byteBuf = Unpooled.buffer();

                        try {
                            byteBuf.writeByte(254);
                            byteBuf.writeByte(1);
                            byteBuf.writeByte(250);
                            char[] cs = "MC|PingHost".toCharArray();
                            byteBuf.writeShort(cs.length);
                            char[] var4 = cs;
                            int var5 = cs.length;

                            int var6;
                            char d;
                            for (var6 = 0; var6 < var5; ++var6) {
                                d = var4[var6];
                                byteBuf.writeChar(d);
                            }

                            byteBuf.writeShort(7 + 2 * serverAddress.getAddress().length());
                            byteBuf.writeByte(127);
                            cs = serverAddress.getAddress().toCharArray();
                            byteBuf.writeShort(cs.length);
                            var4 = cs;
                            var5 = cs.length;

                            for (var6 = 0; var6 < var5; ++var6) {
                                d = var4[var6];
                                byteBuf.writeChar(d);
                            }

                            byteBuf.writeInt(serverAddress.getPort());
                            channelHandlerContext.channel().writeAndFlush(byteBuf).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                        } finally {
                            byteBuf.release();
                        }
                    }

                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
                        short s = byteBuf.readUnsignedByte();
                        if (s == 255) {
                            String string = new String(byteBuf.readBytes(byteBuf.readShort() * 2).array(), StandardCharsets.UTF_16BE);
                            String[] strings = Iterables.toArray(ServerListPinger.ZERO_SPLITTER.split(string), String.class);
                            if ("§1".equals(strings[0])) {
                                String string2 = strings[2];
                                String string3 = strings[3];
                                int j = MathHelper.parseInt(strings[4], -1);
                                int k = MathHelper.parseInt(strings[5], -1);
                                serverInfo.protocolVersion = -1;
                                serverInfo.version = string2;
                                serverInfo.label = string3;
                                serverInfo.playerCountLabel = ServerListPinger.getPlayerCountLabel(j, k);
                            }
                        }

                        channelHandlerContext.close();
                    }

                    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) {
                        channelHandlerContext.close();
                    }
                });
            }
        }).channel(NioSocketChannel.class).connect(serverAddress.getAddress(), serverAddress.getPort());
    }

    public void tick() {
        synchronized (this.clientConnections) {
            Iterator<ClientConnection> iterator = this.clientConnections.iterator();

            while (iterator.hasNext()) {
                ClientConnection clientConnection = iterator.next();
                if (clientConnection.isOpen()) {
                    clientConnection.tick();
                } else {
                    iterator.remove();
                    clientConnection.handleDisconnection();
                }
            }
        }
    }

    public void cancel() {
        synchronized (this.clientConnections) {
            Iterator<ClientConnection> iterator = this.clientConnections.iterator();

            while (iterator.hasNext()) {
                ClientConnection clientConnection = iterator.next();
                if (clientConnection.isOpen()) {
                    iterator.remove();
                    clientConnection.disconnect(Text.translatable("multiplayer.status.cancelled"));
                }
            }
        }
    }
}
