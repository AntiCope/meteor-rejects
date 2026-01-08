package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import java.util.Arrays;
import java.util.List;

public class AutoCraft extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Item>> items = sgGeneral.add(new ItemListSetting.Builder()
        .name("items")
        .description("Items you want to get crafted.")
        .defaultValue(List.of())
        .build()
    );

    private final Setting<Boolean> antiDesync = sgGeneral.add(new BoolSetting.Builder()
            .name("anti-desync")
            .description("Try to prevent inventory desync.")
            .defaultValue(false)
            .build()
    );
    
    private final Setting<Boolean> craftAll = sgGeneral.add(new BoolSetting.Builder()
            .name("craft-all")
            .description("Crafts maximum possible amount amount per craft (shift-clicking)")
            .defaultValue(false)
            .build()
    );
    
    private final Setting<Boolean> drop = sgGeneral.add(new BoolSetting.Builder()
            .name("drop")
            .description("Automatically drops crafted items (useful for when not enough inventory space)")
            .defaultValue(false)
            .build()
    );

    public AutoCraft() {
        super(MeteorRejectsAddon.CATEGORY, "auto-craft", "Automatically crafts items.");
    }
    
    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!Utils.canUpdate() || mc.gameMode == null) return;

        if (items.get().isEmpty()) return;

        if (!(mc.player.containerMenu instanceof CraftingMenu)) return;

        if (antiDesync.get()) 
            mc.player.getInventory().tick();

        // Danke schön GhostTypes
        // https://github.com/GhostTypes/orion/blob/main/src/main/java/me/ghosttypes/orion/modules/main/AutoBedCraft.java
        CraftingMenu currentScreenHandler = (CraftingMenu) mc.player.containerMenu;
        List<Item> itemList = items.get();
        List<RecipeCollection> recipeResultCollectionList  = mc.player.getRecipeBook().getCollections();
        for (RecipeCollection recipeResultCollection : recipeResultCollectionList) {
            // Get craftable recipes only
            List<RecipeDisplayEntry> craftRecipes = recipeResultCollection.getSelectedRecipes(RecipeCollection.CraftableStatus.CRAFTABLE);
            for (RecipeDisplayEntry recipe : craftRecipes) {
                RecipeDisplay recipeDisplay = recipe.display();
                List<ItemStack> resultStacks = recipeDisplay.result().resolveForStacks(SlotDisplayContext.fromLevel(mc.level));
                for (ItemStack resultStack : resultStacks) {
                    // Check if the result item is in the item list
                    if (!itemList.contains(resultStack.getItem())) continue;

                    mc.gameMode.handlePlaceRecipe(currentScreenHandler.containerId, recipe.id(), craftAll.get());
                    mc.gameMode.handleInventoryMouseClick(currentScreenHandler.containerId, 0, 1,
                            drop.get() ? ClickType.THROW : ClickType.QUICK_MOVE, mc.player);
                }
            }
        }
    }
}
