package cloudburst.rejects.mixin;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;


@Mixin(Entity.class)
public interface EntityAccessor {
    @Invoker("setFlag")
    void invokeSetFlag(int index, boolean value);
}
