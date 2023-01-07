package anticope.rejects.utils;

import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class WorldUtils {
    public static List<BlockPos> getSphere(BlockPos centerPos, int radius, int height) {
        ArrayList<BlockPos> blocks = new ArrayList<>();

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

    public static ArrayList<BlockPos> getCube(double range) {
        Vec3d eyesVec = mc.player.getEyePos();
        int rangeI = (int) Math.ceil(range);

        BlockPos center = new BlockPos(eyesVec);
        BlockPos min = center.add(-rangeI, -rangeI, -rangeI);
        BlockPos max = center.add(rangeI, rangeI, rangeI);

        return getAllInBox(min, max).stream()
                .filter(pos -> eyesVec.distanceTo(Vec3d.ofCenter(pos)) <= range)
                .sorted(Comparator.comparingDouble(pos -> eyesVec.squaredDistanceTo(Vec3d.ofCenter(pos))))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static List<BlockPos> getAllInBox(BlockPos from, BlockPos to) {
        List<BlockPos> blocks = new ArrayList<>();

        BlockPos min = new BlockPos(Math.min(from.getX(), to.getX()),
                Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
        BlockPos max = new BlockPos(Math.max(from.getX(), to.getX()),
                Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));

        for (int x = min.getX(); x <= max.getX(); x++)
            for (int y = min.getY(); y <= max.getY(); y++)
                for (int z = min.getZ(); z <= max.getZ(); z++)
                    blocks.add(new BlockPos(x, y, z));

        return blocks;
    }

    public static void interact(BlockPos pos, FindItemResult findItemResult, boolean rotate) {
        Runnable action = () -> {
            boolean wasSneaking = mc.player.input.sneaking;
            mc.player.input.sneaking = false;
            InvUtils.swap(findItemResult.slot(), true);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false));
            mc.player.swingHand(Hand.MAIN_HAND);
            InvUtils.swapBack();
            mc.player.input.sneaking = wasSneaking;
        };
        if (rotate) Rotations.rotate(Rotations.getYaw(pos), Rotations.getPitch(pos), -100, action);
        else action.run();
    }
}
