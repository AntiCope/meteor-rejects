package anticope.rejects.mixin.meteor;

import anticope.rejects.utils.ConfigModifier;
import meteordevelopment.meteorclient.systems.Systems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Systems.class)
public class SystemsMixin {
	@Inject(method = "init", at = @At(value = "INVOKE", target = "Lmeteordevelopment/meteorclient/systems/System;load()V"), remap = false)
	private static void onInitializeConfig(CallbackInfo ci) {
		// adds the reject settings
		new ConfigModifier();
	}
}