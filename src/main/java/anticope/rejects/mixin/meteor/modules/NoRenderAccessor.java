package anticope.rejects.mixin.meteor.modules;

import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NoRender.class)
public interface NoRenderAccessor {
    @Accessor("sgOverlay")
    SettingGroup getSgOverlay();
}
