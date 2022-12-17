package anticope.rejects.mixin;

import anticope.rejects.modules.Rendering;
import meteordevelopment.meteorclient.systems.modules.Modules;

import net.minecraft.client.gl.PostEffectProcessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow @Final MinecraftClient client;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;drawEntityOutlinesFramebuffer()V", ordinal = 0))
	private void renderShader(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        Rendering renderingModule = Modules.get().get(Rendering.class);
        if (renderingModule == null) return;
        PostEffectProcessor shader = renderingModule.getShaderEffect();

        if (shader != null) {
            shader.setupDimensions(client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight());
			shader.render(tickDelta);
        }
    }
}
