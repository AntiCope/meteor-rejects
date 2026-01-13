package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import java.util.List;
import java.util.Set;

public class AutoGrind extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
            .name("delay")
            .description("The tick delay between grinding items.")
            .defaultValue(50)
            .sliderMax(500)
            .min(0)
            .build()
    );

    private final Setting<List<Item>> itemBlacklist = sgGeneral.add(new ItemListSetting.Builder()
            .name("item-blacklist")
            .description("Items that should be ignored.")
            .defaultValue()
            .filter(Item -> Item.components().get(DataComponents.DAMAGE) != null)
            .build()
    );

    private final Setting<Set<ResourceKey<Enchantment>>> enchantmentBlacklist = sgGeneral.add(new EnchantmentListSetting.Builder()
            .name("enchantment-blacklist")
            .description("Enchantments that should be ignored.")
            .defaultValue()
            .build()
    );

    public AutoGrind() {
        super(MeteorRejectsAddon.CATEGORY, "auto-grind", "Automatically disenchants items.");
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (!(mc.player.containerMenu instanceof GrindstoneMenu))
            return;

        MeteorExecutor.execute(() -> {
            for (int i = 0; i <= mc.player.getInventory().getContainerSize(); i++) {
                if (canGrind(mc.player.getInventory().getItem(i))) {
                    try {
                        Thread.sleep(delay.get());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (mc.screen == null) break;

                    InvUtils.shiftClick().slot(i);
                    InvUtils.move().fromId(2).to(i);
                }
            }
        });
    }

    private boolean canGrind(ItemStack stack) {
        if (itemBlacklist.get().contains(stack.getItem())) return false;

        ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
        int availEnchs = 0;

        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            availEnchs++;
            if (EnchantmentHelper.hasTag(stack, EnchantmentTags.CURSE))
                availEnchs--;
            if (enchantmentBlacklist.get().contains(enchantment.value()))
                return false;
        }

        return !enchantments.isEmpty() && availEnchs > 0;
    }
}
