package anticope.rejects.mixin.meteor;

import anticope.rejects.utils.accounts.CustomYggdrasilAccount;
import meteordevelopment.meteorclient.systems.accounts.Account;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = Account.class)
public class AccountMixin {
    @ModifyArg(method = "toTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;putString(Ljava/lang/String;Ljava/lang/String;)V", ordinal = 0), index = 1)
    private String putString(String key) {
        if ((Object) this instanceof CustomYggdrasilAccount) return "Yggdrasil";
        return key;
    }
}
