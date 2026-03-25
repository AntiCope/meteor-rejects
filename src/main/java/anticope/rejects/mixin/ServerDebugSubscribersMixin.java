package anticope.rejects.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.debug.ServerDebugSubscribers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerDebugSubscribers.class)
public class ServerDebugSubscribersMixin {

    @Inject(method = "hasRequiredPermissions", at = @At("HEAD"), cancellable = true)
    private void bypassPermissionCheck(ServerPlayer player, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}

