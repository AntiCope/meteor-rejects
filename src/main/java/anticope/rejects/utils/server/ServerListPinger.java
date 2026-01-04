package anticope.rejects.utils.server;

import com.google.common.collect.Lists;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import net.minecraft.client.network.Address;
import net.minecraft.client.network.AllowedAddressResolver;
import net.minecraft.client.network.LegacyServerPinger;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.NetworkingBackend;
import net.minecraft.network.listener.ClientQueryPacketListener;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket;
import net.minecraft.server.ServerMetadata;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.MultiValueDebugSampleLogImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;

public class ServerListPinger {
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
        ServerAddress serverAddress = ServerAddress.parse(entry.address);
        Optional<InetSocketAddress> address = AllowedAddressResolver.DEFAULT.resolve(serverAddress).map(Address::getInetSocketAddress);
        if (address.isEmpty()) {
            return;
        }
        final ClientConnection clientConnection = ClientConnection.connect(address.get(), NetworkingBackend.remote(false), (MultiValueDebugSampleLogImpl) null);

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
                    return;
                }
                this.received = true;
                ServerMetadata serverMetadata = packet.metadata();
                if (serverMetadata.description() != null) {
                    entry.label = serverMetadata.description().getString();
                } else {
                    entry.label = "";
                }

                entry.version = serverMetadata.version().map(ServerMetadata.Version::gameVersion).orElse("multiplayer.status.old");
                serverMetadata.players().ifPresentOrElse(players -> {
                    entry.playerCountLabel = ServerListPinger.getPlayerCountLabel(players.online(), players.max());
                    entry.playerCount = players.online();
                    entry.playercountMax = players.max();
                }, () -> {
                    entry.playerCountLabel = "multiplayer.status.unknown";
                });

                this.startTime = Util.getMeasuringTimeMs();
                clientConnection.send(new QueryPingC2SPacket(this.startTime));
                this.sentQuery = true;
                notifyDisconnectListeners();
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

            @Override
            public void onDisconnected(DisconnectionInfo info) {

            }

            public boolean isConnectionOpen() {
                return clientConnection.isOpen();
            }
        };

        try {
            clientConnection.connect(serverAddress.getAddress(), serverAddress.getPort(), clientQueryPacketListener);
            clientConnection.send(QueryRequestC2SPacket.INSTANCE);
        } catch (Throwable var8) {
            LOGGER.error("Failed to ping server {}", serverAddress, var8);
        }

    }

    private void ping(final MServerInfo serverInfo) {
        final ServerAddress serverAddress = ServerAddress.parse(serverInfo.address);
        NetworkingBackend backend = NetworkingBackend.remote(false);
        new Bootstrap().group(backend.getEventLoopGroup()).handler(new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                try {
                    ch.config().setOption(ChannelOption.TCP_NODELAY, true);
                } catch (ChannelException ignored) {
                }
                ch.pipeline().addLast(new LegacyServerPinger(serverAddress, ((protocolVersion, version, label, currentPlayers, maxPlayers) -> {
                    serverInfo.version = version;
                    serverInfo.label = label;
                    serverInfo.playerCountLabel = ServerListPinger.getPlayerCountLabel(currentPlayers, maxPlayers);
                })));
            }
        });
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