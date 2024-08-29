package anticope.rejects.mixin;

import anticope.rejects.modules.DebugRender;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    public void render(MatrixStack matrixStack, VertexConsumerProvider.Immediate immediate, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        DebugRender debugRender = Modules.get().get(DebugRender.class);
        if (debugRender != null && debugRender.isActive()) {
            debugRender.render(matrixStack, immediate, cameraX, cameraY, cameraZ);
        }
    }


}