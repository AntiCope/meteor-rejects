package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.mixin.PlayerMoveC2SPacketAccessor;
import anticope.rejects.mixin.VehicleMoveC2SPacketAccessor;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;

public class RoboWalk extends Module {
    public RoboWalk() {
        super(MeteorRejectsAddon.CATEGORY, "robo-walk", "Bypasses LiveOverflow movement check.");
    }

    private double smooth(double d) {
        return Math.round(d * 100.0d) / 100.0d;
    }

    private boolean skip(double x, double z) {
        long dx = ((long) (x * 1000)) % 10;
        long dz = ((long) (z * 1000)) % 10;
        return dx != 0 || dz != 0;
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket packet) {
            if (!packet.changesPosition()) return;

            double x = smooth(packet.getX(0));
            double z = smooth(packet.getZ(0));

            if (skip(x, z)) return;

            ((PlayerMoveC2SPacketAccessor) packet).setX(x);
            ((PlayerMoveC2SPacketAccessor) packet).setZ(z);
        } else if (event.packet instanceof VehicleMoveC2SPacket packet) {
            double x = smooth(packet.getX());
            double z = smooth(packet.getZ());

            if (skip(x, z)) return;

            ((VehicleMoveC2SPacketAccessor) packet).setX(x);
            ((VehicleMoveC2SPacketAccessor) packet).setZ(z);
        }
    }
}
