package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.HappyGhastEntity;
import meteordevelopment.orbit.EventHandler;

public class HappyGhastSpeed extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> flyingSpeed = sgGeneral.add(new DoubleSetting.Builder()
            .name("flying-speed")
            .description("Flying speed for the Happy Ghast you are riding.")
            .defaultValue(0.25)
            .min(0.01)
            .sliderMax(1.0)
            .build()
    );

    private final Setting<Double> movementSpeed = sgGeneral.add(new DoubleSetting.Builder()
            .name("movement-speed")
            .description("Movement speed for the Happy Ghast you are riding.")
            .defaultValue(0.25)
            .min(0.01)
            .sliderMax(1.0)
            .build()
    );

    private final Setting<Double> cameraDistance = sgGeneral.add(new DoubleSetting.Builder()
            .name("camera-distance")
            .description("Camera distance for the Happy Ghast you are riding.")
            .defaultValue(14.0)
            .min(1.0)
            .sliderMax(50.0)
            .build()
    );

    public HappyGhastSpeed() {
        super(MeteorRejectsAddon.CATEGORY, "happy-ghast-speed", "Customizes speed and camera distance for the Happy Ghast you are riding.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;
        if (!(mc.player.getVehicle() instanceof HappyGhastEntity ghast)) return;

        // Only modify the ghast the player is riding (client-side only)
        if (ghast.getAttributeInstance(EntityAttributes.FLYING_SPEED) != null) {
            ghast.getAttributeInstance(EntityAttributes.FLYING_SPEED).setBaseValue(flyingSpeed.get());
        }
        if (ghast.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED) != null) {
            ghast.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(movementSpeed.get());
        }
        if (ghast.getAttributeInstance(EntityAttributes.CAMERA_DISTANCE) != null) {
            ghast.getAttributeInstance(EntityAttributes.CAMERA_DISTANCE).setBaseValue(cameraDistance.get());
        }
    }
}
