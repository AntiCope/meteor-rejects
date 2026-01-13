package anticope.rejects.events;

import net.minecraft.world.item.ItemStack;

public class StopUsingItemEvent {
    private static final StopUsingItemEvent INSTANCE = new StopUsingItemEvent();

    public ItemStack itemStack;

    public static StopUsingItemEvent get(ItemStack itemStack) {
        INSTANCE.itemStack = itemStack;
        return INSTANCE;
    }
}
