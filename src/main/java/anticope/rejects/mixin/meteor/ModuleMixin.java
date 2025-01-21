package anticope.rejects.mixin.meteor;

import anticope.rejects.utils.RejectsConfig;
import anticope.rejects.utils.RejectsUtils;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Module.class, remap = false)
public class ModuleMixin {
    @Mutable @Shadow public String name;

    @Mutable @Shadow public String title;
  
    @Inject(method = "<init>*", at = @At("TAIL"))
    private void onInit(Category category, String name, String description, String[] aliases, CallbackInfo info) {
        if (RejectsConfig.get().duplicateModuleNames) {
            this.name = RejectsUtils.getModuleName(name);
            this.title = Utils.nameToTitle(this.name);
        }
    }
}
