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
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
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

        BlockPos bestBlock = BlockPos.streamOutwards(BlockPos.ofFloored(mc.player.getEyePos()), range.get(), range.get(), range.get())
                .filter(b -> mc.player.getEyePos().distanceTo(Vec3d.ofCenter(b)) <= range.get() && !mossMap.containsKey(b))
                .map(b -> Pair.of(b.toImmutable(), getMossSpots(b)))
                .filter(p -> p.getRight() > 10)
                .map(Pair::getLeft)
                .max(Comparator.naturalOrder()).orElse(null);

        if (bestBlock != null) {
            if (!mc.world.isAir(bestBlock.up())) {
                mc.interactionManager.updateBlockBreakingProgress(bestBlock.up(), Direction.UP);
            }

            BlockUtils.place(bestBlock, findItemResult, rotate.get(), -50);
            mossMap.put(bestBlock, 100);
        }
    }

    private int getMossSpots(BlockPos pos) {
        if (mc.world.getBlockState(pos).getBlock() != Blocks.MOSS_BLOCK
                || mc.world.getBlockState(pos.up()).getHardness(mc.world, pos) != 0f) {
            return 0;
        }

        return (int) BlockPos.streamOutwards(pos, 3, 4, 3)
                .filter(b -> isMossGrowableOn(mc.world.getBlockState(b).getBlock()) && mc.world.isAir(b.up()))
                .count();
    }

    private boolean isMossGrowableOn(Block block) {
        return block == Blocks.STONE || block == Blocks.GRANITE || block == Blocks.ANDESITE || block == Blocks.DIORITE
                || block == Blocks.DIRT || block == Blocks.COARSE_DIRT || block == Blocks.MYCELIUM || block == Blocks.GRASS_BLOCK
                || block == Blocks.PODZOL || block == Blocks.ROOTED_DIRT;
    }
}