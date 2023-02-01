package anticope.rejects.gui.servers;

import anticope.rejects.mixin.MultiplayerScreenAccessor;
import anticope.rejects.utils.server.IServerFinderDoneListener;
import anticope.rejects.utils.server.MServerInfo;
import anticope.rejects.utils.server.ServerPinger;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WIntEdit;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerInfo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Stack;

public class ServerFinderScreen extends WindowScreen implements IServerFinderDoneListener {
    public static ServerFinderScreen instance = null;
    private static int searchNumber = 0;
    private final MultiplayerScreen multiplayerScreen;
    private final WTextBox ipBox;
    private final WTextBox versionBox;
    private final WIntEdit maxThreadsBox;
    private final WButton searchButton;
    private final WLabel stateLabel;
    private final WLabel checkedLabel;
    private final WLabel workingLabel;
    private final WCheckbox scanPortsBox;
    private final Stack<String> ipsToPing = new Stack<>();
    private final Object serverFinderLock = new Object();
    private ServerFinderState state;
    private int maxThreads;
    private volatile int numActiveThreads;
    private volatile int checked;
    private volatile int working;
    private int targetChecked = 1792;
    private ArrayList<String> versionFilters = new ArrayList<>();
    private int playerCountFilter = 0;

    public ServerFinderScreen(GuiTheme theme, MultiplayerScreen multiplayerScreen, Screen parent) {
        super(theme, "Server Discovery");
        this.multiplayerScreen = multiplayerScreen;
        this.parent = parent;
        ipBox = theme.textBox("127.0.0.1");
        versionBox = theme.textBox("1.19; 1.18; 1.17; 1.16; 1.15; 1.14; 1.13; 1.12; 1.11; 1.10; 1.9; 1.8");
        maxThreadsBox = theme.intEdit(128, 1, 1024, 1, 1024);
        stateLabel = theme.label("");
        checkedLabel = theme.label("");
        searchButton = theme.button("Search");
        workingLabel = theme.label("");
        scanPortsBox = theme.checkbox(true);
        state = ServerFinderState.NOT_RUNNING;
        newSearch();
        instance = this;
    }

    public static int getSearchNumber() {
        return searchNumber;
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
        table.row();
        table.add(theme.label("Scan ports"));
        table.add(scanPortsBox);
        table.row();
        table.add(theme.label("Versions:"));
        table.add(versionBox).expandX();
        add(stateLabel);
        add(checkedLabel);
        add(workingLabel);
        WHorizontalList list = add(theme.horizontalList()).expandX().widget();
        list.add(searchButton).expandX();
        searchButton.action = this::searchOrCancel;
    }

    private void newSearch() {
        searchNumber = (searchNumber + 1) % 1000;
    }

    public void incrementTargetChecked(int amount) {
        synchronized (serverFinderLock) {
            if (state != ServerFinderState.CANCELLED)
                targetChecked += amount;
        }
    }

    public ServerFinderState getState() {
        return state;
    }

    private void searchOrCancel() {
        if (state.isRunning()) {
            state = ServerFinderState.CANCELLED;
            return;
        }

        state = ServerFinderState.RESOLVING;
        maxThreads = maxThreadsBox.get();
        ipsToPing.clear();
        targetChecked = 1792;
        numActiveThreads = 0;
        checked = 0;
        working = 0;

        newSearch();

        parseVersionFilters();

        findServers();
    }

    private void parseVersionFilters() {
        String filter = versionBox.get();
        String[] versions = filter.split(";");
        if (versionFilters == null) {
            versionFilters = new ArrayList<>();
        }
        versionFilters.clear();
        for (String version : versions) {
            String trimmed = version.trim();
            if (trimmed.length() > 0)
                versionFilters.add(version.trim());
        }
    }

