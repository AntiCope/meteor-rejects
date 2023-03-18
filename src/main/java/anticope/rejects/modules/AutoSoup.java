package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.StewItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class AutoSoup extends Module {
    private static final String desc = "Automatically eats soup when your health is low on some servers.";

    public AutoSoup() {
        super(MeteorRejectsAddon.CATEGORY, "auto-soup", desc);
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Double> health = sgGeneral.add(new DoubleSetting.Builder()
            .name("health")
            .description("Eats a soup when your health reaches this value or falls below it.")
            .defaultValue(6.5)
            .min(0.5)
            .sliderMin(0.5)
            .sliderMax(9.5)
            .build()
    );

    private int oldSlot = -1;

    @Override
    public void onDeactivate() {
        stopIfEating();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        // sort empty bowls
        for (int i = 0; i < 36; i++) {
            // filter out non-bowl items and empty bowl slot
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack == null || stack.getItem() != Items.BOWL || i == 9)
                continue;

            // check if empty bowl slot contains a non-bowl item
            ItemStack emptyBowlStack = mc.player.getInventory().getStack(9);
            boolean swap = !emptyBowlStack.isEmpty()
                    && emptyBowlStack.getItem() != Items.BOWL;

            // place bowl in empty bowl slot
            InvUtils.click().slot(i < 9 ? 36 + i : i);
            InvUtils.click().slot(9);

            // place non-bowl item from empty bowl slot in current slot
            if (swap)
                InvUtils.click().slot(i < 9 ? 36 + i : i);
        }

        // search soup in hotbar
        int soupInHotbar = findSoup(0, 9);

        // check if any soup was found
        if (soupInHotbar != -1) {
            // check if player should eat soup
            if (!shouldEatSoup()) {
                stopIfEating();
                return;
            }

            // save old slot
            if (oldSlot == -1)
                oldSlot = mc.player.getInventory().selectedSlot;

            // set slot
            mc.player.getInventory().selectedSlot = soupInHotbar;

            // eat soup
            mc.options.useKey.setPressed(true);
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);

            return;
        }

        stopIfEating();

        // search soup in inventory
        int soupInInventory = findSoup(9, 36);

        // move soup in inventory to hotbar
        if (soupInInventory != -1)
            InvUtils.quickMove().slot(soupInInventory);
    }

    private int findSoup(int startSlot, int endSlot) {
        for (int i = startSlot; i < endSlot; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);

            if (stack != null && stack.getItem() instanceof StewItem)
                return i;
        }

        return -1;
    }

    private boolean shouldEatSoup() {
        // check health
        if (mc.player.getHealth() > health.get() * 2F)
            return false;

        // check for clickable objects
        return !isClickable(mc.crosshairTarget);
    }

    private boolean isClickable(HitResult hitResult) {
        if (hitResult == null)
            return false;

        if (hitResult instanceof EntityHitResult) {
            Entity entity = ((EntityHitResult) mc.crosshairTarget).getEntity();
            return entity instanceof VillagerEntity
                    || entity instanceof TameableEntity;
        }

        if (hitResult instanceof BlockHitResult) {
            BlockPos pos = ((BlockHitResult) mc.crosshairTarget).getBlockPos();
            if (pos == null)
                return false;

            Block block = mc.world.getBlockState(pos).getBlock();
            return block instanceof BlockWithEntity
                    || block instanceof CraftingTableBlock;
        }

        return false;
    }

    private void stopIfEating() {
        // check if eating
        if (oldSlot == -1)
            return;

        // stop eating
        mc.options.useKey.setPressed(false);

        // reset slot
        mc.player.getInventory().selectedSlot = oldSlot;
        oldSlot = -1;
    }

}
