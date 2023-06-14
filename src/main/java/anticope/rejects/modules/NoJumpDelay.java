package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.LivingEntityAccessor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NoJumpDelay extends Module {

    public NoJumpDelay() {
        super(MeteorRejectsAddon.CATEGORY, "no-jump-delay", "NoJumpDelay.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        ((LivingEntityAccessor) mc.player).setJumpCooldown(0);
    }
}
