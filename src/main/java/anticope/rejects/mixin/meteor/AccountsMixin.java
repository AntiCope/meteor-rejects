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
    @Inject(method = "lambda$fromTag$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;getString(Ljava/lang/String;)Ljava/lang/String;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private static void onFromTag(Tag tag1, CallbackInfoReturnable<Account<?>> cir, CompoundTag t) {
        if (t.getString("type").equals("Yggdrasil")) {
            Account<CustomYggdrasilAccount> account = new CustomYggdrasilAccount(null, null, null).fromTag(t);
            if (account.fetchInfo()) cir.setReturnValue(account);
        }
    }
}
