package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.Cancellable;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class ShieldBypass extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> ignoreAxe = sgGeneral.add(new BoolSetting.Builder()
            .name("ignore-axe")
            .description("Ignore if you are holding an axe.")
            .defaultValue(true)
            .build()
    );

    public ShieldBypass() {
        super(MeteorRejectsAddon.CATEGORY, "shield-bypass", "Attempts to teleport you behind enemies to bypass shields.");
    }

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if (Modules.get().isActive(KillAura.class)) return;
        if (mc.currentScreen == null && !mc.player.isUsingItem() && event.action == KeyAction.Press && event.button == GLFW_MOUSE_BUTTON_LEFT) {
            if (mc.crosshairTarget instanceof EntityHitResult result) {
                bypass(result.getEntity(), event);
            }
        }
    }

    private boolean isBlocked(Vec3d pos, LivingEntity target) {
        Vec3d vec3d3 = pos.relativize(target.getPos()).normalize();
        return new Vec3d(vec3d3.x, 0.0d, vec3d3.z).dotProduct(target.getRotationVec(1.0f)) >= 0.0d;
    }

    public void bypass(Entity target, Cancellable event) {
        if (target instanceof LivingEntity e && e.isBlocking()) {
            if (ignoreAxe.get() && InvUtils.testInMainHand(i -> i.getItem() instanceof AxeItem)) return;

            // Shield check
            if (isBlocked(mc.player.getPos(), e)) return;

            Vec3d offset = Vec3d.fromPolar(0, mc.player.getYaw()).normalize().multiply(2);
            Vec3d newPos = e.getPos().add(offset);

            // Move up to prevent tping into blocks
            boolean inside = false;
            for (float i = 0; i < 4; i += 0.25) {
                Vec3d targetPos = newPos.add(0, i, 0);

                boolean collides = !mc.world.isSpaceEmpty(null, e.getBoundingBox().offset(offset).offset(targetPos.subtract(newPos)));

                if (!inside && collides) {
                    inside = true;
                } else if (inside && !collides) {
                    newPos = targetPos;
                    break;
                }
            }

            if (!isBlocked(newPos, e)) return;

            event.cancel();

            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(newPos.getX(), newPos.getY(), newPos.getZ(), true));

            mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(e, mc.player.isSneaking()));
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(mc.player.getActiveHand()));
            mc.player.resetLastAttackedTicks();

            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true));
        }
    }
}
