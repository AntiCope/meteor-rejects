package cloudburst.rejects.modules;

import cloudburst.rejects.MeteorRejectsAddon;
import cloudburst.rejects.utils.WorldUtils;
import meteordevelopment.orbit.EventHandler;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.IntSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.systems.modules.Module;
import minegame159.meteorclient.utils.player.FindItemResult;
import minegame159.meteorclient.utils.player.InvUtils;
import minegame159.meteorclient.utils.player.PlayerUtils;
import net.minecraft.block.Blocks;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
    private boolean ignited;
    private int ticks = 0;
    private int preSlot;
    private FindItemResult findSlot;
    
    public AutoTNT() {
        super(MeteorRejectsAddon.CATEGORY, "auto-tnt", "Ignites TNT for you");
    }
    
    @Override
    public void onActivate() {
        ignited = false;
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
            
            List<BlockPos> searchBlocks = WorldUtils.getSphere(mc.player.getBlockPos(), range.get(), range.get());
            for (BlockPos blockPos : searchBlocks) {
                if (mc.world.getBlockState(blockPos).getBlock() == Blocks.TNT) blocks.add(blockPos);
            }
            
            
            // If there isn't any tnt
            if (blocks.size() <= 0) {
                
                // If we should just turn off after igniting
                if (turnOff.get() && ignited) {
                    toggle();
                }
                
                return;
            }
            
            // Sort based on closest tnt
            blocks.sort(Comparator.comparingDouble(PlayerUtils::distanceTo));
            
            // Get slot
            findSlot = getFlintAndSteelSlot();
            if (!findSlot.found()) {
                error("No flint and steel in hotbar");
                toggle();
                return;
            }
            
            // Ignition
            ignite(blocks.get(0), findSlot);
            
            // Reset ticks
            ticks = delay.get();
        } else ticks--;
    }
    
    private void ignite(BlockPos pos, FindItemResult item) {
        // Set slots
        preSlot = mc.player.inventory.selectedSlot;
        mc.player.inventory.selectedSlot = item.slot;


        ActionResult result = mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Direction.UP, pos, true));
//        ActionResult result = mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), Direction.UP, pos, false));
        if (result == ActionResult.CONSUME || result == ActionResult.SUCCESS) ignited = true;
        
        // Reset slot
        mc.player.inventory.selectedSlot = preSlot;
    }
    
    private FindItemResult getFlintAndSteelSlot() {
        return InvUtils.findInHotbar(item -> item.getItem() instanceof FlintAndSteelItem && (antiBreak.get() && (item.getMaxDamage() - item.getDamage()) > 10));
    }
}
