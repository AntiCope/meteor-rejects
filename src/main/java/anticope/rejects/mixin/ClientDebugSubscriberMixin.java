package anticope.rejects.mixin;

import anticope.rejects.modules.DebugRender;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.multiplayer.ClientDebugSubscriber;
import net.minecraft.util.debug.DebugSubscription;
import net.minecraft.util.debug.DebugSubscriptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ClientDebugSubscriber.class)
public class ClientDebugSubscriberMixin {

    @Inject(method = "requestedSubscriptions", at = @At("RETURN"))
    private void addModuleSubscriptions(CallbackInfoReturnable<Set<DebugSubscription<?>>> cir) {
        DebugRender module = Modules.get().get(DebugRender.class);
        if (module == null || !module.isActive()) return;

        Set<DebugSubscription<?>> set = cir.getReturnValue();
        set.add(DebugSubscriptions.BEES);
        set.add(DebugSubscriptions.BEE_HIVES);
        set.add(DebugSubscriptions.BRAINS);
        set.add(DebugSubscriptions.BREEZES);
        set.add(DebugSubscriptions.GOAL_SELECTORS);
        set.add(DebugSubscriptions.ENTITY_PATHS);
        set.add(DebugSubscriptions.ENTITY_BLOCK_INTERSECTIONS);
        set.add(DebugSubscriptions.POIS);
        set.add(DebugSubscriptions.REDSTONE_WIRE_ORIENTATIONS);
        set.add(DebugSubscriptions.VILLAGE_SECTIONS);
        set.add(DebugSubscriptions.RAIDS);
        set.add(DebugSubscriptions.STRUCTURES);
        set.add(DebugSubscriptions.GAME_EVENT_LISTENERS);
        set.add(DebugSubscriptions.NEIGHBOR_UPDATES);
        set.add(DebugSubscriptions.GAME_EVENTS);
    }
}

