package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.ScreenHandler;

import java.util.List;
import java.util.Objects;

public class AutoEnchant extends meteordevelopment.meteorclient.systems.modules.Module {

    public final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
            .name("delay")
            .description("The tick delay between enchanting items.")
            .defaultValue(50)
            .sliderMax(500)
            .min(0)
            .build()
    );

    private final Setting<Integer> level = sgGeneral.add(new IntSetting.Builder()
            .name("level")
            .description("Choose enchantment levels 1-3")
            .defaultValue(3)
            .max(3)
            .min(1)
            .build()
    );

    private final Setting<Boolean> drop = sgGeneral.add(new BoolSetting.Builder()
            .name("drop")
            .description("Automatically drops enchanted items (useful for when not enough inventory space)")
            .defaultValue(false)
            .build()
    );

    private final Setting<List<Item>> itemWhitelist = sgGeneral.add(new ItemListSetting.Builder()
            .name("item-whitelist")
            .description("Item that require enchantment.")
            .defaultValue()
            .filter(item -> item.equals(Items.BOOK) || item.isDamageable())
            .build()
    );

    public AutoEnchant() {
        super(MeteorRejectsAddon.CATEGORY, "auto-enchant ", "Automatically enchanting items.");
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (!(Objects.requireNonNull(mc.player).currentScreenHandler instanceof EnchantmentScreenHandler handler))
            return;
        MeteorExecutor.execute(this::autoEnchant);
    }

    private void autoEnchant() {
        if (!(Objects.requireNonNull(mc.player).currentScreenHandler instanceof EnchantmentScreenHandler handler))
            return;
        while (getEmptySlotCount(handler) > 2 || drop.get()) {
            if (!(mc.player.currentScreenHandler instanceof EnchantmentScreenHandler)) {
                info("Enchanting table is closed.");
                break;
            }
            if (handler.getLapisCount() < 3 && !fillLapisItem()) {
                info("Lapis lazuli is not found.");
                break;
            }
            if (!fillCanEnchantItem()) {
                info("No items found to enchant.");
                break;
            }

            // Why sleep here? I don't know either.
            try {
                Thread.sleep(delay.get());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (mc.player.experienceLevel < handler.enchantmentPower[level.get() - 1]) {
                info("You don't have enough experience levels");
                break;
            }
            if (handler.slots.get(0).hasStack()
                            && EnchantmentHelper.get(handler.slots.get(0).getStack()).isEmpty()
                            && handler.enchantmentPower[level.get() - 1] == 0) {
                info("You need to continue in a higher-level enchanting table");
                break;
            }
            Objects.requireNonNull(mc.interactionManager).clickButton(handler.syncId, level.get() - 1);
            if (getEmptySlotCount(handler) > 2) {
                InvUtils.shiftClick().slotId(0);
            } else if (drop.get() && handler.getSlot(0).hasStack()) {
                // I don't know why an exception LegacyRandomSource is thrown here,
                // so I used the main thread to drop items.
                mc.execute(() -> InvUtils.drop().slotId(0));
            }
        }
    }

    private boolean fillCanEnchantItem() {
        FindItemResult res = InvUtils.find(stack -> itemWhitelist.get().contains(stack.getItem()) && EnchantmentHelper.get(stack).isEmpty());
        if (res.slot() == -1) return false;
        InvUtils.shiftClick().slot(res.slot());
        return true;
    }

    private boolean fillLapisItem() {
        FindItemResult res = InvUtils.find(Items.LAPIS_LAZULI);
        if (res.slot() == -1) return false;
        InvUtils.shiftClick().slot(res.slot());
        return true;
    }

    private int getEmptySlotCount(ScreenHandler handler) {
        int emptySlotCount = 0;
        for (int i = 0; i < handler.slots.size(); i++) {
            if (!handler.slots.get(i).getStack().getItem().equals(Items.AIR))
                continue;
            emptySlotCount++;
        }
        return emptySlotCount;
    }

}
