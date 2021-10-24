package anticope.rejects.mixin.meteor;

import anticope.rejects.utils.RejectsConfig;
import meteordevelopment.meteorclient.gui.tabs.builtin.ConfigTab;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;

import java.util.Arrays;
import java.util.List;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ConfigTab.class)
public class ConfigTabMixin {
    @Shadow
    @Final
    private static Settings settings;

    private static final SettingGroup sgRejects = settings.createGroup("Rejects");

    private static final Setting<RejectsConfig.HttpAllowed> httpAllowed = sgRejects.add(new EnumSetting.Builder<RejectsConfig.HttpAllowed>()
            .name("http-allowed")
            .description("Changes what api endpoints can be reached.")
            .defaultValue(RejectsConfig.get().httpAllowed)
            .onChanged(v -> RejectsConfig.get().httpAllowed = v)
            .build()
    );

    private final Setting<List<Module>> hiddenModules = sgRejects.add(new ModuleListSetting.Builder()
            .name("hidden-modules")
            .description("Which modules to hide.")
            .defaultValue(Arrays.asList())
            .defaultValue(RejectsConfig.get().getHiddenModules())
            .onChanged(v -> RejectsConfig.get().setHiddenModules(v))
            .build()
    );

    // No idea why CallbackInfoReturnable, but fabric crashes otherwise lol 
    @Inject(method = "createScreen", at=@At("HEAD"), remap = false)
    private void onCreateScreen(CallbackInfoReturnable<?> cir) {
        hiddenModules.set(RejectsConfig.get().getHiddenModules());
    }
}
