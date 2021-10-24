package anticope.rejects.modules;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import anticope.rejects.MeteorRejectsAddon;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class BedrockWalk extends Module {
        private final SettingGroup sgGeneral = settings.getDefaultGroup();

        private final Setting<Double> activationWindow = sgGeneral.add(new DoubleSetting.Builder()
        .name("activation-window")
        .description("The area above the target Y level at which pull activates.")
        .min(0.2D)
        .max(5.0D)
        .sliderMin(0.2D)
        .sliderMax(5.0D)
        .defaultValue(0.5D)
        .build()
    );

    
    private final Setting<Integer> driftToHeight = sgGeneral.add(new IntSetting.Builder()
        .name("drift-to-height")
        .description("Y level to find blocks to drift onto.")
        .min(0)
        .max(256)
        .sliderMin(0)
        .sliderMax(256)
        .defaultValue(5)
        .build()
    );

    
    private final Setting<Double> horizontalPullStrength = sgGeneral.add(new DoubleSetting.Builder()
        .name("horizontal-pull")
        .description("The horizontal speed/strength at which you drift to the goal block.")
        .min(0.1D)
        .max(10.0D)
        .sliderMin(0.1D)
        .sliderMax(10.0D)
        .defaultValue(1.0D)
        .build()
        );

    
    private final Setting<Double> verticalPullStrength = sgGeneral.add(new DoubleSetting.Builder()
        .name("vertical-pull")
        .description("The vertical speed/strength at which you drift to the goal block.")
        .min(0.1D)
        .max(10.0D)
        .sliderMin(0.1D)
        .sliderMax(10.0D)
        .defaultValue(1.0D)
        .build()
    );

    
    private final Setting<Integer> searchRadius = sgGeneral.add(new IntSetting.Builder()
        .name("search-radius")
        .description("The radius at which tanuki mode searches for blocks (odd numbers only).")
        .min(3)
        .max(15)
        .sliderMin(3)
        .sliderMax(15)
        .defaultValue(3)
        .build()
    );

    
    private final Setting<Boolean> updatePositionFailsafe = sgGeneral.add(new BoolSetting.Builder()
        .name("failsafe")
        .description("Updates your position to the top of the target block if you miss the jump.")
        .defaultValue(true)
        .build()
    );

    
    private final Setting<Double> failsafeWindow = sgGeneral.add(new DoubleSetting.Builder()
        .name("failsafe-window")
        .description("Window below the target block to fall to trigger failsafe.")
        .min(0.01D)
        .max(1.0D)
        .sliderMin(0.01D)
        .sliderMax(1.0D)
        .defaultValue(0.1D)
        .build()
    );

    
    private final Setting<Double> successfulLandingMargin = sgGeneral.add(new DoubleSetting.Builder()
        .name("landing-margin")
        .description("The distance from a landing block to be considered a successful landing.")
        .min(0.01D)
        .max(10.0D)
        .sliderMin(0.01D)
        .sliderMax(10.0D)
        .defaultValue(1.0D)
        .build()
    ); 

    private final BlockPos.Mutable blockPos = new BlockPos.Mutable(0, 0, 0);
    private final ArrayList<BlockPos> validBlocks = new ArrayList<>();
    private final TreeMap<Double, BlockPos> sortedBlocks = new TreeMap<>();
    private final BlockPos.Mutable playerHorizontalPos = new BlockPos.Mutable();
    private boolean successfulLanding;
   

    public BedrockWalk() {
        super(MeteorRejectsAddon.CATEGORY, "bedrock-walk", "Makes moving on bedrock easier.");
    }

    @Override
    public void onActivate() {
        if (this.searchRadius.get() % 2 == 0) {
            info("%d is not valid for radius, rounding up", this.searchRadius.get());
            searchRadius.set(searchRadius.get() + 1);
        } 
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player.getY() > driftToHeight.get() + activationWindow.get()) return;
        Vec3d targetPos = findNearestBlock(mc.player.getX(), driftToHeight.get() -1, mc.player.getZ());
        if (targetPos == null) return;
        if (mc.player.getY() == targetPos.getY() + 1.0D) return;
        if (mc.options.keyJump.isPressed()) return;
        if (updatePositionFailsafe.get() && !successfulLanding && mc.player.getY() < (driftToHeight.get() -  failsafeWindow.get())) {
            mc.player.setPos(targetPos.getX(), targetPos.getY() + 1.0D, targetPos.getZ());
        }
        Vec3d normalizedDirection = targetPos.subtract(mc.player.getPos()).normalize();
        Vec3d velocity = mc.player.getVelocity();
        ((IVec3d)mc.player.getVelocity()).set(
            velocity.x + normalizedDirection.x * horizontalPullStrength.get() * mc.getTickDelta(),
            velocity.y + normalizedDirection.y * verticalPullStrength.get() * mc.getTickDelta(),
            velocity.z + normalizedDirection.z * horizontalPullStrength.get() * mc.getTickDelta()
        );

        successfulLanding = mc.player.getPos().isInRange(targetPos, successfulLandingMargin.get());
    }

    private Vec3d findNearestBlock(double x, int y, double z) {
        validBlocks.clear();
        sortedBlocks.clear();

        playerHorizontalPos.set(x, y, z);

        int rad = searchRadius.get();
        for (int ix = 0; ix < rad; ix++) {
            for (int iy = 0; iy < rad; iy++) {
                BlockState block = mc.world.getBlockState(blockPos.set(x - ((rad - 1) / 2 - ix), y, x - ((rad - 1) / 2 - iy)));
                if (!block.isAir() &&!(block.getBlock() instanceof FluidBlock)) {
                    validBlocks.add(blockPos.mutableCopy());
                }
            }
        }

        validBlocks.forEach(blockPos -> {
            sortedBlocks.put(blockPos.getSquaredDistance(x, y, z, true), blockPos);
        });

        Map.Entry<Double, BlockPos> firstEntry = sortedBlocks.firstEntry();

        if (firstEntry == null) return null;

        return Vec3d.ofBottomCenter(firstEntry.getValue());
    }
}
