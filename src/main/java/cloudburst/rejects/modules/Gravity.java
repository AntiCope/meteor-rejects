package cloudburst.rejects.modules;

import meteordevelopment.orbit.EventHandler;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.mixininterface.IVec3d;
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.systems.modules.Module;
import net.minecraft.util.math.Vec3d;

import cloudburst.rejects.MeteorRejectsAddon;

public class Gravity extends Module {

    public Gravity() {
        super(MeteorRejectsAddon.CATEGORY, "gravity", "Modifies gravity.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> dolphin = sgGeneral.add(new BoolSetting.Builder()
        .name("dolphin")
        .description("Disable underwater gravity.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> moon = sgGeneral.add(new BoolSetting.Builder()
        .name("moon")
        .description("Tired of being on earth?")
        .defaultValue(true)
        .build()
    );

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.options.keySneak.isPressed()) return;

        if (mc.player.isTouchingWater()) {
            if (dolphin.get()) {
                Vec3d velocity = mc.player.getVelocity();
                ((IVec3d) velocity).set(velocity.x, 0.002, velocity.z);
            }
            
        }
        else if (moon.get()) {
            Vec3d velocity = mc.player.getVelocity();
            ((IVec3d) velocity).set(velocity.x, velocity.y + 0.0568000030517578, velocity.z); // Yes, this was precisely calculated.
        }
    }
}
