package cloudburst.rejects.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.GameRenderer;

import cloudburst.rejects.modules.Shaders;
import minegame159.meteorclient.systems.modules.Modules;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow @Final private MinecraftClient client;

    @Redirect(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;shader:Lnet/minecraft/client/gl/ShaderEffect;", ordinal = 0))
	private ShaderEffect renderShader(GameRenderer renderer, float tickDelta) {
        ShaderEffect shader = Modules.get().get(Shaders.class).getShaderEffect();

        if (shader != null) {
            shader.setupDimensions(client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight());
			shader.render(tickDelta);
        }

        return null;
    }
}
