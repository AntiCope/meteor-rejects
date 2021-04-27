package cloudburst.rejects.mixin;

import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.inventory.SimpleInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HorseBaseEntity.class)
public interface HorseBaseEntityAccessor {
    @Accessor("items")
    SimpleInventory getItems();
}
