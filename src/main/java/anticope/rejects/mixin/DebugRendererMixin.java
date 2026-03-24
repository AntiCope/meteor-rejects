package anticope.rejects.mixin;

import anticope.rejects.modules.DebugRender;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {

    @Inject(method = "emitGizmos", at = @At("HEAD"))
    public void render(Frustum frustum, double d, double e, double f, float g, CallbackInfo ci) {
        DebugRender debugRender = Modules.get().get(DebugRender.class);
        if (debugRender != null && debugRender.isActive()) {
            debugRender.render(frustum, d, e, f, g);
        }
    }

}