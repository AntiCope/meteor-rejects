package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.mixininterface.IInventoryTweaks;
import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.InventoryEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.InventoryTweaks;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class ChestAura extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
            .name("rotate")
            .description("Rotates when opening.")
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

    private final Setting<List<BlockEntityType<?>>> blocks = sgGeneral.add(new StorageBlockListSetting.Builder()
            .name("blocks")
            .description("The blocks you open.")
            .defaultValue(BlockEntityType.CHEST)
            .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
            .name("delay")
            .description("Delay between opening chests.")
            .defaultValue(10)
            .sliderMax(20)
            .build()
    );

    private final Setting<Integer> forget = sgGeneral.add(new IntSetting.Builder()
            .name("forget-after")
            .description("How many ticks to wait before forgetting which chest to open. 0 for infinite.")
            .defaultValue(0)
            .min(0)
            .sliderMax(1000)
            .build()
    );

    private final Setting<CloseCondition> closeCondition = sgGeneral.add(new EnumSetting.Builder<CloseCondition>()
            .name("close-condition")
            .defaultValue(CloseCondition.IfEmpty)
            .description("When to close the chest screen.")
            .build()
    );

    private final Map<BlockPos, Integer> openedBlocks = new HashMap<>();
    private final CloseListener closeListener = new CloseListener();

    private int timer = 0;

    public ChestAura() {
        super(MeteorRejectsAddon.CATEGORY, "chest-aura", "Automatically open chests in radius");
    }

    @Override
    public void onActivate() {
        timer = 0;
        openedBlocks.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (forget.get() != 0) {
            for (Map.Entry<BlockPos, Integer> e : new HashMap<>(openedBlocks).entrySet()) {
                int time = e.getValue();
                if (time > forget.get()) openedBlocks.remove(e.getKey());
                else openedBlocks.replace(e.getKey(), time + 1);
            }
        }

        if (timer > 0 && mc.screen != null) return;

        for (BlockEntity block : Utils.blockEntities()) {
            if (!blocks.get().contains(block.getType())) continue;
            if (mc.player.getEyePosition().distanceTo(Vec3.atCenterOf(block.getBlockPos())) >= range.get()) continue;

            BlockPos pos = block.getBlockPos();
            if (openedBlocks.containsKey(pos)) continue;

            Runnable click = () -> mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(new Vec3(pos.getX(), pos.getY(), pos.getZ()), Direction.UP, pos, false));
            if (rotate.get()) Rotations.rotate(Rotations.getYaw(pos), Rotations.getPitch(pos), click);
            else click.run();

            // Double chest compatibility
            BlockState state = block.getBlockState();
            if (state.hasProperty(ChestBlock.TYPE)) {
                Direction direction = state.getValue(ChestBlock.FACING);
                switch (state.getValue(ChestBlock.TYPE)) {
                    case LEFT -> openedBlocks.put(pos.relative(direction.getClockWise()), 0);
                    case RIGHT -> openedBlocks.put(pos.relative(direction.getCounterClockWise()), 0);
                }
            }

            openedBlocks.put(pos, 0);
            timer = delay.get();
            MeteorClient.EVENT_BUS.subscribe(closeListener);
            break;
        }
        timer--;
    }

    public class CloseListener {
        @EventHandler(priority = EventPriority.HIGH)
        private void onInventory(InventoryEvent event) {
            AbstractContainerMenu handler = mc.player.containerMenu;
            if (event.packet.containerId() == handler.containerId) {
                switch (closeCondition.get()) {
                    case IfEmpty -> {
                        NonNullList<ItemStack> stacks = NonNullList.create();
                        IntStream.range(0, SlotUtils.indexToId(SlotUtils.MAIN_START)).mapToObj(handler.slots::get).map(Slot::getItem).forEach(stacks::add);
                        if (stacks.stream().allMatch(ItemStack::isEmpty)) mc.player.closeContainer();
                    }
                    case Always -> mc.player.closeContainer();
                    case AfterSteal ->
                            ((IInventoryTweaks) Modules.get().get(InventoryTweaks.class)).stealCallback(() -> mc.player.closeContainer());
                }
            }
            MeteorClient.EVENT_BUS.unsubscribe(this);
        }
    }

    public enum CloseCondition {
        Always,
        IfEmpty,
        AfterSteal,
        Never
    }
}
