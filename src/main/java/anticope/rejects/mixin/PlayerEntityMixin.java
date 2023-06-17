package anticope.rejects.mixin;

import anticope.rejects.events.OffGroundSpeedEvent;
import anticope.rejects.modules.FullNoClip;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(method = "getOffGroundSpeed", at = @At("RETURN"), cancellable = true)
    private void onGetOffGroundSpeed(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(MeteorClient.EVENT_BUS.post(OffGroundSpeedEvent.get(cir.getReturnValueF())).speed);
    }
    @Redirect(method = {"tick", "updatePose"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSpectator()Z"))
    private boolean FullNoClip(PlayerEntity player) {
        return player.isSpectator() || Modules.get().isActive(FullNoClip.class);
    }
}