    private void findServers() {
        try {
            InetAddress addr = InetAddress.getByName(ipBox.get().split(":")[0].trim());

            int[] ipParts = new int[4];
            for (int i = 0; i < 4; i++)
                ipParts[i] = addr.getAddress()[i] & 0xff;

            state = ServerFinderState.SEARCHING;
            int[] changes = {0, 1, -1, 2, -2, 3, -3};
            for (int change : changes)
                for (int i2 = 0; i2 <= 255; i2++) {
                    if (state == ServerFinderState.CANCELLED)
                        return;

                    int[] ipParts2 = ipParts.clone();
                    ipParts2[2] = ipParts[2] + change & 0xff;
                    ipParts2[3] = i2;
                    String ip = ipParts2[0] + "." + ipParts2[1] + "." + ipParts2[2] + "." + ipParts2[3];

                    ipsToPing.push(ip);
                }
            while (numActiveThreads < maxThreads && pingNewIP()) {
            }

        } catch (UnknownHostException e) {
            state = ServerFinderState.UNKNOWN_HOST;

        } catch (Exception e) {
            e.printStackTrace();
            state = ServerFinderState.ERROR;
        }
    }

    private boolean pingNewIP() {
        synchronized (serverFinderLock) {
            if (ipsToPing.size() > 0) {
                String ip = ipsToPing.pop();
                ServerPinger pinger = new ServerPinger(scanPortsBox.checked, searchNumber);
                pinger.addServerFinderDoneListener(this);
                pinger.ping(ip);
                numActiveThreads++;
                return true;
            }
        }
        return false;
    }

    @Override
    public void tick() {
        searchButton.set(state.isRunning() ? "Cancel" : "Search");
        if (state.isRunning()) {
            ipBox.setFocused(false);
            maxThreadsBox.set(maxThreads);
        }
        stateLabel.set(state.toString());
        checkedLabel.set("Checked: " + checked + " / " + targetChecked);
        workingLabel.set("Working: " + working);
        searchButton.visible = !ipBox.get().isEmpty();
    }

    private boolean isServerInList(String ip) {
        for (int i = 0; i < multiplayerScreen.getServerList().size(); i++)
            if (multiplayerScreen.getServerList().get(i).address.equals(ip))
                return true;

        return false;
    }

    @Override
    public void close() {
        state = ServerFinderState.CANCELLED;
        super.close();
    }

    private boolean filterPass(MServerInfo info) {
        if (info == null)
            return false;
        if (info.playerCount < playerCountFilter)
            return false;
        for (String version : versionFilters) {
            if (info.version != null && info.version.contains(version)) {
                return true;
            }
        }
        return versionFilters.isEmpty();
    }

    @Override
    public void onServerDone(ServerPinger pinger) {
        if (state == ServerFinderState.CANCELLED || pinger == null || pinger.getSearchNumber() != searchNumber)
            return;
        synchronized (serverFinderLock) {
            checked++;
            numActiveThreads--;
        }
        if (pinger.isWorking()) {
            if (!isServerInList(pinger.getServerIP()) && filterPass(pinger.getServerInfo())) {
                synchronized (serverFinderLock) {
                    working++;
                    multiplayerScreen.getServerList().add(new ServerInfo("Server discovery #" + working, pinger.getServerIP(), false), false);
                    multiplayerScreen.getServerList().saveFile();
                    ((MultiplayerScreenAccessor) multiplayerScreen).getServerListWidget().setSelected(null);
                    ((MultiplayerScreenAccessor) multiplayerScreen).getServerListWidget().setServers(multiplayerScreen.getServerList());
                }
            }
        }
        while (numActiveThreads < maxThreads && pingNewIP()) ;
        synchronized (serverFinderLock) {
            if (checked == targetChecked) {
                state = ServerFinderState.DONE;
            }
        }
    }

    @Override
    public void onServerFailed(ServerPinger pinger) {
        if (state == ServerFinderState.CANCELLED || pinger == null || pinger.getSearchNumber() != searchNumber)
            return;
        synchronized (serverFinderLock) {
            checked++;
            numActiveThreads--;
        }
        while (numActiveThreads < maxThreads && pingNewIP()) ;
        synchronized (serverFinderLock) {
            if (checked == targetChecked) {
                state = ServerFinderState.DONE;
            }
        }
    }

    public enum ServerFinderState {
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
