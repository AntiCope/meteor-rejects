package anticope.rejects.modules;

import net.minecraft.block.Block;
import net.minecraft.block.CropBlock;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
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
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(Utils.vec3d(crop), Direction.UP, crop, false), 0));
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
                    if (block instanceof CocoaBlock) {
                        int age = mc.world.getBlockState(blockPos).get(CocoaBlock.AGE);
                        if (age < 2)
                            return blockPos;
                    }
                    if (block instanceof StemBlock) {
                        int age = mc.world.getBlockState(blockPos).get(StemBlock.AGE);
                        if (age < StemBlock.MAX_AGE)
                            return blockPos;
                    }
                    if (block instanceof MushroomPlantBlock) {
                            return blockPos;
                    }
                    if (block instanceof SweetBerryBushBlock) {
                        int age = mc.world.getBlockState(blockPos).get(SweetBerryBushBlock.AGE);
                        if (age < 3)
                            return blockPos;
                    }
                     if (block instanceof SaplingBlock || block instanceof AzaleaBlock){
                            return blockPos;
                    }
                }
            }
        }
        return null;
    }

    private boolean canPlaceSapling(BlockPos blockPos) {
        return false;
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
