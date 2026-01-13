package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import java.util.List;

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
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack == null || stack.getItem() != Items.BOWL || i == 9)
                continue;

            // check if empty bowl slot contains a non-bowl item
            ItemStack emptyBowlStack = mc.player.getInventory().getItem(9);
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
                oldSlot = mc.player.getInventory().getSelectedSlot();

            // set slot
            mc.player.getInventory().setSelectedSlot(soupInHotbar);

            // eat soup
            mc.options.keyUse.setDown(true);
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);

            return;
        }

        stopIfEating();

        // search soup in inventory
        int soupInInventory = findSoup(9, 36);

        // move soup in inventory to hotbar
        if (soupInInventory != -1)
            InvUtils.quickSwap().slot(soupInInventory);
    }

    private int findSoup(int startSlot, int endSlot) {
        List<Item> stews = List.of(Items.RABBIT_STEW, Items.MUSHROOM_STEW, Items.BEETROOT_SOUP);

        for (int i = startSlot; i < endSlot; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);

            if (stack != null && stews.contains(stack.getItem()))
                return i;
        }

        return -1;
    }

    private boolean shouldEatSoup() {
        // check health
        if (mc.player.getHealth() > health.get() * 2F)
            return false;

        // check for clickable objects
        return !isClickable(mc.hitResult);
    }

    private boolean isClickable(HitResult hitResult) {
        switch (hitResult) {
            case null -> {
                return false;
            }
            case EntityHitResult entityHitResult -> {
                Entity entity = ((EntityHitResult) mc.hitResult).getEntity();
                return entity instanceof Villager
                        || entity instanceof TamableAnimal;
            }
            case BlockHitResult blockHitResult -> {
                BlockPos pos = ((BlockHitResult) mc.hitResult).getBlockPos();
                if (pos == null)
                    return false;

                Block block = mc.level.getBlockState(pos).getBlock();
                return block instanceof BaseEntityBlock
                        || block instanceof CraftingTableBlock;
            }
            default -> {
            }
        }

        return false;
    }

    private void stopIfEating() {
        // check if eating
        if (oldSlot == -1)
            return;

        // stop eating
        mc.options.keyUse.setDown(false);

        // reset slot
        mc.player.getInventory().setSelectedSlot(oldSlot);
        oldSlot = -1;
    }

}
