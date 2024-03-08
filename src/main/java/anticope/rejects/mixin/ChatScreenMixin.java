package anticope.rejects.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.BetterChat;
import meteordevelopment.meteorclient.systems.config.Config;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
    @Shadow protected TextFieldWidget chatField;
	
    @Inject(method = "onChatFieldUpdate", at = @At("HEAD"))
    private void onChatFieldUpdate(String chatText, CallbackInfo ci) {
        setCommandMaxLength(chatText);
    }
	
    @ModifyArg(method = "setChatFromHistory", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;setText(Ljava/lang/String;)V"))
    private String setText(String text) {
        setCommandMaxLength(text);
        return text;
    }
	
    private void setCommandMaxLength(String message) {
		String prefix = Config.get().prefix.get();
		
        if (message.startsWith(prefix) || (Modules.get().get(BetterChat.class).isInfiniteChatBox()))
            chatField.setMaxLength(Integer.MAX_VALUE);
        else {
            if (chatField.getCursor() > 256)
                chatField.setCursor(Math.min(256, chatField.getText().length()));
            chatField.setMaxLength(256);
        }
    }
	
    @Inject(method = "normalize", at = @At(value = "HEAD"), cancellable = true)
    private void avoidNormalizeWithCommand(String str, CallbackInfoReturnable<String> cir) {
		String prefix = Config.get().prefix.get();
		
        if (str.startsWith(prefix) || (Modules.get().get(BetterChat.class).isInfiniteChatBox()))
            cir.setReturnValue(str);
    }
}