package anticope.rejects.utils;

import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class TntDamage {
    public static int calculate(BlockPos bp) {
        if (!Utils.canUpdate()) return 0;
        int score = 0;
        
        for(int j = -5; j <= 5; ++j) {
            for(int k = -5; k <= 5; ++k) {
                for(int l = -5; l <= 5; ++l) {
                    BlockPos blockPos = new BlockPos(j, k, l);
                    BlockState blockState = Utils.mc.world.getBlockState(blockPos);
                    FluidState fluidState = Utils.mc.world.getFluidState(blockPos);
                    
                    float h = 2.8F;
                    Optional<Float> optional = blockState.isAir() && fluidState.isEmpty() ? Optional.empty() : Optional.of(Math.max(blockState.getBlock().getBlastResistance(), fluidState.getBlastResistance()));;
                    if (optional.isPresent()) {
                        h -= (optional.get() + 0.3F) * 0.3F;
                    }
                
                    if (h > 0.0F) {
                        score++;
                    }
                }
            }
        }
        
        return score;
    }
}
