package anticope.rejects.mixin.meteor.modules;

import anticope.rejects.utils.RejectsUtils;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = KillAura.class, remap = false)
public class KillAuraMixin {
    @Shadow
    @Final
    private SettingGroup sgGeneral;

    private Setting<Double> fov;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        fov = sgGeneral.add(new DoubleSetting.Builder()
                .name("fov")
                .description("Will only aim entities in the fov.")
                .defaultValue(360)
                .min(0)
                .max(360)
                .build()
        );
    }

    @Inject(method = "entityCheck", at = @At(value = "RETURN", ordinal = 14), cancellable = true)
    private void onReturn(Entity entity, CallbackInfoReturnable<Boolean> info) {
        info.setReturnValue(info.getReturnValueZ() && RejectsUtils.inFov(entity, fov.get()));
    }
}
