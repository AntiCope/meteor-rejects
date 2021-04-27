package cloudburst.rejects.modules;

import cloudburst.rejects.MeteorRejectsAddon;
import com.google.common.collect.Streams;
import meteordevelopment.orbit.EventHandler;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.systems.modules.Categories;
import minegame159.meteorclient.systems.modules.Module;
import minegame159.meteorclient.systems.modules.Modules;
import minegame159.meteorclient.systems.modules.player.AutoEat;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ObsidianFarm extends Module {

    public ObsidianFarm() {
        super(MeteorRejectsAddon.CATEGORY, "obsidian-farm", "Auto obsidian farm(portals).");
    }


    private boolean allowBreakAgain;

    @Override
    public void onActivate() {
        allowBreakAgain = true;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;
        if (mc.world == null) return;
        if (mc.interactionManager == null) return;
        if (mc.world.getDimension().isRespawnAnchorWorking()) {
            allowBreakAgain = true;
            return;
        }
        if (!allowBreakAgain) return;
        if ((mc.player.isUsingItem() || Modules.get().get(AutoEat.class).isActive()) && (mc.player.getOffHandStack().getItem().isFood() || mc.player.getMainHandStack().getItem().isFood()))
            return;

        if(mc.player.getMainHandStack().getItem() != Items.NETHERITE_PICKAXE && mc.player.getMainHandStack().getItem() != Items.DIAMOND_PICKAXE) {
            int pickAxe = findPickAxe();
            if (pickAxe == -1) {
                if (this.isActive()) {
                    this.toggle();
                    return;
                }
            }
            mc.player.inventory.selectedSlot = pickAxe;
        }

        BlockPos obsidian = findObsidian();
        if (obsidian == null) return;

        mc.interactionManager.updateBlockBreakingProgress(obsidian, Direction.UP);
        mc.player.swingHand(Hand.MAIN_HAND);

        if (mc.player.getBlockPos().down().equals(obsidian) && mc.world.getBlockState(obsidian).getBlock() != Blocks.OBSIDIAN) {
            allowBreakAgain = false;
        }
    }


    private BlockPos findObsidian() {
        List<BlockPos> blocksList = new ArrayList<>();

        for (int x = -2; x < 3; x++) {
            for (int z = -2; z < 3; z++) {
                int y = 2;
                BlockPos block = new BlockPos(mc.player.getBlockPos().getX() + x, mc.player.getBlockPos().getY() + y, mc.player.getBlockPos().getZ() + z);
                blocksList.add(block);
            }
        }

        Optional<BlockPos> result = Streams.stream(blocksList)
                .parallel()
                .filter(blockPos -> mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN)
                .min(Comparator.comparingDouble(blockPos -> mc.player.squaredDistanceTo(blockPos.getX(), blockPos.getY(), blockPos.getZ())));
        if (result.isPresent()) return result.get();

        blocksList.clear();
        for (int x = -2; x < 3; x++) {
            for (int z = -2; z < 3; z++) {
                for (int y = 3; y > -2; y--) {
                    BlockPos block = new BlockPos(mc.player.getBlockPos().getX() + x, mc.player.getBlockPos().getY() + y, mc.player.getBlockPos().getZ() + z);
                    blocksList.add(block);
                }
            }
        }

        Optional<BlockPos> result2 = Streams.stream(blocksList)
                .parallel()
                .filter(blockPos -> !mc.player.getBlockPos().down().equals(blockPos))
                .filter(blockPos -> mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN)
                .min(Comparator.comparingDouble(blockPos -> mc.player.squaredDistanceTo(blockPos.getX(), blockPos.getY(), blockPos.getZ())));
        if (result2.isPresent()) return result2.get();

        if (mc.world.getBlockState(mc.player.getBlockPos().down()).getBlock() == Blocks.OBSIDIAN)
            return mc.player.getBlockPos().down();

        return null;
    }


    private int findPickAxe() {
        int result = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStack(i).getItem() == Items.NETHERITE_PICKAXE) return i;
            if (mc.player.inventory.getStack(i).getItem() == Items.DIAMOND_PICKAXE) result = i;
        }
        return result;
    }


}
