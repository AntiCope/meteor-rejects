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
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.ChestType;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final Map<BlockPos, Integer> opened = new HashMap<>();
    private final CloseListener closeListener = new CloseListener();
    private final List<BlockEntityType<?>> blocks = Arrays.asList(BlockEntityType.CHEST, BlockEntityType.TRAPPED_CHEST, BlockEntityType.SHULKER_BOX);

    private int timer = 0;

    public ChestAura() {
        super(MeteorRejectsAddon.CATEGORY, "chest-aura", "Automatically open chests in radius");
    }

    @Override
    public void onActivate() {
        timer = 0;
        opened.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (forget.get() != 0) {
            for (Map.Entry<BlockPos, Integer> e : new HashMap<>(opened).entrySet()) {
                int time = e.getValue();
                if (time > forget.get()) opened.remove(e.getKey());
                else opened.replace(e.getKey(), time + 1);
            }
        }

        if (timer > 0 && mc.currentScreen != null) return;

        for (BlockEntity block : Utils.blockEntities()) {
            if (!blocks.contains(block.getType())) continue;
            if (mc.player.getEyePos().distanceTo(Vec3d.ofCenter(block.getPos())) >= range.get()) continue;

            BlockPos pos = block.getPos();
            if (opened.containsKey(pos)) continue;

            Runnable click = () -> mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), Direction.UP, pos, false));
            if (rotate.get()) Rotations.rotate(Rotations.getYaw(pos), Rotations.getPitch(pos), click);
            else click.run();

            // Double chest compatibility
            BlockState state = block.getCachedState();
            ChestType chestType = state.contains(ChestBlock.CHEST_TYPE) ? state.get(ChestBlock.CHEST_TYPE) : ChestType.SINGLE;
            if (chestType != ChestType.SINGLE) {
                Direction direction = state.get(ChestBlock.FACING);
                switch (chestType) {
                    case LEFT -> opened.put(pos.offset(direction.rotateYClockwise()), 0);
                    case RIGHT -> opened.put(pos.offset(direction.rotateYCounterclockwise()), 0);
                }
            }

            opened.put(pos, 0);
            timer = delay.get();
            MeteorClient.EVENT_BUS.subscribe(closeListener);
            break;
        }
        timer--;
    }

    public class CloseListener {
        @EventHandler(priority = EventPriority.HIGH)
        private void onInventory(InventoryEvent event) {
            ScreenHandler handler = mc.player.currentScreenHandler;
            if (event.packet.getSyncId() == handler.syncId) {
                if (handler instanceof GenericContainerScreenHandler || handler instanceof ShulkerBoxScreenHandler) {
                    switch (closeCondition.get()) {
                        case IfEmpty -> {
                            if (handler.getStacks().stream().allMatch(ItemStack::isEmpty))
                                mc.player.closeHandledScreen();
                        }
                        case Always -> mc.player.closeHandledScreen();
                        case AfterSteal -> {
                            IInventoryTweaks invTweaks = (IInventoryTweaks) Modules.get().get(InventoryTweaks.class);
                            if (invTweaks.autoSteal())
                                invTweaks.afterSteal(() -> RenderSystem.recordRenderCall(() -> mc.player.closeHandledScreen()));
                        }
                    }
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
