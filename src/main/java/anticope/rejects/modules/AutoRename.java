package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import java.util.List;

public class AutoRename extends Module {
    public enum ContainerType {
        BOTH, BUNDLES, SHULKERS, NONE;

        @Override
        public String toString() {
            return switch (this) {
                case BOTH -> "Both";
                case BUNDLES -> "Bundles";
                case SHULKERS  -> "Shulkers";
                case NONE     -> "None";
            };
        }
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRename = settings.createGroup("Rename");
    private final SettingGroup sgLabelContainers = settings.createGroup("Label Containers");

    // General
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
            .name("delay")
            .description("How many ticks to wait between actions.")
            .defaultValue(2)
            .min(0)
            .sliderMax(40)
            .build()
    );

    // Rename group
    private final Setting<List<Item>> items = sgRename.add(new ItemListSetting.Builder()
            .name("items")
            .description("Items to rename.")
            .defaultValue(List.of())
            .build()
    );

    private final Setting<String> name = sgRename.add(new StringSetting.Builder()
            .name("name")
            .description("Name to apply to items. Leave blank to keep the default name.")
            .defaultValue("")
            .build()
    );

    // Label Containers group
    private final Setting<ContainerType> containerType = sgLabelContainers.add(new EnumSetting.Builder<ContainerType>()
            .name("container-type")
            .description("Which container types to rename based on the first item inside them.")
            .defaultValue(ContainerType.NONE)
            .build()
    );

    public AutoRename() {
        super(MeteorRejectsAddon.CATEGORY, "auto-rename", "Automatically renames items at an anvil. Can also label shulkers and bundles based on their contents.");
    }

    private int delayLeft = 0;

    @EventHandler
    private void onTick(TickEvent.Post ignoredEvent) {
        if (mc.gameMode == null) return;
        if (items.get().isEmpty() && containerType.get() == ContainerType.NONE) return;
        if (!(mc.player.containerMenu instanceof AnvilMenu)) return;

        if (delayLeft > 0) {
            delayLeft--;
            return;
        } else {
            delayLeft = delay.get();
        }

        var slot0 = mc.player.containerMenu.getSlot(0);
        var slot1 = mc.player.containerMenu.getSlot(1);
        var slot2 = mc.player.containerMenu.getSlot(2);
        if (slot1.hasItem()) {
            return; // second anvil slot occupied
        }
        if (slot2.hasItem()) {
            if (mc.player.experienceLevel >= 1) {
                extractNamed();
            }
        } else {
            if (slot0.hasItem()) {
                renameItem(slot0.getItem());
            } else {
                populateAnvil();
            }
        }
    }

    private boolean isContainerTarget(ItemStack st) {
        return switch (containerType.get()) {
            case SHULKERS -> st.has(DataComponents.CONTAINER);
            case BUNDLES  -> st.has(DataComponents.BUNDLE_CONTENTS);
            case BOTH     -> st.has(DataComponents.CONTAINER) || st.has(DataComponents.BUNDLE_CONTENTS);
            case NONE -> false;
        };
    }

    private void renameItem(ItemStack s) {
        String setname = isContainerTarget(s) ? getFirstItemName(s) : name.get();
        if (!(mc.screen instanceof AnvilScreen)) {
            error("Not anvil screen");
            toggle();
            return;
        }
        var input = (EditBox) mc.screen.children().get(0);
        input.setValue(setname);
    }

    private String getFirstItemName(ItemStack stack) {
        ItemContainerContents container = stack.get(DataComponents.CONTAINER);
        if (container != null) {
            for (ItemStack item : container.nonEmptyItems()) {
                return item.getHoverName().getString();
            }
        }

        BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundle != null) {
            for (ItemStack item : bundle.items()) {
                return item.getHoverName().getString();
            }
        }
        return "";
    }

    private void extractNamed() {
        var inv = mc.player.containerMenu;
        for (int i = 3; i < 38; i++) {
            if (inv.getSlot(i).hasItem()) {
                InvUtils.shiftClick().fromId(2).toId(i);
                return;
            }
        }
    }

    private void populateAnvil() {
        var inv = mc.player.containerMenu;
        for (int i = 3; i < 38; i++) {
            var sl = inv.getSlot(i);
            if (!sl.hasItem()) continue;
            var st = sl.getItem();
            boolean hasCustomName = st.getComponents().has(DataComponents.CUSTOM_NAME);
            boolean isRenameItem = items.get().contains(st.getItem()) &&
                    (name.get().isEmpty() ? hasCustomName : !hasCustomName);
            boolean isContainerItem = isContainerTarget(st) && !getFirstItemName(st).isEmpty();

            if (isRenameItem || (isContainerItem && !hasCustomName)) {
                InvUtils.shiftClick().fromId(i).toId(0);
                return;
            }
        }
    }
}
