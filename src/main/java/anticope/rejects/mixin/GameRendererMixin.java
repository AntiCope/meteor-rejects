package anticope.rejects.mixin;

import anticope.rejects.modules.Rendering;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow @Final Minecraft minecraft;
    @Shadow @Final
    CrossFrameResourcePool resourcePool;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V", ordinal = 0))
	private void renderShader(DeltaTracker tickCounter, boolean tick, CallbackInfo ci) {
        Rendering renderingModule = Modules.get().get(Rendering.class);
        if (renderingModule == null) return;
        PostChain shader = renderingModule.getShaderEffect();

        if (shader != null) {
//            shader.setupDimensions(client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight());
            shader.process(this.minecraft.getMainRenderTarget(), this.resourcePool);
        }
    }
}
