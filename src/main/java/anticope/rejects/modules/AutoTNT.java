package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.utils.TntDamage;
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
import net.minecraft.block.TntBlock;
import net.minecraft.item.*;
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
            .description("Whether to place tnt. (VERY LAGGY)")
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
            .visible(ignite::get)
            .build()
    );

    private final Setting<Boolean> fireCharge = sgGeneral.add(new BoolSetting.Builder()
            .name("fire-charge")
            .description("Whether to also use fire charges.")
            .defaultValue(true)
            .visible(ignite::get)
            .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
            .name("rotate")
            .description("Whether to rotate towards action.")
            .defaultValue(true)
            .build()
    );

    private final List<BlockPos.Mutable> blocksToIgnite = new ArrayList<>();
    private final Pool<BlockPos.Mutable> ignitePool = new Pool<>(BlockPos.Mutable::new);
    private final List<TntPos> blocksToPlace = new ArrayList<>();
    private final Pool<TntPos> placePool = new Pool<>(TntPos::new);
    private int igniteTick = 0;
    private int placeTick = 0;

    public AutoTNT() {
        super(MeteorRejectsAddon.CATEGORY, "auto-tnt", "Places and/or ignites tnt automatically. Good for griefing.");
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

            // Register
            BlockIterator.register(horizontalRange.get(), verticalRange.get(), (blockPos, blockState) -> {
                if (blockState.getBlock() instanceof TntBlock) blocksToIgnite.add(ignitePool.get().set(blockPos));
            });
        }

        if (place.get() && placeTick > placeDelay.get()) {
            // Clear blocks
            for (TntPos tntPos : blocksToPlace) placePool.free(tntPos);
            blocksToPlace.clear();

            // Register
            BlockIterator.register(horizontalRange.get(), verticalRange.get(), (blockPos, blockState) -> {
                if (BlockUtils.canPlace(blockPos)) blocksToPlace.add(placePool.get().set(blockPos, TntDamage.calculate(blockPos)));
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
                FindItemResult itemResult = InvUtils.findInHotbar(item -> {
                    if (item.getItem() instanceof FlintAndSteelItem) {
                        return (antiBreak.get() && (item.getMaxDamage() - item.getDamage()) > 10);
                    }
                    else if (item.getItem() instanceof FireChargeItem) {
                        return fireCharge.get();
                    }
                    return false;
                });
                if (!itemResult.found()) {
                    error("No flint and steel in hotbar");
                    toggle();
                    return;
                }
                ignite(blocksToIgnite.get(0), itemResult);

                // Reset ticks
                igniteTick = 0;
            }
        }
        igniteTick++;

        // Placement
        if (place.get() && blocksToPlace.size() > 0) {
            if (placeTick > placeDelay.get()) {
                // Sort based on closest tnt
                blocksToPlace.sort(Comparator.comparingInt(o -> o.score));

                // Placement
                FindItemResult itemResult = InvUtils.findInHotbar(item -> item.getItem() == Items.TNT);
                if (!itemResult.found()) {
                    error("No tnt in hotbar");
                    toggle();
                    return;
                }
                place(blocksToPlace.get(0).blockPos, itemResult);

                // Reset ticks
                placeTick = 0;
            }
        }
        placeTick++;
    }

    private void ignite(BlockPos pos, FindItemResult item) {
        InvUtils.swap(item.slot(), true);

        mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Direction.UP, pos, true));

        InvUtils.swapBack();
    }

    private void place(BlockPos pos, FindItemResult item) {
        BlockUtils.place(pos, item, rotate.get(), 10);
    }

    private class TntPos {
        public BlockPos.Mutable blockPos;
        public int score;

        public TntPos set(BlockPos blockPos, int score) {
            if (this.blockPos != null)
                this.blockPos.set(blockPos);

            this.score = score;

            return this;
        }
    }
}
