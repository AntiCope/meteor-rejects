package cloudburst.rejects.modules;

import cloudburst.rejects.MeteorRejectsAddon;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.block.BedBlock;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoBedTrap extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Integer> bpt = sgGeneral.add(new IntSetting.Builder()
            .name("blocks-per-tick")
            .description("How many blocks to place per tick")
            .defaultValue(2)
            .min(1)
            .sliderMax(8)
            .build()
    );
    
    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
            .name("rotate")
            .description("Rotates when placing")
            .defaultValue(true)
            .build()
    );
    
    BlockPos bed1;
    Direction bed2direction;
    BlockPos bed2;
    int cap = 0;
    boolean bed;

    public AutoBedTrap() {
        super(MeteorRejectsAddon.CATEGORY, "auto-bed-trap", "Automatically places obsidian around bed");
    }

    @Override
    public void onActivate() {
        cap = 0;
        bed1 = null;
        if (mc.crosshairTarget == null) {
            error("Not looking at a bed. Disabling.");
            toggle();
        }
        bed1 = mc.crosshairTarget.getType() == HitResult.Type.BLOCK ? ((BlockHitResult) mc.crosshairTarget).getBlockPos() : null;
        if (bed1 == null || !(mc.world.getBlockState(bed1).getBlock() instanceof BedBlock)) {
            error("Not looking at a bed. Disabling.");
            toggle();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        bed2direction = BedBlock.getOppositePartDirection(mc.world.getBlockState(bed1));
        if (bed2direction == Direction.EAST) {
            bed2 = bed1.east(1);
        } else if (bed2direction == Direction.NORTH) {
            bed2 = bed1.north(1);
        } else if (bed2direction == Direction.SOUTH) {
            bed2 = bed1.south(1);
        } else if (bed2direction == Direction.WEST) {
            bed2 = bed1.west(1);
        }

        placeTickAround(bed1);
        placeTickAround(bed2);
    }

    public void placeTickAround(BlockPos block) {
        for (BlockPos b : new BlockPos[]{
                block.up(), block.west(),
                block.north(), block.south(),
                block.east(), block.down()}) {

            if (cap >= bpt.get()) {
                cap = 0;
                return;
            }

            FindItemResult findBlock = InvUtils.findInHotbar(Items.OBSIDIAN);
            if (!findBlock.found()) {
                error("No specified blocks found. Disabling.");
                toggle();
            }


            if (BlockUtils.place(b, findBlock, rotate.get(), 10, false)) {
                cap++;
                if (cap >= bpt.get()) {
                    return;
                }
            };
        }
        cap = 0;
    }
}
