package anticope.rejects.utils;

import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

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

    public static void interact(BlockPos pos, FindItemResult findItemResult, boolean rotate) {
        Runnable action = () -> {
            boolean wasSneaking = mc.player.input.sneaking;
            mc.player.input.sneaking = false;
            InvUtils.swap(findItemResult.slot(), true);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(Utils.vec3d(pos), Direction.UP, pos, false));
            mc.player.swingHand(Hand.MAIN_HAND);
            InvUtils.swapBack();
            mc.player.input.sneaking = wasSneaking;
        };
        if (rotate) Rotations.rotate(Rotations.getYaw(pos), Rotations.getPitch(pos), -100, action);
        else action.run();
    }
}
