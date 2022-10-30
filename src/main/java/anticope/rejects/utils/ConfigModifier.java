package anticope.rejects.utils;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Module;

import java.util.List;

public class ConfigModifier {

    private static ConfigModifier INSTANCE;

    public final SettingGroup sgRejects = Config.get().settings.createGroup("Rejects");

    public final Setting<RejectsConfig.HttpAllowed> httpAllowed = sgRejects.add(new EnumSetting.Builder<RejectsConfig.HttpAllowed>()
            .name("http-allowed")
            .description("Changes what api endpoints can be reached.")
            .defaultValue(RejectsConfig.get().httpAllowed)
            .onChanged(v -> RejectsConfig.get().httpAllowed = v)
            .build()
    );

    public final Setting<List<Module>> hiddenModules = sgRejects.add(new ModuleListSetting.Builder()
            .name("hidden-modules")
            .description("Which modules to hide.")
            .defaultValue(List.of())
            .defaultValue(RejectsConfig.get().getHiddenModules())
            .onChanged(v -> RejectsConfig.get().setHiddenModules(v))
            .build()
    );

    public final Setting<Boolean> loadSystemFonts = sgRejects.add(new BoolSetting.Builder()
            .name("load-system-fonts")
            .description("Disabling this for faster launch. You can put font into meteor-client/fonts folder. Restart to take effect.")
            .defaultValue(true)
            .defaultValue(RejectsConfig.get().loadSystemFonts)
            .onChanged(v -> RejectsConfig.get().loadSystemFonts = v)
            .build()
    );

    public final Setting<Boolean> duplicateModuleNames = sgRejects.add(new BoolSetting.Builder()
            .name("duplicate-module-names")
            .description("Allow duplicate module names. Best for addon compatibility.")
            .defaultValue(true)
            .defaultValue(RejectsConfig.get().duplicateModuleNames)
            .onChanged(v -> RejectsConfig.get().duplicateModuleNames = v)
            .build()
    );

    public static ConfigModifier get() {
        if (INSTANCE == null) INSTANCE = new ConfigModifier();
        return INSTANCE;
    }
}
