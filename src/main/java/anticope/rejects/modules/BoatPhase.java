package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.entity.BoatMoveEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;

public class BoatPhase extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSpeeds = settings.createGroup("Speeds");

    private final Setting<Boolean> lockYaw = sgGeneral.add(new BoolSetting.Builder()
            .name("lock-boat-yaw")
            .description("Locks boat yaw to the direction you're facing.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> verticalControl = sgGeneral.add(new BoolSetting.Builder()
            .name("vertical-control")
            .description("Whether or not space/ctrl can be used to move vertically.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> adjustHorizontalSpeed = sgGeneral.add(new BoolSetting.Builder()
            .name("adjust-horizontal-speed")
            .description("Whether or not horizontal speed is modified.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> fall = sgGeneral.add(new BoolSetting.Builder()
            .name("fall")
            .description("Toggles vertical glide.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Double> horizontalSpeed = sgSpeeds.add(new DoubleSetting.Builder()
            .name("horizontal-speed")
            .description("Horizontal speed in blocks per second.")
            .defaultValue(10)
            .min(0)
            .sliderMax(50)
            .build()
    );

    private final Setting<Double> verticalSpeed = sgSpeeds.add(new DoubleSetting.Builder()
            .name("vertical-speed")
            .description("Vertical speed in blocks per second.")
            .defaultValue(5)
            .min(0)
            .sliderMax(20)
            .build()
    );

    private final Setting<Double> fallSpeed = sgSpeeds.add(new DoubleSetting.Builder()
            .name("fall-speed")
            .description("How fast you fall in blocks per second.")
            .defaultValue(0.625)
            .min(0)
            .sliderMax(10)
            .build()
    );

    private Boat boat = null;

    public BoatPhase() {
        super(MeteorRejectsAddon.CATEGORY, "boat-phase", "Phase through blocks using a boat.");
    }

    @Override
    public void onActivate() {
        boat = null;
        if (Modules.get().isActive(BoatGlitch.class)) Modules.get().get(BoatGlitch.class).toggle();
    }

    @Override
    public void onDeactivate() {
        if (boat != null) {
            boat.noPhysics = false;
        }
    }

    @EventHandler
    private void onBoatMove(BoatMoveEvent event) {
        if (mc.player.getVehicle() != null && mc.player.getVehicle() instanceof Boat) {
            if (boat != mc.player.getVehicle()) {
                if (boat != null) {
                    boat.noPhysics = false;
                }
                boat = (Boat) mc.player.getVehicle();
            }
        } else boat = null;

        if (boat != null) {
            boat.noPhysics = true;
            //boat.pushSpeedReduction = 1;

            if (lockYaw.get()) {
                boat.setYRot(mc.player.getYRot());
            }

            Vec3 vel;

            if (adjustHorizontalSpeed.get()) {
                vel = PlayerUtils.getHorizontalVelocity(horizontalSpeed.get());
            }
            else {
                vel = boat.getDeltaMovement();
            }

            double velX = vel.x;
            double velY = 0;
            double velZ = vel.z;

            if (verticalControl.get()) {
                if (mc.options.keyJump.isDown()) velY += verticalSpeed.get() / 20;
                if (mc.options.keySprint.isDown()) velY -= verticalSpeed.get() / 20;
                else if (fall.get()) velY -= fallSpeed.get() / 20;
            } else if (fall.get()) velY -= fallSpeed.get() / 20;

            ((IVec3d) boat.getDeltaMovement()).meteor$set(velX,velY,velZ);
        }
    }
}
