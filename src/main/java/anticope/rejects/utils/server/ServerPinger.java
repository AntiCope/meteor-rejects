package anticope.rejects.utils.server;

import anticope.rejects.gui.servers.ServerFinderScreen;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerPinger implements IServerFinderDoneListener, IServerFinderDisconnectListener {
    private static final AtomicInteger threadNumber = new AtomicInteger(0);
    private final Object portPingerLock = new Object();
    private MServerInfo server;
    private boolean done = false;
    private boolean failed = false;
    private Thread thread;
    private int pingPort;
    private ServerListPinger pinger;
    private boolean notifiedDoneListeners = false;
    private boolean scanPorts;
    private int searchNumber;
    private int currentIncrement = 1;
    private boolean startingIncrement = true;
    private ArrayList<IServerFinderDoneListener> doneListeners = new ArrayList<>();
    private int portPingers = 0;
    private int successfulPortPingers = 0;
    private String pingIP;

    public ServerPinger(boolean scanPorts, int searchNumber) {
        pinger = new ServerListPinger();
        pinger.addServerFinderDisconnectListener(this);
        this.scanPorts = scanPorts;
        this.searchNumber = searchNumber;
    }

    public void addServerFinderDoneListener(IServerFinderDoneListener listener) {
        doneListeners.add(listener);
    }

    public void ping(String ip) {
        ping(ip, 25565);
    }

    public int getSearchNumber() {
        return searchNumber;
    }

    public Thread getThread() {
        return thread;
    }

    public int getPingPort() {
        return pingPort;
    }

    public MServerInfo getServerInfo() {
        return server;
    }

    public void ping(String ip, int port) {
        if (isOldSearch())
            return;

        pingIP = ip;
        pingPort = port;
        server = new MServerInfo("", ip + ":" + port);
        server.version = null;

        if (scanPorts) {
            thread = new Thread(() -> pingInCurrentThread(ip, port),
                    "Server Pinger #" + threadNumber.incrementAndGet());
        } else {
            thread = new Thread(() -> pingInCurrentThread(ip, port),
                    "Server Pinger #" + threadNumber + ", " + port);
        }
        thread.start();
    }

    public ServerListPinger getServerListPinger() {
        return pinger;
    }

    private boolean isOldSearch() {
        return ServerFinderScreen.instance == null || ServerFinderScreen.instance.getState() == ServerFinderScreen.ServerFinderState.CANCELLED || ServerFinderScreen.getSearchNumber() != searchNumber;
    }

    private void runPortIncrement(String ip) {
        synchronized (portPingerLock) {
            portPingers = 0;
            successfulPortPingers = 0;
        }
        for (int i = startingIncrement ? 1 : currentIncrement; i < currentIncrement * 2; i++) {
            if (isOldSearch())
                return;
            ServerPinger pp1 = new ServerPinger(false, searchNumber);
            ServerPinger pp2 = new ServerPinger(false, searchNumber);
            for (IServerFinderDoneListener doneListener : doneListeners) {
                pp1.addServerFinderDoneListener(doneListener);
                pp2.addServerFinderDoneListener(doneListener);
            }
            pp1.addServerFinderDoneListener(this);
            pp2.addServerFinderDoneListener(this);
            if (ServerFinderScreen.instance != null && !isOldSearch()) {
                ServerFinderScreen.instance.incrementTargetChecked(2);
            }
            pp1.ping(ip, 25565 - i);
            pp2.ping(ip, 25565 + i);
        }
        synchronized (portPingerLock) {
            currentIncrement *= 2;
        }
    }

    private void pingInCurrentThread(String ip, int port) {
        if (isOldSearch())
            return;


        try {
            pinger.add(server, () -> {
            });
        } catch (Exception e) {
            failed = true;
        }

        startingIncrement = true;
        if (!failed) {
            currentIncrement = 8;
        }

        if (!failed && scanPorts) {
            runPortIncrement(ip);
        }

        if (failed) {
            pinger.cancel();
            done = true;
            notifyDoneListeners(false);
        }
    }

    public boolean isStillPinging() {
        return !done;
    }

    public boolean isWorking() {
        return !failed;
    }

    public boolean isOtherVersion() {
        return server.protocolVersion != 47;
    }

    public String getServerIP() {
        return server.address;
    }

    @Override
    public void onServerDisconnect() {
        if (isOldSearch())
            return;

        pinger.cancel();
        done = true;
        notifyDoneListeners(false);
    }

    private void notifyDoneListeners(boolean failure) {
        synchronized (this) {
            if (!notifiedDoneListeners) {
                notifiedDoneListeners = true;
                for (IServerFinderDoneListener doneListener : doneListeners) {
                    if (doneListener != null) {
                        if (failure) {
                            doneListener.onServerFailed(this);
                        } else {
                            doneListener.onServerDone(this);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onServerFailed() {
        if (isOldSearch())
            return;

        pinger.cancel();
        done = true;
        notifyDoneListeners(true);
    }

    @Override
    public void onServerDone(ServerPinger pinger) {
        synchronized (portPingerLock) {
            portPingers += 1;
            if (pinger.isWorking())
                successfulPortPingers += 1;
            if (portPingers == (startingIncrement ? currentIncrement * 2 - 2 : currentIncrement) && currentIncrement <= 5000 && successfulPortPingers > 0) {
                startingIncrement = false;
                new Thread(() -> runPortIncrement(pingIP)).start();
            }
        }
    }

    @Override
    public void onServerFailed(ServerPinger pinger) {
        synchronized (portPingerLock) {
            portPingers += 1;
        }
    }
}
