package anticope.rejects.mixin;

import anticope.rejects.modules.Rendering;
import meteordevelopment.meteorclient.systems.modules.Modules;

import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.Deadmau5FeatureRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Deadmau5FeatureRenderer.class)
public class Deadmau5FeatureRendererMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(net.minecraft.client.util.math.MatrixStack matrixStack, OrderedRenderCommandQueue queue, int light, PlayerEntityRenderState renderState, float limbAngle, float limbDistance, CallbackInfo ci) {
        // Check if Modules system is initialized
        if (Modules.get() != null) {
            Rendering renderingModule = Modules.get().get(Rendering.class);
            if (renderingModule != null && renderingModule.deadmau5EarsEnabled()) {
                // Allow rendering by not canceling
                return;
            }
        }

        // Default vanilla behavior: only render for "deadmau5"
        if (renderState.displayName != null) {
            String playerName = renderState.displayName.getString();
            if (!playerName.equals("deadmau5")) {
                ci.cancel();
            }
        } else {
            ci.cancel();
        }
    }
}
