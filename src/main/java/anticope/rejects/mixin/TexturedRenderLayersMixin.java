package anticope.rejects.mixin;

import anticope.rejects.modules.Rendering;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ChestRenderer.class)
public class TexturedRenderLayersMixin {
    @ModifyArg(
        method = "extractRenderState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/blockentity/ChestRenderer;getChestMaterial(Lnet/minecraft/world/level/block/entity/BlockEntity;Z)Lnet/minecraft/client/renderer/blockentity/state/ChestRenderState$ChestMaterialType;"
        ),
        index = 1
    )
    private boolean modifyXmasTexturesArg(boolean original) {
        if (Modules.get() != null) {
            Rendering rendering = Modules.get().get(Rendering.class);
            if (rendering != null && rendering.chistmas()) return true;
        }
        return original;
    }
}
