package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.FallingBlock;
import net.minecraft.entity.MovementType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class BlockIn extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Boolean> multiPlace = sgGeneral.add(new BoolSetting.Builder()
            .name("multi-place")
            .description("Whether to place all blocks in a single tick")
            .defaultValue(false)
            .build()
    );
    
    private final Setting<Boolean> center = sgGeneral.add(new BoolSetting.Builder()
            .name("center")
            .description("Whether to center to avoid obstructing placement")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
            .name("rotate")
            .description("Whether to rotate towards block placements.")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Boolean> turnOff = sgGeneral.add(new BoolSetting.Builder()
            .name("turn-off")
            .description("Whether to turn off after finished placing.")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Boolean> onlyOnGround = sgGeneral.add(new BoolSetting.Builder()
            .name("only-on-ground")
            .description("Only places when you are standing on blocks (not in midair).")
            .defaultValue(true)
            .build()
    );
    
    private final BlockPos.Mutable bp = new BlockPos.Mutable();
    private boolean return_;
    private double sY;
    
    public BlockIn() {
        super(MeteorRejectsAddon.CATEGORY, "block-in", "Block yourself in using any block.");
    }
    
    @Override
    public void onActivate() {
        sY = mc.player.getPos().getY();
    }
    
    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        if (center.get()) {
            if (!onlyOnGround.get()) {
                mc.player.setVelocity(0,0,0);
                mc.player.move(MovementType.SELF, new Vec3d(0, -(sY-Math.floor(sY)), 0));
            }
            PlayerUtils.centerPlayer();
        }
        if (onlyOnGround.get() && !mc.player.isOnGround()) return;
        
        return_ = false;
        
        // Multiplace
        if (multiPlace.get()) {
            // Bottom
            boolean p1 = place(0, -1, 0);
            // Lower sides
            boolean p2 = place(1, 0, 0);
            boolean p3 = place(-1, 0, 0);
            boolean p4 = place(0, 0, 1);
            boolean p5 = place(0, 0, -1);
            // Upper sides
            boolean p6 = place(1, 1, 0);
            boolean p7 = place(-1, 1, 0);
            boolean p8 = place(0, 1, 1);
            boolean p9 = place(0, 1, -1);
            // Top
            boolean p10 = place(0, 2, 0);
            
            // Turn off
            if (turnOff.get() && p1 && p2 && p3 && p4 && p5 && p6 && p7 && p8 && p9 && p10) toggle();
            
            // No multiplace
        } else {
            // Bottom
            boolean p1 = place(0, -1, 0);
            if (return_) return;
            // Lower sides
            boolean p2 = place(1, 0, 0);
            if (return_) return;
            boolean p3 = place(-1, 0, 0);
            if (return_) return;
            boolean p4 = place(0, 0, 1);
            if (return_) return;
            boolean p5 = place(0, 0, -1);
            if (return_) return;
            // Upper sides
            boolean p6 = place(1, 1, 0);
            if (return_) return;
            boolean p7 = place(-1, 1, 0);
            if (return_) return;
            boolean p8 = place(0, 1, 1);
            if (return_) return;
            boolean p9 = place(0, 1, -1);
            if (return_) return;
            // Top
            boolean p10 = place(0, 2, 0);
            
            // Turn off
            if (turnOff.get() && p1 && p2 && p3 && p4 && p5 && p6 && p7 && p8 && p9 && p10) toggle();
        }
    }
    
    private boolean place(int x, int y, int z) {
        setBlockPos(x, y, z);
        FindItemResult findItemResult = InvUtils.findInHotbar(itemStack -> validItem(itemStack, bp));
        if (!BlockUtils.canPlace(bp)) return true;
        
        if (BlockUtils.place(bp, findItemResult, rotate.get(), 100, true)) {
            return_ = true;
        }
        
        return false;
    }
    
    private void setBlockPos(int x, int y, int z) {
        bp.set(mc.player.getX() + x, mc.player.getY() + y, mc.player.getZ() + z);
    }
    
    private boolean validItem(ItemStack itemStack, BlockPos pos) {
        if (!(itemStack.getItem() instanceof BlockItem)) return false;
        Block block = ((BlockItem) itemStack.getItem()).getBlock();
        
        if (!Block.isShapeFullCube(block.getDefaultState().getCollisionShape(mc.world, pos))) return false;
        return !(block instanceof FallingBlock) || !FallingBlock.canFallThrough(mc.world.getBlockState(pos));
    }
}
