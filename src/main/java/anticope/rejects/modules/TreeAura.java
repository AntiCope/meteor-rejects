package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.utils.WorldUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.CardinalDirection;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.phys.BlockHitResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

public class TreeAura extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> rotation = sgGeneral.add(new BoolSetting.Builder().name("rotate").description("rotate for block interactions").defaultValue(false).build());
    private final Setting<Integer> plantDelay = sgGeneral.add(new IntSetting.Builder().name("plant-delay").description("delay between planting trees").defaultValue(6).min(0).sliderMax(25).build());
    private final Setting<Integer> bonemealDelay = sgGeneral.add(new IntSetting.Builder().name("bonemeal-delay").description("delay between placing bonemeal on trees").defaultValue(3).min(0).sliderMax(25).build());
    private final Setting<Integer> rRange = sgGeneral.add(new IntSetting.Builder().name("radius").description("how far you can place horizontally").defaultValue(4).min(1).sliderMax(5).build());
    private final Setting<Integer> yRange = sgGeneral.add(new IntSetting.Builder().name("y-range").description("how far you can place vertically").defaultValue(3).min(1).sliderMax(5).build());
    private final Setting<SortMode> sortMode = sgGeneral.add(new EnumSetting.Builder<SortMode>().name("sort-mode").description("how to sort nearby trees/placements.").defaultValue(SortMode.Farthest).build());

    private int bonemealTimer, plantTimer;


    public TreeAura() { // CopeTypes
        super(MeteorRejectsAddon.CATEGORY, "tree-aura", "Plants trees around you");
    }

    @Override
    public void onActivate() {
        bonemealTimer = 0;
        plantTimer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {

        plantTimer--;
        bonemealTimer--;

        if (plantTimer <= 0) {
            BlockPos plantPos = findPlantLocation();
            if (plantPos == null) return;
            doPlant(plantPos);
            plantTimer = plantDelay.get();
        }

        if (bonemealTimer <= 0) {
            BlockPos p = findPlantedSapling();
            if (p == null) return;
            doBonemeal(p);
            bonemealTimer = bonemealDelay.get();
        }
    }


    private FindItemResult findBonemeal() {
        return InvUtils.findInHotbar(Items.BONE_MEAL);
    }

    private FindItemResult findSapling() {
        return InvUtils.findInHotbar(itemStack -> Block.byItem(itemStack.getItem()) instanceof SaplingBlock);
    }

    private boolean isSapling(BlockPos pos) {
        return mc.level.getBlockState(pos).getBlock() instanceof SaplingBlock;
    }

    private void doPlant(BlockPos plantPos) {
        FindItemResult sapling = findSapling();
        if (!sapling.found()) {
            error("No saplings in hotbar");
            toggle();
            return;
        }
        InvUtils.swap(sapling.slot(), false);
        if (rotation.get())
            Rotations.rotate(Rotations.getYaw(plantPos), Rotations.getPitch(plantPos), () -> mc.player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, new BlockHitResult(Utils.vec3d(plantPos), Direction.UP, plantPos, false), 0)));
        else
            mc.player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, new BlockHitResult(Utils.vec3d(plantPos), Direction.UP, plantPos, false), 0));
    }

    private void doBonemeal(BlockPos sapling) {
        FindItemResult bonemeal = findBonemeal();
        if (!bonemeal.found()) {
            error("No bonemeal in hotbar");
            toggle();
            return;
        }
        InvUtils.swap(bonemeal.slot(), false);
        if (rotation.get())
            Rotations.rotate(Rotations.getYaw(sapling), Rotations.getPitch(sapling), () -> mc.player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, new BlockHitResult(Utils.vec3d(sapling), Direction.UP, sapling, false), 0)));
        else
            mc.player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, new BlockHitResult(Utils.vec3d(sapling), Direction.UP, sapling, false), 0));
    }

    private boolean canPlant(BlockPos pos) {
        Block b = mc.level.getBlockState(pos).getBlock();
        if (b.equals(Blocks.SHORT_GRASS) || b.equals(Blocks.GRASS_BLOCK) || b.equals(Blocks.DIRT) || b.equals(Blocks.COARSE_DIRT)) {
            final AtomicBoolean plant = new AtomicBoolean(true);
            IntStream.rangeClosed(1, 5).forEach(i -> {
                // Check above
                BlockPos check = pos.above(i);
                if (!mc.level.getBlockState(check).getBlock().equals(Blocks.AIR)) {
                    plant.set(false);
                    return;
                }
                // Check around
                for (CardinalDirection dir : CardinalDirection.values()) {
                    if (!mc.level.getBlockState(check.relative(dir.toDirection(), i)).getBlock().equals(Blocks.AIR)) {
                        plant.set(false);
                        return;
                    }
                }
            });
            return plant.get();
        }
        return false;
    }

    private List<BlockPos> findSaplings(BlockPos centerPos, int radius, int height) {
        ArrayList<BlockPos> blocc = new ArrayList<>();
        List<BlockPos> blocks = WorldUtils.getSphere(centerPos, radius, height);
        for (BlockPos b : blocks) if (isSapling(b)) blocc.add(b);
        return blocc;
    }

    private BlockPos findPlantedSapling() {
        List<BlockPos> saplings = findSaplings(mc.player.blockPosition(), rRange.get(), yRange.get());
        if (saplings.isEmpty()) return null;
        saplings.sort(Comparator.comparingDouble(PlayerUtils::distanceTo));
        if (sortMode.get().equals(SortMode.Farthest)) Collections.reverse(saplings);
        return saplings.get(0);
    }

    private List<BlockPos> getPlantLocations(BlockPos centerPos, int radius, int height) {
        ArrayList<BlockPos> blocc = new ArrayList<>();
        List<BlockPos> blocks = WorldUtils.getSphere(centerPos, radius, height);
        for (BlockPos b : blocks) if (canPlant(b)) blocc.add(b);
        return blocc;
    }

    private BlockPos findPlantLocation() {
        List<BlockPos> nearby = getPlantLocations(mc.player.blockPosition(), rRange.get(), yRange.get());
        if (nearby.isEmpty()) return null;
        nearby.sort(Comparator.comparingDouble(PlayerUtils::distanceTo));
        if (sortMode.get().equals(SortMode.Farthest)) Collections.reverse(nearby);
        return nearby.get(0);
    }

    private double distanceBetween(BlockPos pos1, BlockPos pos2) {
        double d = pos1.getX() - pos2.getX();
        double e = pos1.getY() - pos2.getY();
        double f = pos1.getZ() - pos2.getZ();
        return Mth.sqrt((float) (d * d + e * e + f * f));
    }

    public enum SortMode {Closest, Farthest}

}