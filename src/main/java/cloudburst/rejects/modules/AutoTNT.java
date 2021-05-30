package cloudburst.rejects.modules;

import meteordevelopment.orbit.EventHandler;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.IntSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.systems.modules.Module;
import minegame159.meteorclient.utils.player.InvUtils;
import minegame159.meteorclient.utils.player.PlayerUtils;
import minegame159.meteorclient.utils.world.BlockUtils;
import net.minecraft.block.TntBlock;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import cloudburst.rejects.MeteorRejectsAddon;

import java.util.ArrayList;
import java.util.Comparator;

public class AutoTNT extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    // General
    
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
            .name("delay")
            .description("Delay in ticks between ignition")
            .defaultValue(1)
            .build()
    );
    
    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
            .name("range")
            .description("Range of ignition")
            .defaultValue(4)
            .build()
    );
    
    private final Setting<Boolean> turnOff = sgGeneral.add(new BoolSetting.Builder()
            .name("turn-off")
            .description("Turns of after igniting tnt")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Boolean> antiBreak = sgGeneral.add(new BoolSetting.Builder()
            .name("anti-break")
            .description("Whether or not to save flint and steel from breaking.")
            .defaultValue(true)
            .build()
    );
    
    private final ArrayList<BlockPos> blocks = new ArrayList<>();
    private final BlockPos.Mutable bp = new BlockPos.Mutable();
    private boolean ignited, messaged;
    private int ticks = 0;
    private int preSlot, slot;
    
    public AutoTNT() {
        super(MeteorRejectsAddon.CATEGORY, "auto-tnt", "Ignites TNT for you");
    }
    
    @Override
    public void onActivate() {
        ignited = false;
        messaged = false;
    }
    
    @Override
    public void onDeactivate() {
        ticks = 0;
    }
    
    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (ticks <= 0) {
            // Clear and get tnt blocks
            blocks.clear();
            for (BlockPos blockPos : BlockUtils.getSphere(mc.player.getBlockPos(), range.get(), range.get())) {
                bp.set(blockPos);
                if (mc.world.getBlockState(blockPos).getBlock() instanceof TntBlock) blocks.add(bp);
            }
            
            // Make sure there are TNTs around us
            if (blocks.size() <= 0) {
                
                // If already ignited and turnOff.get()
                if (turnOff.get() && ignited) {
                    toggle();
                    return;
                }
                
                // If we haven't warned yet
                if (!messaged) {
                    error("No TNT in range");
                    messaged = true;
                }
                return;
            } else messaged = false;
            
            // Sort based on closest tnt
            blocks.sort(Comparator.comparingDouble(PlayerUtils::distanceTo));
            
            // Get slot
            slot = getSlot();
            if (slot == -1) {
                error("No flint and steel in hotbar");
                return;
            }
            
            // Ignition
            bp.set(blocks.get(0));
            ignite(bp, slot);
            
            // Reset ticks
            ticks = delay.get();
        } else ticks--;
    }
    
    private void ignite(BlockPos pos, int slot) {
        // Set slots
        preSlot = mc.player.inventory.selectedSlot;
        mc.player.inventory.selectedSlot = slot;
        
        // Ignited the tnt
        ActionResult result = mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Direction.UP, pos, true));
        if (result == ActionResult.CONSUME || result == ActionResult.SUCCESS) ignited = true;
        
        // Reset slot
        mc.player.inventory.selectedSlot = preSlot;
    }
    
    private int getSlot() {
        return InvUtils.findItemInHotbar(item -> item.getItem() instanceof FlintAndSteelItem && (antiBreak.get() && (item.getMaxDamage() - item.getDamage()) > 10));
    }
    
    private void setBpToVec3d(Vec3d pos) {
        bp.set(pos.getX(), pos.getY(), pos.getZ());
    }
}