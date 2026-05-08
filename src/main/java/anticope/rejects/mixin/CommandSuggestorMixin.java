package anticope.rejects.mixin;

import anticope.rejects.mixininterface.INoRender;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CommandSuggestions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandSuggestions.class)
public class CommandSuggestorMixin {
    @Inject(method = "extractRenderState", at = @At(value = "HEAD"), cancellable = true)
    public void onRenderCommandSuggestion(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo info) {
        if (((INoRender) Modules.get().get(NoRender.class)).noCommandSuggestions()) info.cancel();
    }
}
