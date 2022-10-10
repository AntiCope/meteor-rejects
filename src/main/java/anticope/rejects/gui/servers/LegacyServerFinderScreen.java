package anticope.rejects.gui.servers;

import anticope.rejects.mixin.MultiplayerScreenAccessor;
import anticope.rejects.utils.server.LegacyServerPinger;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WIntEdit;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerInfo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class LegacyServerFinderScreen extends WindowScreen {
    private final MultiplayerScreen multiplayerScreen;
    private final WTextBox ipBox;
    private final WIntEdit maxThreadsBox;
    private final WButton searchButton;
    private final WLabel stateLabel;
    private final WLabel checkedLabel;
    private final WLabel workingLabel;
    private ServerFinderState state;
    private int maxThreads;
    private int checked;
    private int working;

    public LegacyServerFinderScreen(GuiTheme theme, MultiplayerScreen multiplayerScreen, Screen parent) {
        super(theme, "Legacy Server Discovery");
        this.multiplayerScreen = multiplayerScreen;
        this.parent = parent;
        ipBox = theme.textBox("127.0.0.1");
        maxThreadsBox = theme.intEdit(128, 1, 256, 1, 256);
        stateLabel = theme.label("");
        checkedLabel = theme.label("");
        workingLabel = theme.label("");
        searchButton = theme.button("Search");
        state = ServerFinderState.NOT_RUNNING;
    }

    @Override
    public void initWidgets() {
        add(theme.label("This will search for servers with similar IPs"));
        add(theme.label("to the IP you type into the field below."));
        add(theme.label("The servers it finds will be added to your server list."));
        WTable table = add(new WTable()).expandX().widget();
        table.add(theme.label("Server address:"));
        table.add(ipBox).expandX();
        table.row();
        table.add(theme.label("Max. Threads:"));
        table.add(maxThreadsBox);
        add(stateLabel);
        add(checkedLabel);
        add(workingLabel);
        add(searchButton).expandX();
        searchButton.action = this::searchOrCancel;
    }

    private void searchOrCancel() {
        if (state.isRunning()) {
            state = ServerFinderState.CANCELLED;
            return;
        }

        state = ServerFinderState.RESOLVING;
        maxThreads = maxThreadsBox.get();
        checked = 0;
        working = 0;

        new Thread(this::findServers, "Server Discovery").start();
    }

    private void findServers() {
        try {
            InetAddress addr =
                    InetAddress.getByName(ipBox.get().split(":")[0].trim());

            int[] ipParts = new int[4];
            for (int i = 0; i < 4; i++)
                ipParts[i] = addr.getAddress()[i] & 0xff;

            state = ServerFinderState.SEARCHING;
            ArrayList<LegacyServerPinger> pingers = new ArrayList<>();
            int[] changes = {0, 1, -1, 2, -2, 3, -3};
            for (int change : changes)
                for (int i2 = 0; i2 <= 255; i2++) {
                    if (state == ServerFinderState.CANCELLED)
                        return;

                    int[] ipParts2 = ipParts.clone();
                    ipParts2[2] = ipParts[2] + change & 0xff;
                    ipParts2[3] = i2;
                    String ip = ipParts2[0] + "." + ipParts2[1] + "."
                            + ipParts2[2] + "." + ipParts2[3];

                    LegacyServerPinger pinger = new LegacyServerPinger();
                    pinger.ping(ip);
                    pingers.add(pinger);
                    while (pingers.size() >= maxThreads) {
                        if (state == ServerFinderState.CANCELLED)
                            return;

                        updatePingers(pingers);
                    }
                }
            while (pingers.size() > 0) {
                if (state == ServerFinderState.CANCELLED)
                    return;

                updatePingers(pingers);
            }
            state = ServerFinderState.DONE;

        } catch (UnknownHostException e) {
            state = ServerFinderState.UNKNOWN_HOST;

        } catch (Exception e) {
            e.printStackTrace();
            state = ServerFinderState.ERROR;
        }
    }

    @Override
    public void tick() {
        searchButton.set(state.isRunning() ? "Cancel" : "Search");
        if (state.isRunning()) {
            ipBox.setFocused(false);
            maxThreadsBox.set(maxThreads);
        }
        stateLabel.set(state.toString());
        checkedLabel.set("Checked: " + checked + " / 1792");
        workingLabel.set("Working: " + working);
        searchButton.visible = !ipBox.get().isEmpty();
    }

    private boolean isServerInList(String ip) {
        for (int i = 0; i < multiplayerScreen.getServerList().size(); i++)
            if (multiplayerScreen.getServerList().get(i).address.equals(ip))
                return true;

        return false;
    }

    private void updatePingers(ArrayList<LegacyServerPinger> pingers) {
        for (int i = 0; i < pingers.size(); i++)
            if (!pingers.get(i).isStillPinging()) {
                checked++;
                if (pingers.get(i).isWorking()) {
                    working++;

                    if (!isServerInList(pingers.get(i).getServerIP())) {
                        multiplayerScreen.getServerList()
                                .add(new ServerInfo("Server discovery " + working,
                                        pingers.get(i).getServerIP(), false), false);
                        multiplayerScreen.getServerList().saveFile();
                        ((MultiplayerScreenAccessor) multiplayerScreen).getServerListWidget()
                                .setSelected(null);
                        ((MultiplayerScreenAccessor) multiplayerScreen).getServerListWidget()
                                .setServers(multiplayerScreen.getServerList());
                    }
                }
                pingers.remove(i);
            }
    }

    @Override
    public void close() {
        state = ServerFinderState.CANCELLED;
        super.close();
    }

    enum ServerFinderState {
        NOT_RUNNING(""),
        SEARCHING("Searching..."),
        RESOLVING("Resolving..."),
        UNKNOWN_HOST("Unknown Host!"),
        CANCELLED("Cancelled!"),
        DONE("Done!"),
        ERROR("An error occurred!");

        private final String name;

        ServerFinderState(String name) {
            this.name = name;
        }

        public boolean isRunning() {
            return this == SEARCHING || this == RESOLVING;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
