package cloudburst.rejects.modules;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.util.math.Vec3d;

import cloudburst.rejects.MeteorRejectsAddon;

public class Gravity extends Module {

    public Gravity() {
        super(MeteorRejectsAddon.CATEGORY, "gravity", "Modifies gravity.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> moon = sgGeneral.add(new BoolSetting.Builder()
        .name("moon")
        .description("Tired of being on earth?")
        .defaultValue(true)
        .build()
    );

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.options.keySneak.isPressed()) return;

        if (moon.get()) {
            Vec3d velocity = mc.player.getVelocity();
            ((IVec3d) velocity).set(velocity.x, velocity.y + 0.0568000030517578, velocity.z);
        }
    }
}
