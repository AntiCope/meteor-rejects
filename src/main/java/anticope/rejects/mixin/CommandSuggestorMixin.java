package anticope.rejects.mixin;

import anticope.rejects.modules.modifier.NoRenderModifier;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatInputSuggestor.class)
public class CommandSuggestorMixin {
    @Inject(method = "render", at = @At(value = "HEAD"), cancellable = true)
    public void onRenderCommandSuggestion(MatrixStack matrices, int mouseX, int mouseY, CallbackInfo info) {
        if (NoRenderModifier.noCommandSuggestions()) info.cancel();
    }
}
