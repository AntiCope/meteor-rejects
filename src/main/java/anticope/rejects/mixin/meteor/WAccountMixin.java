package anticope.rejects.mixin.meteor;

import anticope.rejects.utils.accounts.CustomYggdrasilAccount;
import meteordevelopment.meteorclient.gui.widgets.WAccount;
import meteordevelopment.meteorclient.systems.accounts.Account;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = WAccount.class, remap = false)
public class WAccountMixin {
    @Shadow @Final private Account<?> account;

    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lmeteordevelopment/meteorclient/gui/GuiTheme;label(Ljava/lang/String;)Lmeteordevelopment/meteorclient/gui/widgets/WLabel;",ordinal = 1))
    private String accountName(String text) {
        if (account instanceof CustomYggdrasilAccount) return "(Yggdrasil)";
        return text;
    }
}
