package anticope.rejects.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import anticope.rejects.events.TeleportParticleEvent;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.Utils;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "handleStatus", at = @At("HEAD"), cancellable = true)
    private void onHandleStatus(byte status, CallbackInfo ci) {
        if ((Object) this instanceof Entity thisPlayer) {
            if ((Object) thisPlayer == mc.player && status == 46 && Utils.canUpdate()) {
                MeteorClient.EVENT_BUS.post(TeleportParticleEvent.get(thisPlayer.getX(), thisPlayer.getY(), thisPlayer.getZ()));
            }
        }
    }
}
