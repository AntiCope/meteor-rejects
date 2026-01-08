package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import java.util.concurrent.atomic.AtomicInteger;

public class AutoExtinguish extends Module {
    private final SettingGroup sgGeneral = settings.createGroup("Extinguish Fire around you");
    private final SettingGroup sgBucket = settings.createGroup("Extinguish yourself");

    private final Setting<Boolean> extinguish = sgGeneral.add(new BoolSetting.Builder()
            .name("extinguish")
            .description("Automatically extinguishes fire around you.")
            .defaultValue(false)
            .build()
    );
    private final Setting<Integer> horizontalRadius = sgGeneral.add(new IntSetting.Builder()
            .name("horizontal-radius")
            .description("Horizontal radius in which to search for fire.")
            .defaultValue(4)
            .min(0)
            .sliderMax(6)
            .build()
    );

    private final Setting<Integer> verticalRadius = sgGeneral.add(new IntSetting.Builder()
            .name("vertical-radius")
            .description("Vertical radius in which to search for fire.")
            .defaultValue(4)
            .min(0)
            .sliderMax(6)
            .build()
    );
    private final Setting<Integer> maxBlockPerTick = sgGeneral.add(new IntSetting.Builder()
            .name("block-per-tick")
            .description("Maximum amount of Blocks to extinguish per tick.")
            .defaultValue(5)
            .min(1)
            .sliderMax(50)
            .build()
    );
    private final Setting<Boolean> waterBucket = sgBucket.add(new BoolSetting.Builder()
            .name("water")
            .description("Automatically places water when you are on fire (and don't have fire resistance).")
            .defaultValue(false)
            .build()
    );
    private final Setting<Boolean> center = sgBucket.add(new BoolSetting.Builder()
            .name("center")
            .description("Automatically centers you when placing water.")
            .defaultValue(false)
            .build()
    );
    private final Setting<Boolean> onGround = sgBucket.add(new BoolSetting.Builder()
            .name("on-ground")
            .description("Only place when you are on ground.")
            .defaultValue(false)
            .build()
    );

    private boolean hasPlacedWater = false;
    private BlockPos blockPos = null;
    private boolean doesWaterBucketWork = true;

    private static final MobEffect FIRE_RESISTANCE = BuiltInRegistries.MOB_EFFECT.getValue(Identifier.parse("fire_resistance"));

    public AutoExtinguish() {
        super(MeteorRejectsAddon.CATEGORY, "auto-extinguish", "Automatically extinguishes fire around you");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.level.dimensionType().attributes().applyModifier(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false)) {
            if (doesWaterBucketWork) {
                warning("Water Buckets don't work in this dimension!");
                doesWaterBucketWork = false;

            }
        } else {
            if (!doesWaterBucketWork) {
                warning("Enabled Water Buckets!");
                doesWaterBucketWork = true;
            }
        }
        if (onGround.get() && !mc.player.onGround()) {
            return;
        }

        if (waterBucket.get() && doesWaterBucketWork) {
            if (hasPlacedWater) {
                final int slot = findSlot(Items.BUCKET);
                blockPos = mc.player.blockPosition();
                place(slot);
                hasPlacedWater = false;

            } else if (!mc.player.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(FIRE_RESISTANCE)) && mc.player.isOnFire()) {
                blockPos = mc.player.blockPosition();
                final int slot = findSlot(Items.WATER_BUCKET);
                if (mc.level.getBlockState(blockPos).getBlock() == Blocks.FIRE || mc.level.getBlockState(blockPos).getBlock() == Blocks.SOUL_FIRE) {
                    float yaw = mc.gameRenderer.getMainCamera().yRot() % 360;
                    float pitch = mc.gameRenderer.getMainCamera().xRot() % 360;
                    if (center.get()) {
                        PlayerUtils.centerPlayer();
                    }
                    Rotations.rotate(yaw, 90);
                    mc.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.UP));
                    mc.player.swing(InteractionHand.MAIN_HAND);
                    mc.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));

                    Rotations.rotate(yaw, pitch);
                }
                place(slot);
                hasPlacedWater = true;
            }
        }

        if (extinguish.get()) {
            AtomicInteger blocksPerTick = new AtomicInteger();
            BlockIterator.register(horizontalRadius.get(), verticalRadius.get(), (blockPos, blockState) -> {
                if (blocksPerTick.get() <= maxBlockPerTick.get()) {
                    if (blockState.getBlock() == Blocks.FIRE || mc.level.getBlockState(blockPos).getBlock() == Blocks.SOUL_FIRE) {
                        extinguishFire(blockPos);
                        blocksPerTick.getAndIncrement();
                    }
                }
            });
        }
    }

    private void place(int slot) {
        if (slot != -1) {
            final int preSlot = mc.player.getInventory().getSelectedSlot();
            if (center.get()) {
                PlayerUtils.centerPlayer();
            }
            mc.player.getInventory().setSelectedSlot(slot);
            float yaw = mc.gameRenderer.getMainCamera().yRot() % 360;
            float pitch = mc.gameRenderer.getMainCamera().xRot() % 360;

            Rotations.rotate(yaw, 90);
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            mc.player.getInventory().setSelectedSlot(preSlot);
            Rotations.rotate(yaw, pitch);

        }
    }

    private void extinguishFire(BlockPos blockPos) {
        mc.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, blockPos, net.minecraft.core.Direction.UP));
        mc.player.swing(InteractionHand.MAIN_HAND);
        mc.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, blockPos, net.minecraft.core.Direction.UP));
    }

    private int findSlot(Item item) {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack block = mc.player.getInventory().getItem(i);
            if (block.getItem() == item) {
                slot = i;
                break;
            }
        }

        return slot;
    }

}
