package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.AnvilScreenHandler;

import java.util.List;

public class AutoRename extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Item>> items = sgGeneral.add(new ItemListSetting.Builder()
            .name("items")
            .description("Items you want to rename.")
            .defaultValue(List.of())
            .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
            .name("delay")
            .description("How many ticks to wait between actions.")
            .defaultValue(2)
            .min(0)
            .sliderMax(40)
            .build()
    );

    private final Setting<String> name = sgGeneral.add(new StringSetting.Builder()
            .name("name")
            .description("Name for an item, empty for reverting name to default.")
            .defaultValue("")
            .build()
    );

    private final Setting<Boolean> firstItemInContainer = sgGeneral.add(new BoolSetting.Builder()
            .name("first-item-in-container")
            .description("Will rename containers based on name of first item in it.")
            .defaultValue(false)
            .build()
    );

    private final Setting<List<Item>> containerItems = sgGeneral.add(new ItemListSetting.Builder()
            .name("container-items")
            .description("Items to treat as containers.")
            .defaultValue(List.of())
            .build()
    );

    public AutoRename() {
        super(MeteorRejectsAddon.CATEGORY, "auto-rename", "Automatically renames items.");
    }

    private int delayLeft = 0;
    @EventHandler
    private void onTick(TickEvent.Post ignoredEvent) {
        if (mc.interactionManager == null) return;
        if (items.get().isEmpty()) return;
        if (!(mc.player.currentScreenHandler instanceof AnvilScreenHandler)) return;

        if (delayLeft > 0) {
            delayLeft--;
            return;
        } else {
            delayLeft = delay.get();
        }

        var slot0 = mc.player.currentScreenHandler.getSlot(0);
        var slot1 = mc.player.currentScreenHandler.getSlot(1);
        var slot2 = mc.player.currentScreenHandler.getSlot(2);
        if (slot1.hasStack()) {
//            info("Slot 1 occupied");
            return; // touching anything
        }
        if (slot2.hasStack()) {
            if (mc.player.experienceLevel < 1) {
//                info("No exp");
            } else {
//                info("Extracting named");
                extractNamed();
            }
        } else {
            if (slot0.hasStack()) {
//                info("Renaming");
                renameItem(slot0.getStack());
            } else {
//                info("Populating");
                populateAnvil();
            }
        }
    }

    private void renameItem(ItemStack s) {
        var setname = "";
        if (firstItemInContainer.get() && containerItems.get().contains(s.getItem())) {
            setname = getFirstItemName(s);
        } else {
            setname = name.get();
        }
//        info("Renaming");
        if (mc.currentScreen == null || !(mc.currentScreen instanceof AnvilScreen)) {
            error("Not anvil screen");
            toggle();
            return;
        }
        var widgets = mc.currentScreen.children();
        var input = (TextFieldWidget)widgets.get(0);
        input.setText(setname);
    }

    private String getFirstItemName(ItemStack stack) {
        Item item = stack.getItem();
        if (!(item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof ShulkerBoxBlock)) {
            return "";
        }
        NbtCompound compound = stack.getNbt();
        if (compound == null) {
            return "";
        }
        compound = compound.getCompound("BlockEntityTag");
        if (compound == null) {
            return "";
        }
        var list = compound.getList("Items", NbtElement.COMPOUND_TYPE);
        if (list == null) {
            return "";
        }
        var minslot = Byte.MAX_VALUE;
        var name = "";
        for (int i = 0; i < list.size(); i++) {
            var invItem = list.getCompound(i);
            var invSlot = invItem.getByte("Slot");
            if (minslot < invSlot) {
                continue;
            }
            var itemId = invItem.getString("id");
            if (itemId == null) {
                continue;
            }
            name = ItemStack.fromNbt(invItem).getName().getString();
            minslot = invSlot;
        }
        return name;
    }

    private void extractNamed() {
        var to = -1;
        var inv = mc.player.currentScreenHandler;
        for (int i = 3; i < 38; i++) {
            var sl = inv.getSlot(i);
            if (sl.hasStack()) {
                to = i;
                break;
            }
        }
        if (to == -1) {
//            info("No output slot");
            return;
        }
        var from = 2;
//        info("Shift click %d %d", from, to);
        InvUtils.shiftClick().fromId(from).toId(to);
    }

    private void populateAnvil() {
        var gItems = items.get();
        var from = -1;
        var inv = mc.player.currentScreenHandler;
        for (int i = 3; i < 38; i++) {
            var sl = inv.getSlot(i);
            if (!sl.hasStack()) {
                continue;
            }
            var st = sl.getStack();
            if (gItems.contains(st.getItem()) && !st.hasCustomName()) {
                from = i;
                break;
            }
        }
        if (from == -1) {
//            info("Nothing to rename");
            return;
        }
        var to = 0;
//        info("Shift click %d %d", from, to);
        InvUtils.shiftClick().fromId(from).toId(to);
    }
}
