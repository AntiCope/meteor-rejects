package anticope.rejects.gui.servers;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.mixin.MultiplayerScreenAccessor;
import anticope.rejects.mixin.ServerListAccessor;
import anticope.rejects.utils.server.IPAddress;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.utils.misc.IGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.network.chat.Component;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class ServerManagerScreen extends WindowScreen {

    private static final PointerBuffer saveFileFilters;

    static {
        saveFileFilters = BufferUtils.createPointerBuffer(1);
        saveFileFilters.put(MemoryUtil.memASCII("*.txt"));
        saveFileFilters.rewind();
    }

    private final JoinMultiplayerScreen multiplayerScreen;

    public ServerManagerScreen(GuiTheme theme, JoinMultiplayerScreen multiplayerScreen) {
        super(theme, "Manage Servers");
        this.parent = multiplayerScreen;
        this.multiplayerScreen = multiplayerScreen;
    }

    public static Runnable tryHandle(ThrowingRunnable<?> tr, Consumer<Throwable> handler) {
        return Objects.requireNonNull(tr).addHandler(handler);
    }

    @Override
    public void initWidgets() {
        WHorizontalList l = add(theme.horizontalList()).expandX().widget();
        addButton(l, "Find Servers (new)", () -> new ServerFinderScreen(theme, multiplayerScreen, this));
        addButton(l, "Find Servers (legacy)", () -> new LegacyServerFinderScreen(theme, multiplayerScreen, this));
        addButton(l, "Clean Up", () -> new CleanUpScreen(theme, multiplayerScreen, this));
        l = add(theme.horizontalList()).expandX().widget();
        l.add(theme.button("Save IPs")).expandX().widget().action = tryHandle(() -> {
            String targetPath = TinyFileDialogs.tinyfd_saveFileDialog("Save IPs", null, saveFileFilters, null);
            if (targetPath == null) return;
            if (!targetPath.endsWith(".txt")) targetPath += ".txt";
            Path filePath = Path.of(targetPath);

            int newIPs = 0;

            Set<IPAddress> hashedIPs = new HashSet<>();
            if (Files.exists(filePath)) {
                try {
                    List<String> ips = Files.readAllLines(filePath);
                    for (String ip : ips) {
                        IPAddress parsedIP = IPAddress.fromText(ip);
                        if (parsedIP != null)
                            hashedIPs.add(parsedIP);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            ServerList servers = multiplayerScreen.getServers();
            for (int i = 0; i < servers.size(); i++) {
                ServerData info = servers.get(i);
                IPAddress addr = IPAddress.fromText(info.ip);
                if (addr != null && hashedIPs.add(addr))
                    newIPs++;
            }

            StringBuilder fileOutput = new StringBuilder();
            for (IPAddress ip : hashedIPs) {
                String stringIP = ip.toString();
                if (stringIP != null)
                    fileOutput.append(stringIP).append("\n");
            }

            try {
                Files.writeString(filePath, fileOutput.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            toast("Success!", newIPs == 1 ? "Saved %s new IP" : "Saved %s new IPs", newIPs);
        }, e -> {
            MeteorRejectsAddon.LOG.error("Could not save IPs");
            toast("Something went wrong", "The IPs could not be saved, look at the log for details");
        });
        l.add(theme.button("Load IPs")).expandX().widget().action = tryHandle(() -> {
            String targetPath = TinyFileDialogs.tinyfd_openFileDialog("Load IPs", null, saveFileFilters, "", false);
            if (targetPath == null) return;
            Path filePath = Path.of(targetPath);
            if (!Files.exists(filePath)) return;

            List<ServerData> servers = ((ServerListAccessor) multiplayerScreen.getServers()).getServerList();
            Set<String> presentAddresses = new HashSet<>();
            int newIPs = 0;
            for (ServerData server : servers) presentAddresses.add(server.ip);
            for (String addr : Minecraft.getInstance().keyboardHandler.getClipboard().split("[\r\n]+")) {
                if (presentAddresses.add(addr = addr.split(" ")[0])) {
                    servers.add(new ServerData("Server discovery #" + presentAddresses.size(), addr, ServerData.Type.OTHER));
                    newIPs++;
                }
            }
            multiplayerScreen.getServers().save();
            ((MultiplayerScreenAccessor) multiplayerScreen).getServerListWidget().setSelected(null);
            ((MultiplayerScreenAccessor) multiplayerScreen).getServerListWidget().updateOnlineServers(multiplayerScreen.getServers());
            toast("Success!", newIPs == 1 ? "Loaded %s new IP" : "Loaded %s new IPs", newIPs);
        }, e -> {
            MeteorRejectsAddon.LOG.error("Could not load IPs");
            toast("Something went wrong", "The IPs could not be loaded, look at the log for details");
        });
    }

    private void toast(String titleKey, String descriptionKey, Object... params) {
        SystemToast.add(minecraft.getToastManager(), SystemToast.SystemToastId.WORLD_BACKUP, Component.literal(titleKey), Component.translatable(descriptionKey, params));
    }

    private void addButton(WContainer c, String text, IGetter<Screen> action) {
        WButton button = c.add(theme.button(text)).expandX().widget();
        button.action = () -> minecraft.setScreen(action.get());
    }

    public interface ThrowingRunnable<TEx extends Throwable> {
        void run() throws TEx;

        default Runnable addHandler(Consumer<Throwable> handler) {
            Objects.requireNonNull(handler);
            return () -> {
                try {
                    this.run();
                } catch (Throwable var3) {
                    handler.accept(var3);
                }
            };
        }
    }

}
