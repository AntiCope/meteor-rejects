package anticope.rejects.modules;

import net.minecraft.block.Block;
import net.minecraft.block.CropBlock;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;

public class BonemealAura extends Module {
    public BonemealAura() {
        super(MeteorRejectsAddon.CATEGORY, "bonemeal-aura", "Automatically bonemeal crops around the player");
    }

    public boolean isBonemealing;

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        BlockPos crop = getCrop();
        if (crop == null) {
            isBonemealing = false;
            return;
        }

        FindItemResult bonemeal = InvUtils.findInHotbar(Items.BONE_MEAL);
        if (!bonemeal.found()) {
            isBonemealing = false;
            return;
        }
        

        isBonemealing = true;
        Rotations.rotate(Rotations.getYaw(crop), Rotations.getPitch(crop), () -> {
            InvUtils.swap(bonemeal.slot(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(
                mc.player.getPos(), rayTraceCheck(crop), crop, true));
            mc.player.swingHand(Hand.MAIN_HAND);
        });
    }

    private BlockPos getCrop() {
        for (int x = -4; x < 4; x++) {
            for (int y = -2; y < 2; y++) {
                for (int z = -4; z < 4; z++) {
                    BlockPos blockPos = mc.player.getBlockPos().add(x, y, z);
                    Block block = mc.world.getBlockState(blockPos).getBlock();
                    if (block instanceof CropBlock cropBlock) {
                        int age = mc.world.getBlockState(blockPos).get(cropBlock.getAgeProperty());
                        if (age < cropBlock.getMaxAge())
                            return blockPos;
                    }
                }
            }
        }
        return null;
    }
    private Direction rayTraceCheck(BlockPos pos) {
        Vec3d eyesPos = new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());
        for (Direction direction : Direction.values()) {
            RaycastContext raycastContext = new RaycastContext(eyesPos, new Vec3d(pos.getX() + 0.5 + direction.getVector().getX() * 0.5,
                pos.getY() + 0.5 + direction.getVector().getY() * 0.5,
                pos.getZ() + 0.5 + direction.getVector().getZ() * 0.5), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
            BlockHitResult result = mc.world.raycast(raycastContext);
            if (result != null && result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(pos)) {
                return direction;
            }
        }

        if (pos.getY() > eyesPos.y) return Direction.DOWN;

        return Direction.UP;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        BlockPos crop = getCrop();
        if (crop == null || !InvUtils.findInHotbar(Items.BONE_MEAL).found()) return;
        event.renderer.box(crop, Color.WHITE, Color.WHITE, ShapeMode.Lines, 0);
    }

    @Override
    public String getInfoString() {
        return isBonemealing?"Busy":"";
    }
}
