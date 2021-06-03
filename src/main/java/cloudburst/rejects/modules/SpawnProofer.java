package cloudburst.rejects.modules;

import cloudburst.rejects.MeteorRejectsAddon;
import cloudburst.rejects.utils.WorldUtils;
import meteordevelopment.orbit.EventHandler;
import minegame159.meteorclient.events.render.RenderEvent;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.rendering.MeshBuilder;
import minegame159.meteorclient.rendering.Renderer;
import minegame159.meteorclient.rendering.ShapeMode;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.systems.modules.Module;
import minegame159.meteorclient.utils.player.InvUtils;
import minegame159.meteorclient.utils.render.color.SettingColor;
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
            .min(1)
            .max(4)
            .sliderMin(1)
            .sliderMax(1)
            .defaultValue(3)
            .build()
    );
    
    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
            .name("blocks")
            .description("Blocks to use for spawn proofing")
            .defaultValue(getDefaultBlocks())
            .build()
    );
    
    private final Setting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .description("Shape mode")
            .defaultValue(ShapeMode.Both)
            .build()
    );
    
    private final Setting<SettingColor> sideColor = sgGeneral.add(new ColorSetting.Builder()
            .name("side-color")
            .description("Edge color")
            .defaultValue(new SettingColor(255, 0, 0, 75))
            .build()
    );
    
    private final Setting<SettingColor> lineColor = sgGeneral.add(new ColorSetting.Builder()
            .name("line-color")
            .description("Line color")
            .defaultValue(new SettingColor(255, 0, 0, 255))
            .build()
    );
    
    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
            .name("rotate")
            .description("Rotates towards the blocks being placed.")
            .defaultValue(true)
            .build()
    );
    
    private final ArrayList<BlockPos> positions = new ArrayList<>();
    private final MeshBuilder mb = new MeshBuilder();
    
    public SpawnProofer() {
        super(MeteorRejectsAddon.CATEGORY, "spawn-proofer", "Spawn proofs things using slabs.");
    }
    
    @EventHandler
    private void onTick(TickEvent.Post event) {
        // Clear and set positions
        positions.clear();
        for (BlockPos blockPos : WorldUtils.getSphere(mc.player.getBlockPos(), range.get(), range.get())) {
            if (validSpawn(blockPos)) positions.add(blockPos);
        }
        
        for (BlockPos blockPos : positions) {
            // Set slot
            int slot = findSlot();
            // Place blocks
            BlockUtils.place(blockPos, Hand.MAIN_HAND, slot, rotate.get(), -50, true);
        }
    }
    
    @EventHandler
    private void onRender(RenderEvent event) {
        // Render all positions
        for (BlockPos blockPos : positions) {
            Renderer.boxWithLines(Renderer.NORMAL, Renderer.LINES, blockPos.down(), sideColor.get(), lineColor.get(), shapeMode.get(), 0);
        }
    }
    
    private int findSlot() {
        return InvUtils.findItemInHotbar(itemStack -> blocks.get().contains(Block.getBlockFromItem(itemStack.getItem())));
    }
    
    private boolean validSpawn(BlockPos blockPos) {
        BlockState blockState = mc.world.getBlockState(blockPos);
        
        if (blockPos.getY() == 0) return false;
        if (!(blockState.getBlock() instanceof AirBlock)) return false;
        
        if (!topSurface(mc.world.getBlockState(blockPos.down()))) {
            if (mc.world.getBlockState(blockPos.down()).getCollisionShape(mc.world, blockPos.down()) != VoxelShapes.fullCube()) return false;
            if (mc.world.getBlockState(blockPos.down()).isTranslucent(mc.world, blockPos.down())) return false;
        }
        
        if (mc.world.getLightLevel(blockPos, 0) <= 7) return false;
        else return mc.world.getLightLevel(LightType.BLOCK, blockPos) <= 7;
    }
    
    private boolean topSurface(BlockState blockState) {
        if (blockState.getBlock() instanceof SlabBlock && blockState.get(SlabBlock.TYPE) == SlabType.TOP) return true;
        else return blockState.getBlock() instanceof StairsBlock && blockState.get(StairsBlock.HALF) == BlockHalf.TOP;
    }
    
    private List<Block> getDefaultBlocks() {
        List<Block> defaultBlocks = new ArrayList<>();
        
        for (Block block : Registry.BLOCK) {
            if (block instanceof SlabBlock) defaultBlocks.add(block);
            if (block instanceof AbstractButtonBlock) defaultBlocks.add(block);
            if (block instanceof TorchBlock) defaultBlocks.add(block);
        }
        
        return defaultBlocks;
    }
}
