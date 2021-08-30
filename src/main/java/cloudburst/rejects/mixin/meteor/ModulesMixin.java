package cloudburst.rejects.mixin.meteor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import cloudburst.rejects.utils.RejectsConfig;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;

@Mixin(Modules.class)
public class ModulesMixin {
    @Shadow
    @Final
    private Map<Category, List<Module>> groups;

    @Inject(method = "getGroup", at=@At("HEAD"), cancellable = true, remap = false)
    private void onGetGroup(Category category, CallbackInfoReturnable<List<Module>> cir) {
        Set<String> hiddenModules = RejectsConfig.get().hiddenModules;
        if (hiddenModules.isEmpty()) return;

        List<Module> foundModules = groups.computeIfAbsent(category, category1 -> new ArrayList<>());
        foundModules.removeIf(m -> hiddenModules.contains(m.name));

        cir.setReturnValue(foundModules);
    }
}
