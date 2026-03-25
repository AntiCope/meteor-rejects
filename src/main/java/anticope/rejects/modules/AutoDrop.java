package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

import java.util.List;

public class AutoDrop extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Item>> items = sgGeneral.add(new ItemListSetting.Builder()
        .name("items")
        .description("Items to automatically drop from your inventory.")
        .defaultValue(List.of())
        .build()
    );

    private final Setting<Boolean> dropHotbar = sgGeneral.add(new BoolSetting.Builder()
        .name("drop-hotbar")
        .description("Also drop matching items from your hotbar.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay in ticks between each drop check.")
        .defaultValue(10)
        .min(1)
        .sliderMax(40)
        .build()
    );

    private int timer = 0;

    public AutoDrop() {
        super(MeteorRejectsAddon.CATEGORY, "auto-drop", "Automatically drops selected items from your inventory.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.interactionManager == null) return;

        timer++;
        if (timer < delay.get()) return;
        timer = 0;

        int startSlot = dropHotbar.get() ? 0 : 9;

        for (int i = startSlot; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (!items.get().contains(stack.getItem())) continue;

            mc.interactionManager.clickSlot(
                mc.player.playerScreenHandler.syncId,
                i < 9 ? i + 36 : i, // hotbar slots are offset in screen handler
                1,
                SlotActionType.THROW,
                mc.player
            );
        }
    }
}
