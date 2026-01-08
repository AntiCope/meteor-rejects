package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;

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
        if (event.packet instanceof ClientboundExplodePacket packet) {
            Vec3 explodePos = packet.center();
            Vec3 playerKnockback = new Vec3(0, 0, 0);
            if(packet.playerKnockback().isPresent()) {
                playerKnockback = packet.playerKnockback().get();
            }
            if (/* outside of world */ explodePos.x() > 30_000_000 || explodePos.y() > 30_000_000 || explodePos.z() > 30_000_000 || explodePos.x() < -30_000_000 || explodePos.y() < -30_000_000 || explodePos.z() < -30_000_000 ||
                    // too much knockback
                    playerKnockback.x > 30_000_000 || playerKnockback.y > 30_000_000 || playerKnockback.z > 30_000_000
                    // knockback can be negative?
                    || playerKnockback.x < -30_000_000 || playerKnockback.y < -30_000_000 || playerKnockback.z < -30_000_000
            ) cancel(event);
        } else if (event.packet instanceof ClientboundLevelParticlesPacket packet) {
            // too many particles
            if (packet.getCount() > 100_000) cancel(event);
        } else if (event.packet instanceof ClientboundPlayerPositionPacket packet) {
            Vec3 playerPos = packet.change().position();
            // out of world movement
            if (playerPos.x > 30_000_000 || playerPos.y > 30_000_000 || playerPos.z > 30_000_000 || playerPos.x < -30_000_000 || playerPos.y < -30_000_000 || playerPos.z < -30_000_000)
                cancel(event);
        } else if (event.packet instanceof ClientboundSetEntityMotionPacket packet) {
            // velocity
            if (packet.getMovement().x > 30_000_000 || packet.getMovement().y > 30_000_000 || packet.getMovement().z > 30_000_000
                    || packet.getMovement().x < -30_000_000 || packet.getMovement().y  < -30_000_000 || packet.getMovement().z < -30_000_000
            ) cancel(event);
        }
    }

    private void cancel(PacketEvent.Receive event) {
        if (log.get()) warning("Server attempts to crash you");
        event.cancel();
    }
}
