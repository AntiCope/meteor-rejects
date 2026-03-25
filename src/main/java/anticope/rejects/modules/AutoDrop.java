package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

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

    @Override
    public void onActivate() {
        timer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;

        if (timer > 0) {
            timer--;
            return;
        }
        timer = delay.get();

        int startSlot = dropHotbar.get() ? 0 : 9;

        for (int i = startSlot; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !items.get().contains(stack.getItem())) continue;

            InvUtils.drop().slot(i);
            return;
        }
    }
}
