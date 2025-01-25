package anticope.rejects.mixin;

import net.minecraft.world.gen.heightprovider.HeightProvider;
import net.minecraft.world.gen.placementmodifier.HeightRangePlacementModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HeightRangePlacementModifier.class)
public interface HeightRangePlacementModifierAccessor {

    @Accessor
    HeightProvider getHeight();
}
