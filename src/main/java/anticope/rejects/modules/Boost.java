package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import net.minecraft.util.math.Vec3d;

import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;

public class Boost extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> strength = sgGeneral.add(new DoubleSetting.Builder()
        .name("strength")
        .description("Strength to yeet you with.")
        .defaultValue(4.0)
        .min(0.5)
        .sliderMax(10)
        .build()
    );

    public Boost() {
        super(MeteorRejectsAddon.CATEGORY, "boost", "Works like a dash move.");
    }
    
    @Override
    public void onActivate() {
        if (mc.player ==  null) {
            this.toggle();
            return;
        }
        Vec3d v = mc.player.getRotationVecClient().multiply(strength.get());
        mc.player.addVelocity(v.getX(), v.getY(), v.getZ());
        this.toggle();
    }
}
