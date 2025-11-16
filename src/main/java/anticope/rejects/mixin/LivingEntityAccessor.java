package anticope.rejects.mixin;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("jumpingCooldown")
    void setJumpingCooldown(int cooldown);
    
    @Accessor("jumpingCooldown")
    int getJumpingCooldown();
}

