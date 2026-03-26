package anticope.rejects.mixin;

import anticope.rejects.modules.Rendering;
import com.mojang.blaze3d.vertex.PoseStack;
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
    @Inject(
        method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V",
        at = @At("HEAD")
    )
    private void onRender(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState renderState, float f, float g, CallbackInfo ci) {
        if (Modules.get() != null) {
            Rendering renderingModule = Modules.get().get(Rendering.class);
            if (renderingModule != null && renderingModule.deadmau5EarsEnabled()) {
                renderState.showExtraEars = true;
            }
        }
    }
}
