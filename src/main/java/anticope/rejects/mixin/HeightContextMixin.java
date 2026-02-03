package anticope.rejects.mixin;

import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldGenerationContext.class)
public abstract class HeightContextMixin {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkGenerator;getMinY()I"))
    private int onMinY(ChunkGenerator instance) {
        return instance == null ? -9999999 : instance.getMinY();
    }


    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkGenerator;getGenDepth()I"))
    private int onHeight(ChunkGenerator instance) {
        return instance == null ? 100000000 : instance.getGenDepth();
    }
}
