package cloudburst.rejects.modules.modifier;

import cloudburst.rejects.mixin.meteor.modules.NoRenderAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;

public class NoRenderModifier {
    static SettingGroup sgOverlay = ((NoRenderAccessor) Modules.get().get(NoRender.class)).getSgOverlay();
    
    public static final Setting<Boolean> noCommandSuggestions = sgOverlay.add(new BoolSetting.Builder()
            .name("command-suggestions")
            .description("Disables command suggestions in chat.")
            .defaultValue(false)
            .build()
    );
    
    public static boolean noCommandSuggestions() {
        return Modules.get().get(NoRender.class).isActive() && noCommandSuggestions.get();
    }
}
