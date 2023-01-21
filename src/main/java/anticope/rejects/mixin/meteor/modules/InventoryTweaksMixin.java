package anticope.rejects.mixin.meteor.modules;

import anticope.rejects.mixininterface.IInventoryTweaks;
import meteordevelopment.meteorclient.systems.modules.misc.InventoryTweaks;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryTweaks.class)
public abstract class InventoryTweaksMixin implements IInventoryTweaks {
    private Runnable callback;

    @Inject(method = "lambda$steal$3", at = @At("RETURN"))
    private void afterSteal(ScreenHandler handler, CallbackInfo info) {
        if (callback != null) {
            callback.run();
            callback = null;
        }
    }

    @Override
    public void afterSteal(Runnable callback) {
        this.callback = callback;
    }
}
