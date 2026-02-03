package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.utils.WorldUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class MossBot extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
            .name("range")
            .description("The bonemeal range.")
            .defaultValue(4)
            .min(0)
            .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
            .name("rotate")
            .description("Whether or not to rotate towards block while bonemealing.")
            .defaultValue(true)
            .build()
    );

    private final Map<BlockPos, Integer> mossMap = new HashMap<>();

    public MossBot() {
        super(MeteorRejectsAddon.CATEGORY, "moss-bot", "Use bonemeal on moss blocks with maximized efficiency.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        mossMap.entrySet().removeIf(e -> e.setValue(e.getValue() - 1) == 0);
        FindItemResult findItemResult = InvUtils.findInHotbar(Items.BONE_MEAL);
        if (!findItemResult.found()) {
            return;
        }

        BlockPos bestBlock = BlockPos.withinManhattanStream(BlockPos.containing(mc.player.getEyePosition()), range.get(), range.get(), range.get())
                .filter(b -> mc.player.getEyePosition().distanceTo(Vec3.atCenterOf(b)) <= range.get() && !mossMap.containsKey(b))
                .map(b -> Pair.of(b.immutable(), getMossSpots(b)))
                .filter(p -> p.getRight() > 10)
                .map(Pair::getLeft)
                .max(Comparator.naturalOrder()).orElse(null);

        if (bestBlock != null) {
            if (!mc.level.isEmptyBlock(bestBlock.above())) {
                mc.gameMode.continueDestroyBlock(bestBlock.above(), Direction.UP);
            }

            WorldUtils.interact(bestBlock, findItemResult, rotate.get());
            mossMap.put(bestBlock, 100);
        }
    }

    private int getMossSpots(BlockPos pos) {
        if (mc.level.getBlockState(pos).getBlock() != Blocks.MOSS_BLOCK
                || mc.level.getBlockState(pos.above()).getDestroySpeed(mc.level, pos) != 0f) {
            return 0;
        }

        return (int) BlockPos.withinManhattanStream(pos, 3, 4, 3)
                .filter(b -> isMossGrowableOn(mc.level.getBlockState(b).getBlock()) && mc.level.isEmptyBlock(b.above()))
                .count();
    }

    private boolean isMossGrowableOn(Block block) {
        return block == Blocks.STONE || block == Blocks.GRANITE || block == Blocks.ANDESITE || block == Blocks.DIORITE
                || block == Blocks.DIRT || block == Blocks.COARSE_DIRT || block == Blocks.MYCELIUM || block == Blocks.GRASS_BLOCK
                || block == Blocks.PODZOL || block == Blocks.ROOTED_DIRT;
    }
}