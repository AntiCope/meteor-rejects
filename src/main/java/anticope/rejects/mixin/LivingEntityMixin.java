package anticope.rejects.mixin;

import anticope.rejects.events.TeleportParticleEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "handleEntityEvent", at = @At("HEAD"))
    private void onHandleStatus(byte status, CallbackInfo ci) {
        if ((Object) this == mc.player && status == 46) {
            MeteorClient.EVENT_BUS.post(TeleportParticleEvent.get(mc.player.getX(), mc.player.getY(), mc.player.getZ()));
        }
    }
}
