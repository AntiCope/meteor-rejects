package cloudburst.rejects.modules;

import meteordevelopment.orbit.EventHandler;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.mixininterface.IVec3d;
import minegame159.meteorclient.systems.modules.Module;
import net.minecraft.util.math.Vec3d;

import cloudburst.rejects.MeteorRejectsAddon;

public class Dolphin extends Module {

    public Dolphin() {
        super(MeteorRejectsAddon.CATEGORY, "dolphin", "Disables underwater gravity.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.options.keySneak.isPressed()) return;

        if (mc.player.isTouchingWater()) {
            Vec3d velocity = mc.player.getVelocity();
            ((IVec3d) velocity).set(velocity.x, 0.002, velocity.z);
        }
    }
}
