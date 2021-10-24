package anticope.rejects.mixin;

import anticope.rejects.modules.Rendering;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.render.entity.feature.Deadmau5FeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Deadmau5FeatureRenderer.class)
public class Deadmau5FeatureRendererMixin {
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z"))
    private boolean redirectAllow(String s, Object name){
        if (Modules.get().get(Rendering.class).deadmau5EarsEnabled()) {
            return true; //Allow it always
        }
        return name.equals(s);
    }
}
