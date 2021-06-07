package cloudburst.rejects.modules;

import cloudburst.rejects.MeteorRejectsAddon;
import cloudburst.rejects.utils.WorldUtils;
import meteordevelopment.orbit.EventHandler;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.systems.modules.Module;
import minegame159.meteorclient.utils.player.FindItemResult;
import minegame159.meteorclient.utils.player.InvUtils;
import minegame159.meteorclient.utils.world.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

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
    
    private ArrayList<BlockPos> positions = new ArrayList<>();
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
        FindItemResult findItemResult = InvUtils.findInHotbar(itemStack -> block.get() == Block.getBlockFromItem(itemStack.getItem()));
        if (!findItemResult.found()) {
            error("No selected blocks in hotbar");
            toggle();
            return;
        }
        
        // Find spots
        for (BlockPos blockPos : WorldUtils.getSphere(mc.player.getBlockPos(), range.get(), range.get())) {
            if (shouldPlace(blockPos, block.get())) positions.add(blockPos);
        }
        
        // Place
        for (BlockPos blockPos : positions) {
            BlockUtils.place(blockPos, findItemResult, rotate.get(), -100, false);
    
            // Delay 0
            if (delay.get() != 0) break;
        }
    }
    
    private boolean shouldPlace(BlockPos blockPos, Block useBlock) {
        // Self
        if (!mc.world.getBlockState(blockPos).getMaterial().isReplaceable()) return false;
    
        // One block height
        if (!oneBlockHeight.get() &&
                !mc.world.getBlockState(blockPos.up()).getMaterial().isReplaceable() &&
                !mc.world.getBlockState(blockPos.down()).getMaterial().isReplaceable()) return false;
    
    
        boolean north = true;
        boolean south = true;
        boolean east = true;
        boolean west = true;
        boolean up = true;
        boolean bottom = true;
        BlockState northState = mc.world.getBlockState(blockPos.north());
        BlockState southState = mc.world.getBlockState(blockPos.south());
        BlockState eastState = mc.world.getBlockState(blockPos.east());
        BlockState westState = mc.world.getBlockState(blockPos.west());
        BlockState upState = mc.world.getBlockState(blockPos.up());
        BlockState bottomState = mc.world.getBlockState(blockPos.down());
    
        // Top surface
        if (topSurfaces.get()) {
            if (upState.getMaterial().isReplaceable() || upState.getBlock() == useBlock) up = false;
        }
        
        // Side surfaces
        if (sideSurfaces.get()) {
            
            if (northState.getMaterial().isReplaceable() || northState.getBlock() == useBlock) north = false;
            if (southState.getMaterial().isReplaceable() || southState.getBlock() == useBlock) south = false;
            if (eastState.getMaterial().isReplaceable() || eastState.getBlock() == useBlock) east = false;
            if (westState.getMaterial().isReplaceable() || westState.getBlock() == useBlock) west = false;
        }
        
        // Bottom surface
        if (bottomSurfaces.get()) {
            if (bottomState.getMaterial().isReplaceable() || bottomState.getBlock() == useBlock) bottom = false;
        }
    
        return north || south || east || west || up || bottom;
    }
}
