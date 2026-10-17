package anticope.rejects.mixin;

import anticope.rejects.modules.Rendering;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow @Final Minecraft minecraft;
    @Shadow @Final
    CrossFrameResourcePool resourcePool;

    // Minecraft 26.3 refactored GameRenderer:
    //   render(DeltaTracker, boolean)  ->  render()          (no arguments any more)
    //   renderLevel(DeltaTracker)      ->  renderLevel()
    // and LevelRenderer#doEntityOutline was split into executeOutline (private) and
    // blitEntityOutline (public, still invoked from GameRenderer#render()).
    // The callback signature therefore has to be (CallbackInfo) - the old
    // (DeltaTracker, boolean, CallbackInfo) form fails with InvalidInjectionException.
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;blitEntityOutline()V", ordinal = 0))
    private void renderShader(CallbackInfo ci) {
        Rendering renderingModule = Modules.get().get(Rendering.class);
        if (renderingModule == null) return;
        PostChain shader = renderingModule.getShaderEffect();

        if (shader != null) {
            shader.process(this.minecraft.gameRenderer.mainRenderTarget(), this.resourcePool);
        }
    }
}
