package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
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
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AutoTNT extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // General

    private final Setting<Boolean> ignite = sgGeneral.add(new BoolSetting.Builder()
            .name("ignite")
            .description("Whether to ignite tnt.")
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

    private final Setting<Integer> horizontalRange = sgGeneral.add(new IntSetting.Builder()
            .name("horizontal-range")
            .description("Horizontal range of ignition")
            .defaultValue(4)
            .build()
    );

    private final Setting<Integer> verticalRange = sgGeneral.add(new IntSetting.Builder()
            .name("vertical-range")
            .description("Vertical range of ignition")
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

    private final List<BlockPos.MutableBlockPos> blocksToIgnite = new ArrayList<>();
    private final Pool<BlockPos.MutableBlockPos> ignitePool = new Pool<>(BlockPos.MutableBlockPos::new);
    private int igniteTick = 0;

    public AutoTNT() {
        super(MeteorRejectsAddon.CATEGORY, "auto-tnt", "Ignites tnt automatically. Good for griefing.");
    }

    @Override
    public void onDeactivate() {
        igniteTick = 0;
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        if (ignite.get() && igniteTick > igniteDelay.get()) {
            // Clear blocks
            for (BlockPos.MutableBlockPos blockPos : blocksToIgnite) ignitePool.free(blockPos);
            blocksToIgnite.clear();

            // Register
            BlockIterator.register(horizontalRange.get(), verticalRange.get(), (blockPos, blockState) -> {
                if (blockState.getBlock() instanceof TntBlock) blocksToIgnite.add(ignitePool.get().set(blockPos));
            });
        }
    }

    @EventHandler
    private void onPostTick(TickEvent.Post event) {
        // Ignition
        if (ignite.get() && !blocksToIgnite.isEmpty()) {
            if (igniteTick > igniteDelay.get()) {
                // Sort based on closest tnt
                blocksToIgnite.sort(Comparator.comparingDouble(PlayerUtils::distanceTo));

                // Ignition
                FindItemResult itemResult = InvUtils.findInHotbar(item -> {
                    if (item.getItem() instanceof FlintAndSteelItem) {
                        return (antiBreak.get() && (item.getMaxDamage() - item.getDamageValue()) > 10);
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
    }

    private void ignite(BlockPos pos, FindItemResult item) {
        InvUtils.swap(item.slot(), true);

        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Direction.UP, pos, true));

        InvUtils.swapBack();
    }
}
