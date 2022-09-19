package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BrewingStandBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class ChestAura extends Module {

    public enum CloseScreen {
        Always,
        IfEmpty,
        Never
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
            .name("rotate")
            .description("Rotates when placing")
            .defaultValue(true)
            .build()
    );

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
            .name("range")
            .description("The interact range.")
            .defaultValue(4)
            .min(0)
            .build()
    );

    private final Setting<List<Block>> blockTypes = sgGeneral.add(new BlockListSetting.Builder()
        .name("storage-blocks")
        .description("The blocks you open.")
        .filter(this::predicate)
        .defaultValue(Arrays.asList(Blocks.CHEST))
        .build()
    );

    private final Setting<Integer> bpt = sgGeneral.add(new IntSetting.Builder()
        .name("opens-per-tick")
        .description("How many blocks to open per tick")
        .defaultValue(2)
        .min(1)
        .sliderMax(8)
        .build()
    );

    private final Setting<Integer> forgetAfter = sgGeneral.add(new IntSetting.Builder()
        .name("forget-after")
        .description("How many ticks to wait before forgetting which chest to open. 0 for infinite")
        .defaultValue(0)
        .min(0)
        .sliderMax(1000)
        .build()
    );

    private final Setting<CloseScreen> closeScreen = sgGeneral.add(new EnumSetting.Builder<CloseScreen>()
        .name("close-screen")
        .defaultValue(CloseScreen.IfEmpty)
        .description("when to close the chest screen")
        .build()
    );

    private final Pool<BlockPos.Mutable> blockPosPool = new Pool<>(BlockPos.Mutable::new);
    private final List<BlockPos.Mutable> blocks = new ArrayList<>();
    private final List<BlockPos.Mutable> clickedBlocks = new ArrayList<>();

    int cap = 0;
    int forget = 0;

    public ChestAura() {
        super(MeteorRejectsAddon.CATEGORY, "chest-aura", "Automatically open chests in radius");
    }

    @Override
    public void onDeactivate() {
        for (BlockPos.Mutable blockPos : blocks) blockPosPool.free(blockPos);
        for (BlockPos.Mutable blockPos : clickedBlocks) blockPosPool.free(blockPos);
        blocks.clear();
        clickedBlocks.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {

        BlockIterator.register((int) Math.floor(range.get()), (int) Math.floor(range.get()), (blockPos, blockState) -> {
            if (!BlockUtils.canBreak(blockPos, blockState)) return;

            if (!(blockTypes.get().contains(blockState.getBlock()))) return;

            if (clickedBlocks.contains(blockPos)) return;

            blocks.add(blockPosPool.get().set(blockPos));
        });

        if (forgetAfter.get() > 0) {
            if (forget >= forgetAfter.get()) {
                forget = 0;
                for (BlockPos.Mutable blockPos : clickedBlocks) blockPosPool.free(blockPos);
                clickedBlocks.clear();
            }
        }
        
    }

    @EventHandler
    private void onTickPost(TickEvent.Post event) {
        if (mc.currentScreen == null) {
            for (BlockPos blockPos : blocks) {
                if (clickedBlocks.contains(blockPos)) continue;
                if (mc.currentScreen != null) return;
                if (cap >= bpt.get()) {
                    cap = 0;
                    return;
                }
                if (rotate.get()) {
                    Rotations.rotate(Rotations.getYaw(blockPos), Rotations.getPitch(blockPos), () -> {
                        click(blockPos);
                        clickedBlocks.add(blockPosPool.get().set(blockPos));
                    });
                } else {
                    click(blockPos);
                    clickedBlocks.add(blockPosPool.get().set(blockPos));
                }
                cap++;
            }
        } else if (mc.currentScreen instanceof GenericContainerScreen containerScreen) {
            closeIfEmpty(containerScreen.getScreenHandler());
        }

    }

    private boolean predicate(Block block) {
        return (
            block instanceof AbstractChestBlock ||
            block instanceof ShulkerBoxBlock ||
            block instanceof BarrelBlock ||
            block instanceof BrewingStandBlock ||
            block instanceof DispenserBlock ||
            block instanceof AbstractFurnaceBlock
        );
    }

    private void click(BlockPos pos) {
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(
            new Vec3d(pos.getX(), pos.getY(), pos.getZ()), 
            Direction.UP, 
            pos,
            false
        ));
    }

    private void closeIfEmpty(GenericContainerScreenHandler handler) {
        switch (closeScreen.get()) {
            case IfEmpty: {
                if (!handler.getInventory().containsAny(item -> {
                    return !item.isEmpty();
                })) {
                    if (!handler.getCursorStack().isEmpty()) return;
                    mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(handler.syncId));
                    mc.setScreen(null);
                }
                break;
            }

            case Always: {
                mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(handler.syncId));
                mc.setScreen(null);
                break;
            }
        
            default:
                break;
        }
        
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (!(event.screen instanceof GenericContainerScreen containerScreen)) return;
        closeIfEmpty(containerScreen.getScreenHandler());
    }

}
