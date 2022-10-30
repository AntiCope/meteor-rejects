package anticope.rejects.mixin.meteor;

import anticope.rejects.utils.RejectsConfig;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Module.class)
public class ModuleMixin {
    @Mutable @Shadow public String name;

    @Mutable @Shadow public String title;

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void onInit(Category category, String name, String description, CallbackInfo info) {
        if (RejectsConfig.get().duplicateModuleNames) {
            this.name = getModuleName(name);
            this.title = Utils.nameToTitle(this.name);
        }
    }

    private static String getModuleName(String name) {
        boolean dupe = false;
        for (Module module : Modules.get().getAll()) {
            if (module.name.equals(name)) {
                dupe = true;
                break;
            }
        }
        if (dupe) name += "*";
        return name;
    }
}
