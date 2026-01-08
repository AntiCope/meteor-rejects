package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.AutoEat;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ObsidianFarm extends Module {

    private boolean allowBreakAgain;
    
    public ObsidianFarm() {
        super(MeteorRejectsAddon.CATEGORY, "obsidian-farm", "Auto obsidian farm(portals).");
    }

    @Override
    public void onActivate() {
        allowBreakAgain = true;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;
        if (mc.level == null) return;
        if (mc.gameMode == null) return;
        if (mc.level.dimensionType().attributes().applyModifier(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false)) {
            allowBreakAgain = true;
            return;
        }
        if (!allowBreakAgain) return;
        if ((mc.player.isUsingItem() || Modules.get().get(AutoEat.class).isActive()) && (mc.player.getOffhandItem().has(DataComponents.FOOD) || mc.player.getMainHandItem().has(DataComponents.FOOD)))
            return;

        if(mc.player.getMainHandItem().getItem() != Items.NETHERITE_PICKAXE && mc.player.getMainHandItem().getItem() != Items.DIAMOND_PICKAXE) {
            int pickAxe = findPickAxe();
            if (pickAxe == -1) {
                if (this.isActive()) {
                    this.toggle();
                    return;
                }
            }
            mc.player.getInventory().setSelectedSlot(pickAxe);
        }

        BlockPos obsidian = findObsidian();
        if (obsidian == null) return;

        mc.gameMode.continueDestroyBlock(obsidian, Direction.UP);
        mc.player.swing(InteractionHand.MAIN_HAND);

        if (mc.player.blockPosition().below().equals(obsidian) && mc.level.getBlockState(obsidian).getBlock() != Blocks.OBSIDIAN) {
            allowBreakAgain = false;
        }
    }

    private BlockPos findObsidian() {
        List<BlockPos> blocksList = new ArrayList<>();

        for (int x = -2; x < 3; x++) {
            for (int z = -2; z < 3; z++) {
                int y = 2;
                BlockPos block = new BlockPos(mc.player.blockPosition().getX() + x, mc.player.blockPosition().getY() + y, mc.player.blockPosition().getZ() + z);
                blocksList.add(block);
            }
        }

        Optional<BlockPos> result = blocksList.stream()
                .parallel()
                .filter(blockPos -> mc.level.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN)
                .min(Comparator.comparingDouble(blockPos -> mc.player.distanceToSqr(blockPos.getX(), blockPos.getY(), blockPos.getZ())));
        if (result.isPresent()) return result.get();

        blocksList.clear();
        for (int x = -2; x < 3; x++) {
            for (int z = -2; z < 3; z++) {
                for (int y = 3; y > -2; y--) {
                    BlockPos block = new BlockPos(mc.player.blockPosition().getX() + x, mc.player.blockPosition().getY() + y, mc.player.blockPosition().getZ() + z);
                    blocksList.add(block);
                }
            }
        }

        Optional<BlockPos> result2 = blocksList.stream()
                .parallel()
                .filter(blockPos -> !mc.player.blockPosition().below().equals(blockPos))
                .filter(blockPos -> mc.level.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN)
                .min(Comparator.comparingDouble(blockPos -> mc.player.distanceToSqr(blockPos.getX(), blockPos.getY(), blockPos.getZ())));
        if (result2.isPresent()) return result2.get();

        if (mc.level.getBlockState(mc.player.blockPosition().below()).getBlock() == Blocks.OBSIDIAN)
            return mc.player.blockPosition().below();

        return null;
    }

    private int findPickAxe() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).getItem() == Items.NETHERITE_PICKAXE) return i;
            if (mc.player.getInventory().getItem(i).getItem() == Items.DIAMOND_PICKAXE) return i;
        }
        return -1;
    }


}
