package cloudburst.rejects.mixin;

import net.minecraft.client.MinecraftClient;

import cloudburst.rejects.utils.RejectsUtils;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects the CPS counter.
 */
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin
{
    @Inject(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;doAttack()V"))
    private void onAttack(CallbackInfo ci)
    {
        RejectsUtils.CPS++;
    }
}
