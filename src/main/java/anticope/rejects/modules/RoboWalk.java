package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.mixin.PlayerMoveC2SPacketAccessor;
import anticope.rejects.mixin.VehicleMoveC2SPacketAccessor;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.world.phys.Vec3;

public class RoboWalk extends Module {
    public RoboWalk() {
        super(MeteorRejectsAddon.CATEGORY, "robo-walk", "Bypasses LiveOverflow movement check.");
    }

    private double smooth(double d) {
        double temp = (double) Math.round(d * 100) / 100;
        return Math.nextAfter(temp, temp + Math.signum(d));
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (event.packet instanceof ServerboundMovePlayerPacket packet) {
            if (!packet.hasPosition()) return;

            double x = smooth(packet.getX(0));
            double z = smooth(packet.getZ(0));

            ((PlayerMoveC2SPacketAccessor) packet).setX(x);
            ((PlayerMoveC2SPacketAccessor) packet).setZ(z);
        } else if (event.packet instanceof ServerboundMoveVehiclePacket packet) {
            Vec3 pos = ((VehicleMoveC2SPacketAccessor) (Object) packet).getPosition();
            double x = smooth(pos.x());
            double z = smooth(pos.z());

            event.packet = VehicleMoveC2SPacketAccessor.create(new Vec3(x, pos.y(), z), packet.yRot(), packet.xRot(), packet.onGround());
        }
    }
}
