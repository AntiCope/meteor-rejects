package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ShieldBypass extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
            .name("rotate")
            .description("Rotate towards enemy. Disable if killaura enabled.")
            .defaultValue(true)
            .build()
    );

    public ShieldBypass() {
        super(MeteorRejectsAddon.CATEGORY, "shield-bypass", "Attempts to teleport you behind enemies to bypass shields.");
    }

    private Vec3d originalPos;
    private Entity target;

    @Override
    public void onDeactivate() {
        originalPos = null;
        target = null;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (originalPos != null && target != null) {
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.setPosition(originalPos);
            if (rotate.get()) Rotations.rotate(-mc.player.getYaw(), mc.player.getPitch());
        }
        originalPos = null;
        target = null;
    }

    @EventHandler
    private void onAttackEntity(AttackEntityEvent event) {
        if (event.entity instanceof LivingEntity e && e.isBlocking()) {
            if (originalPos != null) return;

            Vec3d originalPos = mc.player.getPos();

            // Shield check
            Vec3d vec3d3 = originalPos.relativize(e.getPos()).normalize();
            if (new Vec3d(vec3d3.x, 0.0d, vec3d3.z).dotProduct(e.getRotationVec(1.0f)) >= 0.0d) return;

            double range = mc.player.distanceTo(e);
            while (range > 0.5) {
                Vec3d tp = Vec3d.fromPolar(0, mc.player.getYaw()).normalize().multiply(range);
                Vec3d newPos = tp.add(e.getPos());
                BlockPos pos = new BlockPos(newPos);
                for (int i = -2; i <= 2; i++) {
                    if (mc.player.world.getBlockState(pos.up(i)).isAir() && mc.player.world.getBlockState(pos).isAir()) {
                        this.originalPos = originalPos;
                        if (rotate.get()) Rotations.rotate(-mc.player.getYaw(), mc.player.getPitch());
                        target = e;
                        event.cancel();
                        mc.player.setPosition(newPos.add(0, i, 0));
                        return;
                    }
                }
                range--;
            }
        }
    }
}
