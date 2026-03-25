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
        .description("Trash list.")
        .defaultValue(List.of())
        .build()
    );

    private final Setting<Boolean> hotbar = sgGeneral.add(new BoolSetting.Builder()
        .name("include-hotbar")
        .description("Toss from hotbar too.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Ticks between drops.")
        .defaultValue(1)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private int ticks;

    public AutoDrop() {
        super(MeteorRejectsAddon.CATEGORY, "auto-drop", "Dumps trash.");
    }

    @Override
    public void onActivate() {
        ticks = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;

        if (ticks > 0) {
            ticks--;
            return;
        }

        int start = hotbar.get() ? 0 : 9;

        for (int i = start; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            
            if (stack.isEmpty() || !items.get().contains(stack.getItem())) continue;

            // Map inv to screen handler IDs
            int slot = i < 9 ? i + 36 : i;

            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, slot, 1, SlotActionType.THROW, mc.player);
            
            ticks = delay.get();
            return; // drop one per cycle
        }
    }
}
