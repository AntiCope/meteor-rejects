package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.Cancellable;
import meteordevelopment.meteorclient.events.meteor.MouseClickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

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
    private void onMouseButton(MouseClickEvent event) {
        if (Modules.get().isActive(KillAura.class)) return;
        if (mc.screen == null && !mc.player.isUsingItem() && event.action == KeyAction.Press && event.button() == GLFW_MOUSE_BUTTON_LEFT) {
            if (mc.hitResult instanceof EntityHitResult result) {
                bypass(result.getEntity(), event);
            }
        }
    }

    private boolean isBlocked(Vec3 pos, LivingEntity target) {
        Vec3 targetPos = new Vec3(target.getX(), target.getY(), target.getZ());
        Vec3 vec3d3 = pos.vectorTo(targetPos).normalize();
        return new Vec3(vec3d3.x, 0.0d, vec3d3.z).dot(target.getViewVector(1.0f)) >= 0.0d;
    }

    public void bypass(Entity target, Cancellable event) {
        if (target instanceof LivingEntity e && e.isBlocking()) {
            if (ignoreAxe.get() && InvUtils.testInMainHand(i -> i.getItem() instanceof AxeItem)) return;

            // Shield check
            Vec3 playerPos = new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ());
            if (isBlocked(playerPos, e)) return;

            Vec3 offset = Vec3.directionFromRotation(0, mc.player.getYRot()).normalize().scale(2);
            Vec3 ePos = new Vec3(e.getX(), e.getY(), e.getZ());
            Vec3 newPos = ePos.add(offset);

            // Move up to prevent tping into blocks
            boolean inside = false;
            for (float i = 0; i < 4; i += 0.25) {
                Vec3 targetPos = newPos.add(0, i, 0);

                boolean collides = !mc.level.noCollision(null, e.getBoundingBox().move(offset).move(targetPos.subtract(newPos)));

                if (!inside && collides) {
                    inside = true;
                } else if (inside && !collides) {
                    newPos = targetPos;
                    break;
                }
            }

            if (!isBlocked(newPos, e)) return;

            event.cancel();

            mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(newPos.x(), newPos.y(), newPos.z(), true, false));

            mc.getConnection().send(ServerboundInteractPacket.createAttackPacket(e, mc.player.isShiftKeyDown()));
            mc.getConnection().send(new ServerboundSwingPacket(mc.player.getUsedItemHand()));
            mc.player.resetOnlyAttackStrengthTicker();

            mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true, mc.player.horizontalCollision));
        }
    }
}
