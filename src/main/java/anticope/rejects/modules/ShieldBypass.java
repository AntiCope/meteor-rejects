package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
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
        if (event.entity instanceof LivingEntity e && e.getMainHandStack().getItem() == Items.SHIELD && e.isBlocking()) {
            if (originalPos != null) return;

            originalPos = mc.player.getPos();

            Vec3d targetPos = e.getPos();

            Vec3d vec3d3 = originalPos.relativize(targetPos).normalize();

            if (new Vec3d(vec3d3.x, 0.0d, vec3d3.z).dotProduct(e.getRotationVec(1.0f)) >= 0.0d) return;

            Vec3d tp = Vec3d.fromPolar(0, mc.player.getYaw()).normalize().multiply(mc.player.distanceTo(e));
            mc.player.setPosition(targetPos.x + tp.x, targetPos.y, targetPos.z + tp.z);
            if (rotate.get()) Rotations.rotate(-mc.player.getYaw(), mc.player.getPitch());
            target = e;
            event.cancel();
        }
    }
}
