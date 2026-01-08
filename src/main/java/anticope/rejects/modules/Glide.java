package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.events.OffGroundSpeedEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;


public class Glide extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Double> fallSpeed = sgGeneral.add(new DoubleSetting.Builder()
            .name("fall-speed")
            .description("Fall speed.")
            .defaultValue(0.125)
            .min(0.005)
            .sliderRange(0.005, 0.25)
            .build()
    );

    public final Setting<Double> moveSpeed = sgGeneral.add(new DoubleSetting.Builder()
            .name("move-speed")
            .description("Horizontal movement factor.")
            .defaultValue(1.2)
            .min(1)
            .sliderRange(1, 5)
            .build()
    );

    public final Setting<Double> minHeight = sgGeneral.add(new DoubleSetting.Builder()
            .name("min-height")
            .description("Won't glide when you are too close to the ground.")
            .defaultValue(0)
            .min(0)
            .sliderRange(0, 2)
            .build()
    );

    public Glide() {
        super(MeteorRejectsAddon.CATEGORY, "glide", "Yeehaw!");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        LocalPlayer player = mc.player;
        Vec3 v = player.getDeltaMovement();

        if (player.onGround() || player.isInWater() || player.isInLava() || player.onClimbable() || v.y >= 0)
            return;

        if (minHeight.get() > 0) {
            AABB box = player.getBoundingBox();
            box = box.minmax(box.move(0, -minHeight.get(), 0));
            if (!mc.level.noCollision(box)) return;
        }

        player.setDeltaMovement(v.x, Math.max(v.y, -fallSpeed.get()), v.z);
    }

    @EventHandler
    private void onOffGroundSpeed(OffGroundSpeedEvent event) {
        event.speed *= moveSpeed.get().floatValue();
    }
}