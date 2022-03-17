package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.CollisionShapeEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;

import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShapes;

public class Prone extends Module {

    public enum Mode {
        WaterBucket,
		JustMaintain,
		Collision
	}

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("The mode used.")
        .defaultValue(Mode.WaterBucket)
        .build()
    );

    private Setting<Boolean> autoMaintain = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-maintain")
        .description("Switch to maintain mode when prone.")
        .defaultValue(true)
        .build()
    );

    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("blocks")
        .description("Selected blocks.")
        .build()
    );

    private int waterModeStage = 0;

    private List<BlockPos> waterModeTargets = Arrays.asList(
        new BlockPos(0, 0, 1),
        new BlockPos(0, 0, -1),
        new BlockPos(1, 0, 0),
        new BlockPos(-1, 0, 0)
    );

    public Prone() {
        super(MeteorRejectsAddon.CATEGORY, "prone", "Become prone on demand.");
    }

    @Override
    public void onDeactivate() {
        waterModeStage = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (autoMaintain.get() && mc.player.isInSwimmingPose() && !mc.player.isSubmergedInWater()) {
            BlockUtils.place(mc.player.getBlockPos().up(), InvUtils.find((itemstack) -> {return (itemstack.getItem() instanceof BlockItem blockitem && blocks.get().contains(blockitem.getBlock()));}), true, 1);
        }
        if (mode.get() == Mode.WaterBucket && mc.player.isInSwimmingPose() && waterModeStage > 0) {
            mc.options.forwardKey.setPressed(false);
            waterModeStage = 0;
        }
        if (mode.get() == Mode.WaterBucket && !mc.player.isInSwimmingPose()) {
            if (mc.player.isSubmergedInWater()) {
                mc.options.sprintKey.setPressed(true);
                waterModeStage += 1;
                if (waterModeStage > 2) {
                    mc.options.forwardKey.setPressed(true);
                }
            } else {
                FindItemResult result = InvUtils.findInHotbar(Items.WATER_BUCKET);
                if (!result.found()) {
                    waterModeStage = 0;
                }
                for (BlockPos offset : waterModeTargets) {
                    BlockPos target = mc.player.getBlockPos().add(offset);
                    if (mc.world.getBlockState(target).isFullCube(mc.world, target) && mc.world.getBlockState(target.up()).isAir()) {
                        useBucket(result, target);
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    private void onCollisionShape(CollisionShapeEvent event) {
        if (mode.get() != Mode.Collision) return;
        if (mc.world == null || mc.player == null || event.pos == null) return;
        if (event.state == null) return;
    
        if (event.pos.getY() != mc.player.getY() + 1) return;

        event.shape = VoxelShapes.fullCube();
    }

    private void useBucket(FindItemResult bucket, BlockPos target) {
        if (!bucket.found()) return;

        Rotations.rotate(Rotations.getYaw(target), Rotations.getPitch(target), 10, true, () -> {
            if (bucket.isOffhand()) {
                mc.interactionManager.interactItem(mc.player, mc.world, Hand.OFF_HAND);
            } else {
                InvUtils.swap(bucket.slot(), true);
                mc.interactionManager.interactItem(mc.player, mc.world, Hand.MAIN_HAND);
                InvUtils.swapBack();
            }

        });
    }
}
