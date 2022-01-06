package anticope.rejects.utils;

import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.ModuleListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Module;

import java.util.Arrays;
import java.util.List;

public class ConfigModifier {

    public static ConfigModifier INSTANCE;

    public ConfigModifier() {
        INSTANCE = this;
    }

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
            .defaultValue(Arrays.asList())
            .defaultValue(RejectsConfig.get().getHiddenModules())
            .onChanged(v -> RejectsConfig.get().setHiddenModules(v))
            .build()
    );
}
