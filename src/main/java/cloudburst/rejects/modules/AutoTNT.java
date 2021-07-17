package cloudburst.rejects.modules;

import cloudburst.rejects.MeteorRejectsAddon;
import cloudburst.rejects.utils.TntDamage;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.Items;
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
    
    private final Setting<Boolean> ignite = sgGeneral.add(new BoolSetting.Builder()
            .name("ignite")
            .description("Whether to ignite tnt.")
            .defaultValue(false)
            .build()
    );
    
    private final Setting<Boolean> place = sgGeneral.add(new BoolSetting.Builder()
            .name("place")
            .description("Whether to place tnt.")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Integer> igniteDelay = sgGeneral.add(new IntSetting.Builder()
            .name("ignition-delay")
            .description("Delay in ticks between ignition")
            .defaultValue(1)
            .visible(ignite::get)
            .build()
    );
    
    private final Setting<Integer> placeDelay = sgGeneral.add(new IntSetting.Builder()
            .name("place-delay")
            .description("Delay in ticks between placement")
            .defaultValue(1)
            .visible(place::get)
            .build()
    );
    
    private final Setting<Integer> horizontalRange = sgGeneral.add(new IntSetting.Builder()
            .name("horizontal-range")
            .description("Horizontal range of ignition and placement")
            .defaultValue(4)
            .build()
    );
    
    private final Setting<Integer> verticalRange = sgGeneral.add(new IntSetting.Builder()
            .name("vertical-range")
            .description("Vertical range of ignition and placement")
            .defaultValue(4)
            .build()
    );
    
    private final Setting<Boolean> antiBreak = sgGeneral.add(new BoolSetting.Builder()
            .name("anti-break")
            .description("Whether to save flint and steel from breaking.")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
            .name("rotate")
            .description("Whether to rotate towards action.")
            .defaultValue(true)
            .build()
    );
    
    private final List<BlockPos.Mutable> blocksToIgnite = new ArrayList<>();
    private final List<BlockPos.Mutable> blocksToPlace = new ArrayList<>();
    private final Pool<BlockPos.Mutable> ignitePool = new Pool<>(BlockPos.Mutable::new);
    private final Pool<BlockPos.Mutable> placePool = new Pool<>(BlockPos.Mutable::new);
    private int igniteTick = 0;
    private int placeTick = 0;
    private int prevSlot;
    
    public AutoTNT() {
        super(MeteorRejectsAddon.CATEGORY, "auto-tnt", "Ignites TNT for you");
    }
    
    @Override
    public void onDeactivate() {
        igniteTick = 0;
        placeTick = 0;
    }
    
    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        if (ignite.get() && igniteTick > igniteDelay.get()) {
            // Clear blocks
            for (BlockPos.Mutable blockPos : blocksToIgnite) ignitePool.free(blockPos);
            blocksToIgnite.clear();
            
            // Item check
            FindItemResult findSlot = InvUtils.findInHotbar(item -> item.getItem() instanceof FlintAndSteelItem && (antiBreak.get() && (item.getMaxDamage() - item.getDamage()) > 10));
            if (!findSlot.found()) {
                error("No flint and steel in hotbar");
                toggle();
                return;
            }
    
            // Register
            BlockIterator.register(horizontalRange.get(), verticalRange.get(), (blockPos, blockState) -> {
                if (blockState.getBlock() == Blocks.TNT) blocksToIgnite.add(ignitePool.get().set(blockPos));
            });
        }
        
        if (place.get() && placeTick > placeDelay.get()) {
            // Clear blocks
            for (BlockPos.Mutable blockPos : blocksToPlace) placePool.free(blockPos);
            blocksToPlace.clear();
            
            // Item check
            FindItemResult findSlot = InvUtils.findInHotbar(item -> item.getItem() == Items.TNT);
            if (!findSlot.found()) {
                error("No tnt in hotbar");
                toggle();
                return;
            }
            
            // Register
            BlockIterator.register(horizontalRange.get(), verticalRange.get(), (blockPos, blockState) -> {
                if (BlockUtils.canPlace(blockPos)) blocksToPlace.add(placePool.get().set(blockPos));
            });
        }
    }
    
    @EventHandler
    private void onPostTick(TickEvent.Post event) {
        // Ignition
        if (ignite.get() && blocksToIgnite.size() > 0) {
            if (igniteTick > igniteDelay.get()) {
                // Sort based on closest tnt
                blocksToIgnite.sort(Comparator.comparingDouble(PlayerUtils::distanceTo));
        
                // Ignition
                ignite(blocksToIgnite.get(0), InvUtils.findInHotbar(item -> item.getItem() instanceof FlintAndSteelItem && (antiBreak.get() && (item.getMaxDamage() - item.getDamage()) > 10)));
        
                // Reset ticks
                igniteTick = 0;
            } else igniteTick++;
        }
        
        // Placement
        if (place.get() && blocksToPlace.size() > 0) {
            if (placeTick > placeDelay.get()) {
                // Sort based on closest tnt
                blocksToPlace.sort((o1, o2) -> {
                    int s1 = TntDamage.calculate(o1);
                    int s2 = TntDamage.calculate(o2);
                    return Integer.compare(s1, s2);
                });
        
                // Placement
                place(blocksToPlace.get(0), InvUtils.findInHotbar(item -> item.getItem() == Items.TNT));
        
                // Reset ticks
                placeTick = 0;
            } else placeTick++;
        }
    }
    
    private void ignite(BlockPos pos, FindItemResult item) {
        // Set slots
        prevSlot = mc.player.getInventory().selectedSlot;
        InvUtils.swap(item.getSlot());

        mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Direction.UP, pos, true));
    
        InvUtils.swap(prevSlot);
    }
    
    private void place(BlockPos pos, FindItemResult item) {
        BlockUtils.place(pos, item, rotate.get(), 10);
    }
}
