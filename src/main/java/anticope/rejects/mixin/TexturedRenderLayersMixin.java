package anticope.rejects.mixin;

import anticope.rejects.modules.Rendering;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.render.block.entity.ChestBlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChestBlockEntityRenderer.class)
public class TexturedRenderLayersMixin {
    @Inject(method = "isAroundChristmas", at = @At("HEAD"), cancellable = true)
    private static void onIsAroundChristmas(CallbackInfoReturnable<Boolean> cir) {
        // Check if Modules system is initialized
        if (Modules.get() != null) {
            Rendering rendering = Modules.get().get(Rendering.class);
            if (rendering != null && rendering.chistmas()) {
                cir.setReturnValue(true);
            }
        }
    }
}
