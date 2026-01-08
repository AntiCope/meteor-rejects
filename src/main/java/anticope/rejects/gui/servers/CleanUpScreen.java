package anticope.rejects.gui.servers;

import anticope.rejects.mixin.MultiplayerScreenAccessor;
import anticope.rejects.mixin.ServerListAccessor;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CleanUpScreen extends WindowScreen {
    private final JoinMultiplayerScreen multiplayerScreen;
    private final WCheckbox removeAll;
    private final WCheckbox removeFailed;
    private final WCheckbox removeOutdated;
    private final WCheckbox removeUnknown;
    private final WCheckbox removeGriefMe;
    private final WCheckbox removeDuplicates;
    private final WCheckbox rename;

    public CleanUpScreen(GuiTheme theme, JoinMultiplayerScreen multiplayerScreen, Screen parent) {
        super(theme, "Clean Up");
        this.multiplayerScreen = multiplayerScreen;
        this.parent = parent;
        removeUnknown = theme.checkbox(true);
        removeOutdated = theme.checkbox(false);
        removeFailed = theme.checkbox(true);
        removeGriefMe = theme.checkbox(false);
        removeAll = theme.checkbox(false);
        removeDuplicates = theme.checkbox(true);
        rename = theme.checkbox(true);
    }

    @Override
    public void initWidgets() {
        WTable table = add(new WTable()).widget();
        table.add(theme.label("Remove:"));
        table.row();
        table.add(theme.label("Unknown Hosts:")).widget().tooltip = "";
        table.add(removeUnknown).widget();
        table.row();
        table.add(theme.label("Outdated Servers:"));
        table.add(removeOutdated).widget();
        table.row();
        table.add(theme.label("Failed Ping:"));
        table.add(removeFailed).widget();
        table.row();
        table.add(theme.label("\"Server discovery\" Servers:"));
        table.add(removeGriefMe).widget();
        table.row();
        table.add(theme.label("Everything:")).widget().color = new Color(255, 0, 0);
        table.add(removeAll).widget();
        table.row();
        table.add(theme.label("Duplicates:"));
        table.add(removeDuplicates).widget();
        table.row();
        table.add(theme.label("Rename all Servers:"));
        table.add(rename).widget();
        table.row();
        table.add(theme.button("Execute!")).expandX().widget().action = this::cleanUp;
    }

    private void cleanUp() {
        Set<String> knownIPs = new HashSet<>();
        List<ServerData> servers = ((ServerListAccessor) multiplayerScreen.getServers()).getServerList();
        for (ServerData server : servers.toArray(ServerData[]::new)) {
            if (removeAll.checked || shouldRemove(server, knownIPs))
                servers.remove(server);
        }

        if (rename.checked)
            for (int i = 0; i < servers.size(); i++) {
                ServerData server = servers.get(i);
                server.name = "Server discovery " + (i + 1);
            }

        saveServerList();
        minecraft.setScreen(parent);
    }

    private boolean shouldRemove(ServerData server, Set<String> knownIPs) {
        return server != null && (removeUnknown.checked && isUnknownHost(server)
                || removeOutdated.checked && !isSameProtocol(server)
                || removeFailed.checked && isFailedPing(server)
                || removeGriefMe.checked && isGriefMeServer(server)
                || removeDuplicates.checked && !knownIPs.add(server.ip));
    }

    private boolean isUnknownHost(ServerData server) {
        if (server.motd == null || server.motd.getString() == null) return false;

        return server.motd.getString().equals("\u00a74Can't resolve hostname");
    }

    private boolean isSameProtocol(ServerData server) {
        return server.protocol == net.minecraft.SharedConstants.getCurrentVersion().protocolVersion();
    }

    private boolean isFailedPing(ServerData server) {
        return server.ping != -2L && server.ping < 0L;
    }

    private boolean isGriefMeServer(ServerData server) {
        return server.name != null && server.name.startsWith("Server discovery ");
    }

    private void saveServerList() {
        multiplayerScreen.getServers().save();

        ServerSelectionList serverListSelector = ((MultiplayerScreenAccessor) multiplayerScreen).getServerListWidget();

        serverListSelector.setSelected(null);
        serverListSelector.updateOnlineServers(multiplayerScreen.getServers());
    }
}
