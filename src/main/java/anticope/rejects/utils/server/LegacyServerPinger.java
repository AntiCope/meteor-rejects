package anticope.rejects.utils.server;

import anticope.rejects.MeteorRejectsAddon;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.server.network.EventLoopGroupHolder;

public class LegacyServerPinger {
    private static final AtomicInteger threadNumber = new AtomicInteger(0);
    private ServerData server;
    private boolean done = false;
    private boolean failed = false;

    public void ping(String ip) {
        ping(ip, 25565);
    }

    public void ping(String ip, int port) {
        server = new ServerData("", ip + ":" + port, ServerData.Type.OTHER);

        new Thread(() -> pingInCurrentThread(ip, port),
                "Server Pinger #" + threadNumber.incrementAndGet()).start();
    }

    private void pingInCurrentThread(String ip, int port) {
        ServerStatusPinger pinger = new ServerStatusPinger();
        MeteorRejectsAddon.LOG.info("Pinging {}:{}...", ip, port);

        try {
            pinger.pingServer(server, () -> {}, () -> {}, EventLoopGroupHolder.remote(false));
            MeteorRejectsAddon.LOG.info("Ping successful: {}:{}", ip, port);

        } catch (UnknownHostException e) {
            MeteorRejectsAddon.LOG.warn("Unknown host: {}:{}", ip, port);
            failed = true;

        } catch (Exception e2) {
            MeteorRejectsAddon.LOG.warn("Ping failed: {}:{}", ip, port);
            failed = true;
        }

        pinger.removeAll();
        done = true;
    }

    public boolean isStillPinging() {
        return !done;
    }

    public boolean isWorking() {
        return !failed;
    }

    public String getServerIP() {
        return server.ip;
    }
}
