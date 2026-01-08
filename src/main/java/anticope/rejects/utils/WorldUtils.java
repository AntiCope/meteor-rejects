package anticope.rejects.utils;

import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

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
        return Mth.sqrt((float) (d * d + e * e + f * f));
    }

    public static boolean interact(BlockPos pos, FindItemResult findItemResult, boolean rotate) {
        if (!findItemResult.found()) return false;
        Runnable action = () -> {
            boolean wasSneaking = mc.player.isShiftKeyDown();
            mc.player.setShiftKeyDown(false);
            InvUtils.swap(findItemResult.slot(), true);
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
            mc.player.swing(InteractionHand.MAIN_HAND);
            InvUtils.swapBack();
            mc.player.setShiftKeyDown(wasSneaking);
        };
        if (rotate) Rotations.rotate(Rotations.getYaw(pos), Rotations.getPitch(pos), -100, action);
        else action.run();
        return true;
    }
}
