package anticope.rejects.mixin.meteor;

import anticope.rejects.utils.accounts.CustomYggdrasilAccount;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.accounts.Accounts;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = Accounts.class)
public class AccountsMixin {
    // Meteor 26.3 restructured Accounts#fromTag: the Runnable handed to MeteorExecutor is now
    // lambda$fromTag$0 and the per-account NbtUtils.ToValue is lambda$fromTag$1.
    // CompoundTag#getString returns Optional<String> in 26.3, so Meteor reads the account type
    // with getStringOr(String, String) - that is the call this injection has to hook.
    @Inject(method = "lambda$fromTag$1", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;getStringOr(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private static void onFromTag(Tag tag1, CallbackInfoReturnable<Account<?>> cir, CompoundTag t) {
        if ("Yggdrasil".equals(t.getStringOr("type", ""))) {
            Account<CustomYggdrasilAccount> account = new CustomYggdrasilAccount(null, null, null).fromTag(t);
            if (account.fetchInfo()) cir.setReturnValue(account);
        }
    }
}
