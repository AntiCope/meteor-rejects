package anticope.rejects.mixin;

import anticope.rejects.modules.Rendering;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {

    @Inject(method = "setupRotations", at = @At(value = "TAIL"))
    private void dinnerboneEntities(S state, PoseStack matrices, float animationProgress, float bodyYaw, CallbackInfo ci) {
        Rendering renderingModule = Modules.get().get(Rendering.class);
        if (renderingModule == null) return;
        if ((!(state instanceof AvatarRenderState)) && renderingModule.dinnerboneEnabled()) {
            matrices.translate(0.0D, state.boundingBoxHeight + 0.1F, 0.0D);
            matrices.mulPose(Axis.ZP.rotationDegrees(180.0F));
        }
    }

}
