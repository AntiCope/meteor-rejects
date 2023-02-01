package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;

public class Boost extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> strength = sgGeneral.add(new DoubleSetting.Builder()
            .name("strength")
            .description("Strength to yeet you with.")
            .defaultValue(4.0)
            .sliderMax(10)
            .build()
    );

    private final Setting<Boolean> autoBoost = sgGeneral.add(new BoolSetting.Builder()
            .name("auto-boost")
            .description("Automatically boosts you.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Integer> interval = sgGeneral.add(new IntSetting.Builder()
            .name("interval")
            .description("Boost interval in ticks.")
            .visible(autoBoost::get)
            .defaultValue(20)
            .sliderMax(120)
            .build()
    );

    private int timer = 0;

    public Boost() {
        super(MeteorRejectsAddon.CATEGORY, "boost", "Works like a dash move.");
    }

    @Override
    public void onActivate() {
        timer = interval.get();
        if (!autoBoost.get()) {
            if (mc.player != null) boost();
            this.toggle();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!autoBoost.get()) return;
        if (timer < 1) {
            boost();
            timer = interval.get();
        } else {
            timer--;
        }
    }

    private void boost() {
        Vec3d v = mc.player.getRotationVecClient().multiply(strength.get());
        mc.player.addVelocity(v.getX(), v.getY(), v.getZ());
    }
}
