package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.utils.WorldUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Painter extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Block> block = sgGeneral.add(new BlockSetting.Builder()
            .name("block")
            .description("Block to use for painting")
            .defaultValue(Blocks.STONE_BUTTON)
            .build()
    );
    
    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
            .name("range")
            .description("Range of placement")
            .min(0)
            .defaultValue(0)
            .build()
    );
    
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
            .name("delay")
            .description("Delay between block placement (in ticks)")
            .min(0)
            .defaultValue(0)
            .build()
    );

    private final Setting<Integer> bpt = sgGeneral.add(new IntSetting.Builder()
            .name("blocks-per-tick")
            .description("Amount of blocks that can be placed in one tick")
            .min(1)
            .defaultValue(1)
            .build()
    );
    
    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
            .name("rotate")
            .description("Whether or not to rotate towards block while placing")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Boolean> topSurfaces = sgGeneral.add(new BoolSetting.Builder()
            .name("top-surface")
            .description("Whether or not to cover top surfaces")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Boolean> sideSurfaces = sgGeneral.add(new BoolSetting.Builder()
            .name("side-surface")
            .description("Whether or not to cover side surfaces")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Boolean> bottomSurfaces = sgGeneral.add(new BoolSetting.Builder()
            .name("bottom-surface")
            .description("Whether or not to cover bottom surfaces")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Boolean> oneBlockHeight = sgGeneral.add(new BoolSetting.Builder()
            .name("one-block-height")
            .description("Whether or not to cover in one block high gaps")
            .defaultValue(true)
            .build()
    );

    private int ticksWaited;
    
    public Painter() {
        super(MeteorRejectsAddon.CATEGORY, "painter", "Automatically paints/covers surfaces (good for trolling)");
    }
    
    @EventHandler
    private void onTick(TickEvent.Post event) {
        // Tick delay
        if (delay.get() != 0 && ticksWaited < delay.get() - 1) {
            ticksWaited++;
            return;
        }
        else ticksWaited = 0;
        
        // Get slot
        FindItemResult findItemResult = InvUtils.findInHotbar(itemStack -> block.get() == Block.byItem(itemStack.getItem()));
        if (!findItemResult.found()) {
            error("No selected blocks in hotbar");
            toggle();
            return;
        }
        
        // Find spots
        int placed = 0;
        for (BlockPos blockPos : WorldUtils.getSphere(mc.player.blockPosition(), range.get(), range.get())) {
            if (shouldPlace(blockPos, block.get())) {
                BlockUtils.place(blockPos, findItemResult, rotate.get(), -100, false);
                placed++;

                // Delay 0
                if (delay.get() != 0 && placed >= bpt.get()) break;
            }
        }
    }
    
    private boolean shouldPlace(BlockPos blockPos, Block useBlock) {
        // Self
        if (!mc.level.getBlockState(blockPos).canBeReplaced()) return false;
    
        // One block height
        if (!oneBlockHeight.get() &&
                !mc.level.getBlockState(blockPos.above()).canBeReplaced() &&
                !mc.level.getBlockState(blockPos.below()).canBeReplaced()) return false;
    
    
        boolean north = true;
        boolean south = true;
        boolean east = true;
        boolean west = true;
        boolean up = true;
        boolean bottom = true;
        BlockState northState = mc.level.getBlockState(blockPos.north());
        BlockState southState = mc.level.getBlockState(blockPos.south());
        BlockState eastState = mc.level.getBlockState(blockPos.east());
        BlockState westState = mc.level.getBlockState(blockPos.west());
        BlockState upState = mc.level.getBlockState(blockPos.above());
        BlockState bottomState = mc.level.getBlockState(blockPos.below());
    
        // Top surface
        if (topSurfaces.get()) {
            if (upState.canBeReplaced() || upState.getBlock() == useBlock) up = false;
        }
        
        // Side surfaces
        if (sideSurfaces.get()) {
            
            if (northState.canBeReplaced() || northState.getBlock() == useBlock) north = false;
            if (southState.canBeReplaced() || southState.getBlock() == useBlock) south = false;
            if (eastState.canBeReplaced() || eastState.getBlock() == useBlock) east = false;
            if (westState.canBeReplaced() || westState.getBlock() == useBlock) west = false;
        }
        
        // Bottom surface
        if (bottomSurfaces.get()) {
            if (bottomState.canBeReplaced() || bottomState.getBlock() == useBlock) bottom = false;
        }
    
        return north || south || east || west || up || bottom;
    }
}
