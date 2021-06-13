package cloudburst.rejects.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.client.network.ServerInfo;
import net.minecraft.command.CommandSource;
import net.minecraft.text.BaseText;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.ClickEvent.Action;
import net.minecraft.util.Formatting;

import cloudburst.rejects.utils.portscanner.PScanRunner;
import cloudburst.rejects.utils.portscanner.PortScannerManager;
import minegame159.meteorclient.systems.commands.Command;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.mojang.brigadier.arguments.IntegerArgumentType;

/*
    Ported from Cornos
    https://github.com/cornos/Cornos/blob/master/src/main/java/me/zeroX150/cornos/features/command/impl/Scan.java
*/
public class ServerCommand extends Command {

    private final static SimpleCommandExceptionType ADDRESS_ERROR = new SimpleCommandExceptionType(new LiteralText("Couldn't obtain server address"));
    private final static SimpleCommandExceptionType INVALID_RANGE = new SimpleCommandExceptionType(new LiteralText("Invalid range"));

    private final static HashMap<Integer, String> ports = new HashMap();

    public ServerCommand() {
        super("server", "Prints server information");

        ports.put(20, "FTP");
        ports.put(22, "SSH");
        ports.put(80, "HTTP");
        ports.put(443, "HTTPS");
        ports.put(25565, "Java Server");
        ports.put(25575, "Java Server RCON");
        ports.put(19132, "Bedrock Server");
        ports.put(19133, "Bedrock Server IPv6");
        ports.put(8123, "DynMap");
        ports.put(25566, "Minequery");
        ports.put(3306, "MySQL");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("ports").executes(ctx -> {
            scanKnownPorts(getAddress());
            return SINGLE_SUCCESS;
        }));
        builder.then(literal("ports").then(literal("known").executes(ctx -> {
            scanKnownPorts(getAddress());
            return SINGLE_SUCCESS;
        })));
        builder.then(literal("ports").then(argument("from", IntegerArgumentType.integer(0)).then(argument("to", IntegerArgumentType.integer(1)).executes(ctx -> {
            scanRange(getAddress(), IntegerArgumentType.getInteger(ctx, "from"),
                IntegerArgumentType.getInteger(ctx, "to"));
            return SINGLE_SUCCESS;
        }))));
    }

    private InetAddress getAddress() throws CommandSyntaxException {
        if (mc.isIntegratedServerRunning()) {
            try {
                return InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                throw ADDRESS_ERROR.create();
            }
        }
        else {
            ServerInfo server = mc.getCurrentServerEntry();
            if (server == null) throw ADDRESS_ERROR.create();
            try {
                return InetAddress.getByName(server.address);
            } catch (UnknownHostException e) {
                throw ADDRESS_ERROR.create();
            }
        }
    }

    private void scanPorts(InetAddress address, Collection<Integer> port_list){
        info("Started scanning %d ports", port_list.size());
        PScanRunner pScanRunner = new PScanRunner(address, 5, 3, 200, port_list, scanResults -> {
            int open_ports = 0;
            info("Open ports:");
            for (PortScannerManager.ScanResult result : scanResults) {
                if (result.isOpen()) {
                    info(formatPort(result.getPort(), address));
                    open_ports++;
                }
            }
            info("Open count: %d/%d", open_ports, scanResults.size());
        });
        PortScannerManager.scans.add(pScanRunner);
    }

    private void scanKnownPorts(InetAddress address) {
        scanPorts(address, ports.keySet());
    }

    private void scanRange(InetAddress address, int min, int max) throws CommandSyntaxException {
        if (max<min) throw INVALID_RANGE.create();
        List<Integer> port_list = new LinkedList<>();
        for (int i = min; i <= max; i++) port_list.add(i);
        scanPorts(address, port_list);
    }

    private BaseText formatPort(int port, InetAddress address) {
        BaseText text = new LiteralText(String.format("- %s%d%s ", Formatting.GREEN, port, Formatting.GRAY));
        if (ports.containsKey(port)) {
            text.append(ports.get(port));
            if (ports.get(port).startsWith("HTTP")) {
                text.setStyle(text.getStyle()
                    .withClickEvent(new ClickEvent(
                        Action.OPEN_URL,
                        String.format("%s://%s:%d", ports.get(port).toLowerCase(), address.getHostAddress(), port)
                    ))
                    .withHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new LiteralText("Open in browser")
                    ))
                );
            }
            else if (ports.get(port) == "DynMap") {
                text.setStyle(text.getStyle()
                    .withClickEvent(new ClickEvent(
                        ClickEvent.Action.OPEN_URL,
                        String.format("http://%s:%d", address.getHostAddress(), port)
                    ))
                    .withHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new LiteralText("Open in browser")
                    ))
                );
            }
        }

        return text;
    }
}
