package anticope.rejects.mixin;

import anticope.rejects.modules.Rendering;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;

import meteordevelopment.meteorclient.systems.modules.Modules;

@Mixin(ToastManager.class)
public class ToastManagerMixin {
    @Inject(method="add", at = @At("HEAD"), cancellable = true)
    public void preventAdd(Toast toast, CallbackInfo ci) {
        if (Modules.get().get(Rendering.class).disableToasts()) ci.cancel();
    }
}
