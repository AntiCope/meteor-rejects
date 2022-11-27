package anticope.rejects.modules;

import anticope.rejects.utils.WorldUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.MinecraftClientAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TillAura extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
            .name("range")
            .description("How far Tillaura will reach to till blocks.")
            .defaultValue(5)
            .min(0)
            .build()
    );

    private final Setting<Boolean> multiTill = sgGeneral.add(new BoolSetting.Builder().
            name("multi-till")
            .description("Tills multiple blocks at once. Faster, but can't bypass NoCheat+.")
            .defaultValue(false)
            .build()
    );
    private final Setting<Boolean> checkLOS = sgGeneral.add(new BoolSetting.Builder().
            name("check-line-of-sight")
            .description("Prevents Tillaura from reaching through blocks. Good for NoCheat+ servers, but unnecessary in vanilla.")
            .defaultValue(true)
            .build()
    );

    private final List<Block> tillableBlocks = Arrays.asList(Blocks.GRASS_BLOCK,
            Blocks.DIRT_PATH, Blocks.DIRT, Blocks.COARSE_DIRT);

    public TillAura() {
        super(Categories.World, "till-aura", "Automatically turns dirt, grass, etc. into farmland.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        // wait for right click timer
        if (((MinecraftClientAccessor) mc).getItemUseCooldown() > 0)
            return;

        // check held item
        ItemStack stack = mc.player.getInventory().getMainHandStack();
        if (stack.isEmpty() || !(stack.getItem() instanceof HoeItem))
            return;

        // get valid blocks
        ArrayList<BlockPos> validBlocks = getValidBlocks(range.get(), this::isCorrectBlock);

        if (multiTill.get()) {
            boolean shouldSwing = false;

            // till all valid blocks
            for (BlockPos pos : validBlocks)
                if (rightClickBlockSimple(pos))
                    shouldSwing = true;

            // swing arm
            if (shouldSwing)
                mc.player.swingHand(Hand.MAIN_HAND);
        } else
            // till next valid block
            for (BlockPos pos : validBlocks)
                if (rightClickBlockLegit(pos))
                    break;
    }

    private Vec3d getEyesPos() {
        ClientPlayerEntity player = mc.player;

        return new Vec3d(player.getX(),
                player.getY() + player.getEyeHeight(player.getPose()),
                player.getZ());
    }

    private ArrayList<BlockPos> getValidBlocks(double range,
                                               Predicate<BlockPos> validator) {
        Vec3d eyesVec = getEyesPos().subtract(0.5, 0.5, 0.5);
        double rangeSq = Math.pow(range + 0.5, 2);
        int rangeI = (int) Math.ceil(range);

        BlockPos center = new BlockPos(getEyesPos());
        BlockPos min = center.add(-rangeI, -rangeI, -rangeI);
        BlockPos max = center.add(rangeI, rangeI, rangeI);

        return WorldUtils.getAllInBox(min, max).stream()
                .filter(pos -> eyesVec.squaredDistanceTo(Vec3d.of(pos)) <= rangeSq)
                .filter(validator)
                .sorted(Comparator.comparingDouble(
                        pos -> eyesVec.squaredDistanceTo(Vec3d.of(pos))))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private boolean isCorrectBlock(BlockPos pos) {
        if (!tillableBlocks.contains(mc.world.getBlockState(pos).getBlock()))
            return false;

        return mc.world.getBlockState(pos.up()).isAir();
    }

    private boolean rightClickBlockLegit(BlockPos pos) {
        Vec3d eyesPos = getEyesPos();
        Vec3d posVec = Vec3d.ofCenter(pos);
        double distanceSqPosVec = eyesPos.squaredDistanceTo(posVec);
        double rangeSq = Math.pow(range.get(), 2);

        for (Direction side : Direction.values()) {
            Vec3d hitVec = posVec.add(Vec3d.of(side.getVector()).multiply(0.5));
            double distanceSqHitVec = eyesPos.squaredDistanceTo(hitVec);

            // check if hitVec is within range
            if (distanceSqHitVec > rangeSq)
                continue;

            // check if side is facing towards player
            if (distanceSqHitVec >= distanceSqPosVec)
                continue;

            if (checkLOS.get() && !hasLineOfSight(eyesPos, hitVec))
                continue;

            // face block
            Rotations.rotate(Rotations.getYaw(hitVec), Rotations.getPitch(hitVec));

            // right click block
            rightClickBlock(pos, side, hitVec);
            mc.player.swingHand(Hand.MAIN_HAND);
            ((MinecraftClientAccessor) mc).setItemUseCooldown(4);
            return true;
        }

        return false;
    }

    private void rightClickBlock(BlockPos pos, Direction side, Vec3d hitVec) {
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
                new BlockHitResult(hitVec, side, pos, false));
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
    }

    private boolean rightClickBlockSimple(BlockPos pos) {
        Vec3d eyesPos = getEyesPos();
        Vec3d posVec = Vec3d.ofCenter(pos);
        double distanceSqPosVec = eyesPos.squaredDistanceTo(posVec);
        double rangeSq = Math.pow(range.get(), 2);

        for (Direction side : Direction.values()) {
            Vec3d hitVec = posVec.add(Vec3d.of(side.getVector()).multiply(0.5));
            double distanceSqHitVec = eyesPos.squaredDistanceTo(hitVec);

            // check if hitVec is within range
            if (distanceSqHitVec > rangeSq)
                continue;

            // check if side is facing towards player
            if (distanceSqHitVec >= distanceSqPosVec)
                continue;

            if (checkLOS.get() && !hasLineOfSight(eyesPos, hitVec))
                continue;

            rightClickBlock(pos, side, hitVec);
            return true;
        }

        return false;
    }

    private boolean hasLineOfSight(Vec3d from, Vec3d to) {
        ShapeType type = RaycastContext.ShapeType.COLLIDER;
        FluidHandling fluid = RaycastContext.FluidHandling.NONE;

        RaycastContext context =
                new RaycastContext(from, to, type, fluid, mc.player);

        return mc.world.raycast(context).getType() == HitResult.Type.MISS;
    }
}
