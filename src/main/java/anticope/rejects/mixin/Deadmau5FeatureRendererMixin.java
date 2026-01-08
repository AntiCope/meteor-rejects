package anticope.rejects.mixin;

import anticope.rejects.modules.Rendering;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.Deadmau5EarsLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Deadmau5EarsLayer.class)
public class Deadmau5FeatureRendererMixin {
    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void onRender(com.mojang.blaze3d.vertex.PoseStack matrixStack, SubmitNodeCollector queue, int light, AvatarRenderState renderState, float limbAngle, float limbDistance, CallbackInfo ci) {
        // Check if Modules system is initialized
        if (Modules.get() != null) {
            Rendering renderingModule = Modules.get().get(Rendering.class);
            if (renderingModule != null && renderingModule.deadmau5EarsEnabled()) {
                // Allow rendering by not canceling
                return;
            }
        }

        // Default vanilla behavior: only render for "deadmau5"
        if (renderState.nameTag != null) {
            String playerName = renderState.nameTag.getString();
            if (!playerName.equals("deadmau5")) {
                ci.cancel();
            }
        } else {
            ci.cancel();
        }
    }
}
