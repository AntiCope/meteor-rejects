package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;

public class AntiCrash extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> log = sgGeneral.add(new BoolSetting.Builder()
            .name("log")
            .description("Logs when crash packet detected.")
            .defaultValue(false)
            .build()
    );

    public AntiCrash() {
        super(MeteorRejectsAddon.CATEGORY, "anti-crash", "Attempts to cancel packets that may crash the client.");
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof ExplosionS2CPacket packet) {
            Vec3d explodePos = packet.center();
            Vec3d playerKnockback = new Vec3d(0, 0, 0);
            if(packet.playerKnockback().isPresent()) {
                playerKnockback = packet.playerKnockback().get();
            }
            if (/* outside of world */ explodePos.getX() > 30_000_000 || explodePos.getY() > 30_000_000 || explodePos.getZ() > 30_000_000 || explodePos.getX() < -30_000_000 || explodePos.getY() < -30_000_000 || explodePos.getZ() < -30_000_000 ||
                    // too much knockback
                    playerKnockback.x > 30_000_000 || playerKnockback.y > 30_000_000 || playerKnockback.z > 30_000_000
                    // knockback can be negative?
                    || playerKnockback.x < -30_000_000 || playerKnockback.y < -30_000_000 || playerKnockback.z < -30_000_000
            ) cancel(event);
        } else if (event.packet instanceof ParticleS2CPacket packet) {
            // too many particles
            if (packet.getCount() > 100_000) cancel(event);
        } else if (event.packet instanceof PlayerPositionLookS2CPacket packet) {
            Vec3d playerPos = packet.change().position();
            // out of world movement
            if (playerPos.x > 30_000_000 || playerPos.y > 30_000_000 || playerPos.z > 30_000_000 || playerPos.x < -30_000_000 || playerPos.y < -30_000_000 || playerPos.z < -30_000_000)
                cancel(event);
        } else if (event.packet instanceof EntityVelocityUpdateS2CPacket packet) {
            // velocity
            if (packet.getVelocity().x > 30_000_000 || packet.getVelocity().y > 30_000_000 || packet.getVelocity().z > 30_000_000
                    || packet.getVelocity().x < -30_000_000 || packet.getVelocity().y  < -30_000_000 || packet.getVelocity().z < -30_000_000
            ) cancel(event);
        }
    }

    private void cancel(PacketEvent.Receive event) {
        if (log.get()) warning("Server attempts to crash you");
        event.cancel();
    }
}
