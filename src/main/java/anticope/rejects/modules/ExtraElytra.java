package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ExtraElytra extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> instantFly = sgGeneral.add(new BoolSetting.Builder()
            .name("instant-fly")
            .description("Jump to fly, no weird double-jump needed!")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> speedCtrl = sgGeneral.add(new BoolSetting.Builder()
            .name("speed-ctrl")
            .description("""
                    Control your speed with the Forward and Back keys.
                    (default: W and S)
                    No fireworks needed!""")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> heightCtrl = sgGeneral.add(new BoolSetting.Builder()
            .name("height-ctrl")
            .description("""
                    Control your height with the Jump and Sneak keys.
                    (default: Spacebar and Shift)
                    No fireworks needed!""")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> stopInWater = sgGeneral.add(new BoolSetting.Builder()
            .name("stop-in-water")
            .description("Stop flying in water")
            .defaultValue(true)
            .build()
    );

    private int jumpTimer;

    @Override
    public void onActivate() {
        jumpTimer = 0;
    }

    public ExtraElytra() {
        super(MeteorRejectsAddon.CATEGORY, "extra-elytra", "Easier elytra");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (jumpTimer > 0)
            jumpTimer--;

        ItemStack chest = mc.player.getEquippedStack(EquipmentSlot.CHEST);
        if (chest.getItem() != Items.ELYTRA)
            return;

        if (mc.player.isGliding()) {
            if (stopInWater.get() && mc.player.isTouchingWater()) {
                sendStartStopPacket();
                return;
            }
            controlSpeed();
            controlHeight();
            return;
        }

        if (chest.getDamage() < chest.getMaxDamage() - 1 && mc.options.jumpKey.isPressed())
            doInstantFly();
    }

    private void sendStartStopPacket() {
        ClientCommandC2SPacket packet = new ClientCommandC2SPacket(mc.player,
                ClientCommandC2SPacket.Mode.START_FALL_FLYING);
        mc.player.networkHandler.sendPacket(packet);
    }

    private void controlHeight() {
        if (!heightCtrl.get())
            return;

        Vec3d v = mc.player.getVelocity();

        if (mc.options.jumpKey.isPressed())
            mc.player.setVelocity(v.x, v.y + 0.08, v.z);
        else if (mc.options.sneakKey.isPressed())
            mc.player.setVelocity(v.x, v.y - 0.04, v.z);
    }

    private void controlSpeed() {
        if (!speedCtrl.get())
            return;

        float yaw = (float) Math.toRadians(mc.player.getYaw());
        Vec3d forward = new Vec3d(-MathHelper.sin(yaw) * 0.05, 0,
                MathHelper.cos(yaw) * 0.05);

        Vec3d v = mc.player.getVelocity();

        if (mc.options.forwardKey.isPressed())
            mc.player.setVelocity(v.add(forward));
        else if (mc.options.backKey.isPressed())
            mc.player.setVelocity(v.subtract(forward));
    }

    private void doInstantFly() {
        if (!instantFly.get())
            return;

        if (jumpTimer <= 0) {
            jumpTimer = 20;
            mc.player.setJumping(false);
            mc.player.setSprinting(true);
            mc.player.jump();
        }

        sendStartStopPacket();
    }
}
