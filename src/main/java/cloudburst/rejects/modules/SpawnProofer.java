package cloudburst.rejects.modules;

import cloudburst.rejects.MeteorRejectsAddon;
import cloudburst.rejects.utils.WorldUtils;
import meteordevelopment.orbit.EventHandler;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.systems.modules.Module;
import minegame159.meteorclient.utils.player.InvUtils;
import minegame159.meteorclient.utils.world.BlockUtils;
import net.minecraft.block.*;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.LightType;

import java.util.ArrayList;
import java.util.List;

public class SpawnProofer extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
            .name("range")
            .description("Range for block placement and rendering")
            .min(0)
            .sliderMax(10)
            .defaultValue(3)
            .build()
    );
    
    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
            .name("blocks")
            .description("Block to use for spawn proofing")
            .defaultValue(getDefaultBlocks())
            .filter(this::filterBlocks)
            .build()
    );
    
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
            .name("delay")
            .description("Delay in ticks between placing blocks")
            .defaultValue(0)
            .min(0).sliderMax(10)
            .build()
    );
    
    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
            .name("rotate")
            .description("Rotates towards the blocks being placed.")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Boolean> spawnProofPotentialSpawns = sgGeneral.add(new BoolSetting.Builder()
            .name("potential-spawns")
            .description("Spawn Proofs Potential Spawns (Spots that have access to sunlight and only spawns mobs during night time)")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Boolean> spawnProofAlwaysSpawns = sgGeneral.add(new BoolSetting.Builder()
            .name("always-spawns")
            .description("Spawn Proofs Always Spawns (Spots that undoubtedly will spawn mobs)")
            .defaultValue(true)
            .build()
    );
    
    
    private final ArrayList<BlockPos> positions = new ArrayList<>();
    private int ticksWaited;
    
    public SpawnProofer() {
        super(MeteorRejectsAddon.CATEGORY, "spawn-proofer", "Automatically spawnproofs using blocks.");
    }
    
    @EventHandler
    private void onTick(TickEvent.Post event) {
    
        // Tick delay
        if (ticksWaited < delay.get()) {
            ticksWaited++;
            return;
        }
    
        // Find slot
        int slot = findSlot();
        if (slot == -1) {
            error("Found none of the chosen blocks in hotbar");
            toggle();
            return;
        }
        
        // Clear and set positions
        positions.clear();
        for (BlockPos blockPos : WorldUtils.getSphere(mc.player.getBlockPos(), range.get(), range.get())) {
            if (validSpawn(blockPos)) positions.add(blockPos);
        }
        if (positions.size() == 0) return;
    
        
        // Place the blocks
        if (delay.get() == 0) {
            
            for (BlockPos blockPos : positions)  BlockUtils.place(blockPos, Hand.MAIN_HAND, slot, rotate.get(), -50, false);
            
        } else {
            
            // If is light source
            if (isLightSource(Block.getBlockFromItem(mc.player.inventory.getStack(slot).getItem()))) {
        
                // Find lowest light level block
                int lowestLightLevel = 16;
                BlockPos selectedBlockPos = positions.get(0); // Just for initialization
                for (BlockPos blockPos : positions) {
            
                    int lightLevel = mc.world.getLightLevel(blockPos);
                    if (lightLevel < lowestLightLevel) {
                        lowestLightLevel = lightLevel;
                        selectedBlockPos = blockPos;
                    }
                }
                BlockUtils.place(selectedBlockPos, Hand.MAIN_HAND, slot, rotate.get(), -50, false);
                
            } else {
    
                // Place first in positions
                BlockUtils.place(positions.get(0), Hand.MAIN_HAND, slot, rotate.get(), -50, false);
    
            }
        }
        
        // Reset tick delay
        ticksWaited = 0;
    }
    
    private int findSlot() {
        return InvUtils.findItemInHotbar(itemStack -> blocks.get().contains(Block.getBlockFromItem(itemStack.getItem())));
    }
    
    private boolean validSpawn(BlockPos blockPos) { // Copied from Light Overlay and modified slightly
        BlockState blockState = mc.world.getBlockState(blockPos);
        
        if (blockPos.getY() == 0) return false;
        if (!(blockState.getBlock() instanceof AirBlock)) return false;
        
        if (!topSurface(mc.world.getBlockState(blockPos.down()))) {
            if (mc.world.getBlockState(blockPos.down()).getCollisionShape(mc.world, blockPos.down()) != VoxelShapes.fullCube()) return false;
            if (mc.world.getBlockState(blockPos.down()).isTranslucent(mc.world, blockPos.down())) return false;
        }
        
        if (mc.world.getLightLevel(blockPos, 0) <= 7) return spawnProofPotentialSpawns.get();
        else if (mc.world.getLightLevel(LightType.BLOCK, blockPos) <= 7) return spawnProofAlwaysSpawns.get();
        
        return false;
    }
    
    private boolean topSurface(BlockState blockState) { // Copied from Light Overlay
        if (blockState.getBlock() instanceof SlabBlock && blockState.get(SlabBlock.TYPE) == SlabType.TOP) return true;
        else return blockState.getBlock() instanceof StairsBlock && blockState.get(StairsBlock.HALF) == BlockHalf.TOP;
    }
    
    private List<Block> getDefaultBlocks() {
        
        ArrayList<Block> defaultBlocks = new ArrayList<>();
        for (Block block : Registry.BLOCK) {
            if (filterBlocks(block)) defaultBlocks.add(block);
        }
        return defaultBlocks;
    }
    
    private boolean filterBlocks(Block block) {
        return isNonOpaqueBlock(block) || isLightSource(block);
    }
    
    private boolean isNonOpaqueBlock(Block block) {
        return block instanceof AbstractButtonBlock ||
                block instanceof SlabBlock ||
                block instanceof AbstractPressurePlateBlock ||
                block instanceof TransparentBlock ||
                block instanceof TripwireBlock;
    }
    
    private boolean isLightSource(Block block) {
        return block.getDefaultState().getLuminance() > 0;
    }
}
