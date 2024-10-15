package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.annotation.AutoRegister;
import anticope.rejects.events.OffGroundSpeedEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

@AutoRegister
public class Jetpack extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> jetpackSpeed = sgGeneral.add(new DoubleSetting.Builder()
            .name("jetpack-speed")
            .description("How fast while ascending.")
            .defaultValue(0.42)
            .min(0)
            .sliderMax(1)
            .build()
    );

    public Jetpack() {
        super(MeteorRejectsAddon.CATEGORY, "jetpack", "Flies as if using a jetpack.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.options.jumpKey.isPressed()) {
            ((IVec3d) mc.player.getVelocity()).setY(jetpackSpeed.get());
        }
    }

    @EventHandler
    private void onOffGroundSpeed(OffGroundSpeedEvent event) {
        event.speed = mc.player.getMovementSpeed() * jetpackSpeed.get().floatValue();
    }
}

