package cloudburst.rejects.mixin.meteor;

import cloudburst.rejects.utils.RejectsConfig;
import meteordevelopment.meteorclient.gui.tabs.builtin.ConfigTab;
import meteordevelopment.meteorclient.settings.*;
import org.spongepowered.asm.mixin.*;

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
}
