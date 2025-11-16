package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.LivingEntityAccessor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.systems.modules.Module;

public class NoJumpDelay extends Module {

    public NoJumpDelay() {
        super(MeteorRejectsAddon.CATEGORY, "no-jump-delay", "NoJumpDelay.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        // TODO: Fix for 1.21.10 - LivingEntityAccessor.setJumpCooldown() may have changed
        // try {
        //     ((LivingEntityAccessor) mc.player).setJumpCooldown(0);
        // } catch (NoSuchMethodError e) {
        //     // Accessor might have changed in newer versions
        // }
    }
}
