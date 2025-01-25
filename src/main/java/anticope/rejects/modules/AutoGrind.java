package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.screen.GrindstoneScreenHandler;

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
            .filter(Item -> Item.getComponents().get(DataComponentTypes.DAMAGE) != null)
            .build()
    );

    private final Setting<Set<RegistryKey<Enchantment>>> enchantmentBlacklist = sgGeneral.add(new EnchantmentListSetting.Builder()
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
        if (!(mc.player.currentScreenHandler instanceof GrindstoneScreenHandler))
            return;

        MeteorExecutor.execute(() -> {
            for (int i = 0; i <= mc.player.getInventory().size(); i++) {
                if (canGrind(mc.player.getInventory().getStack(i))) {
                    try {
                        Thread.sleep(delay.get());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (mc.currentScreen == null) break;

                    InvUtils.shiftClick().slot(i);
                    InvUtils.move().fromId(2).to(i);
                }
            }
        });
    }

    private boolean canGrind(ItemStack stack) {
        if (itemBlacklist.get().contains(stack.getItem())) return false;

        ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(stack);
        int availEnchs = 0;

        for (RegistryEntry<Enchantment> enchantment : enchantments.getEnchantments()) {
            availEnchs++;
            if (EnchantmentHelper.hasAnyEnchantmentsIn(stack, EnchantmentTags.CURSE))
                availEnchs--;
            if (enchantmentBlacklist.get().contains(enchantment.value()))
                return false;
        }

        return !enchantments.isEmpty() && availEnchs > 0;
    }
}
