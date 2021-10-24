package anticope.rejects.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class WorldUtils {
    private static final ArrayList<BlockPos> blocks = new ArrayList<>();

    public static List<BlockPos> getSphere(BlockPos centerPos, int radius, int height) {
        blocks.clear();

        for (int i = centerPos.getX() - radius; i < centerPos.getX() + radius; i++) {
            for (int j = centerPos.getY() - height; j < centerPos.getY() + height; j++) {
                for (int k = centerPos.getZ() - radius; k < centerPos.getZ() + radius; k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    if (distanceBetween(centerPos, pos) <= radius && !blocks.contains(pos)) blocks.add(pos);
                }
            }
        }

        return blocks;
    }

    public static double distanceBetween(BlockPos pos1, BlockPos pos2) {
        double d = pos1.getX() - pos2.getX();
        double e = pos1.getY() - pos2.getY();
        double f = pos1.getZ() - pos2.getZ();
        return MathHelper.sqrt((float) (d * d + e * e + f * f));
    }
}
