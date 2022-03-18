package anticope.rejects.mixin;

import anticope.rejects.modules.modifier.NoRenderModifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;

@Mixin(ToastManager.class)
public class ToastManagerMixin {
    @Inject(method="add", at = @At("HEAD"), cancellable = true)
    public void preventAdd(Toast toast, CallbackInfo ci) {
        if (NoRenderModifier.disableToasts()) ci.cancel();
    }
}
