package anticope.rejects.mixin.meteor.modules;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.movement.Flight;
import net.minecraft.world.phys.Vec3;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(Flight.class)
public class FlightMixin {
    @Shadow(remap = false)
    @Final
    private SettingGroup sgGeneral;

    private Setting<Boolean> stopMomentum = null;

    @Inject(method = "<init>", at=@At("TAIL"), remap = false)
    private void onInit(CallbackInfo ci) {
        stopMomentum = sgGeneral.add(new BoolSetting.Builder()
            .name("stop-momentum")
            .description("Stops momentum on flight disable")
            .defaultValue(false)
            .build()
        );
    }

    @Inject(method = "onDeactivate", at=@At("TAIL"), remap = false)
    private void onDeactivate(CallbackInfo ci) {
        if (mc.player == null || stopMomentum == null || !stopMomentum.get()) return;

        mc.options.keyUp.setDown(false);
        mc.options.keyLeft.setDown(false);
        mc.options.keyDown.setDown(false);
        mc.options.keyRight.setDown(false);

        mc.player.setDeltaMovement(Vec3.ZERO);
    }
}
