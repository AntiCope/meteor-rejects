package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

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

        ItemStack chest = mc.player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() != Items.ELYTRA)
            return;

        if (mc.player.isFallFlying()) {
            if (stopInWater.get() && mc.player.isInWater()) {
                sendStartStopPacket();
                return;
            }
            controlSpeed();
            controlHeight();
            return;
        }

        if (chest.getDamageValue() < chest.getMaxDamage() - 1 && mc.options.keyJump.isDown())
            doInstantFly();
    }

    private void sendStartStopPacket() {
        ServerboundPlayerCommandPacket packet = new ServerboundPlayerCommandPacket(mc.player,
                ServerboundPlayerCommandPacket.Action.START_FALL_FLYING);
        mc.player.connection.send(packet);
    }

    private void controlHeight() {
        if (!heightCtrl.get())
            return;

        Vec3 v = mc.player.getDeltaMovement();

        if (mc.options.keyJump.isDown())
            mc.player.setDeltaMovement(v.x, v.y + 0.08, v.z);
        else if (mc.options.keyShift.isDown())
            mc.player.setDeltaMovement(v.x, v.y - 0.04, v.z);
    }

    private void controlSpeed() {
        if (!speedCtrl.get())
            return;

        float yaw = (float) Math.toRadians(mc.player.getYRot());
        Vec3 forward = new Vec3(-Mth.sin(yaw) * 0.05, 0,
                Mth.cos(yaw) * 0.05);

        Vec3 v = mc.player.getDeltaMovement();

        if (mc.options.keyUp.isDown())
            mc.player.setDeltaMovement(v.add(forward));
        else if (mc.options.keyDown.isDown())
            mc.player.setDeltaMovement(v.subtract(forward));
    }

    private void doInstantFly() {
        if (!instantFly.get())
            return;

        if (jumpTimer <= 0) {
            jumpTimer = 20;
            mc.player.setJumping(false);
            mc.player.setSprinting(true);
            mc.player.jumpFromGround();
        }

        sendStartStopPacket();
    }
}
