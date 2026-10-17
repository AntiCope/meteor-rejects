package anticope.rejects.mixin.meteor.modules;

import anticope.rejects.mixininterface.IInventoryTweaks;
import meteordevelopment.meteorclient.systems.modules.misc.InventoryTweaks;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InventoryTweaks.class, remap = false)
public abstract class InventoryTweaksMixin implements IInventoryTweaks {
    private Runnable callback;

    // The steal work runs inside a lambda handed to MeteorExecutor; its synthetic name has to
    // match the Meteor Client build this addon is compiled against.
    @Inject(method = "lambda$steal$0", at = @At("RETURN"))
    private void afterSteal(AbstractContainerMenu handler, CallbackInfo info) {
        if (callback != null) {
            callback.run();
            callback = null;
        }
    }

    @Override
    public void stealCallback(Runnable callback) {
        this.callback = callback;
    }

    @Inject(method = "lambda$new$1", at = @At("HEAD"))
    private void onStealChanged(Boolean b, CallbackInfo info) {
        callback = null;
    }
}
