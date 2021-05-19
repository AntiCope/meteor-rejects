package cloudburst.rejects.modules;

import cloudburst.rejects.MeteorRejectsAddon;
import meteordevelopment.orbit.EventHandler;
import minegame159.meteorclient.events.render.RenderEvent;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.systems.modules.Module;
import minegame159.meteorclient.rendering.Renderer;
import minegame159.meteorclient.rendering.ShapeMode;
import minegame159.meteorclient.settings.IntSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.utils.player.InvUtils;
import minegame159.meteorclient.utils.player.Rotations;
import minegame159.meteorclient.utils.render.color.SettingColor;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.RaycastContext;


public class Lavacast extends Module {

    private enum Stage {
        None,
        LavaDown,
        LavaUp,
        WaterDown,
        WaterUp
    }

    private int dist;
    private BlockPos placeFluidPos;
    private int tick;
    private Stage stage = Stage.None;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgShape = settings.createGroup("Shape", false);
    private final Setting<Integer> tickInterval = sgGeneral.add(new IntSetting.Builder()
            .name("tick-interval")
            .description("Interval")
            .defaultValue(2)
            .min(0)
            .sliderMax(20)
            .build()
    );

    private final Setting<Integer> distMin = sgShape.add(new IntSetting.Builder()
            .name("minimum-distance")
            .description("Top plane cutoff")
            .defaultValue(5)
            .min(0)
            .sliderMax(10)
            .build()
    );
    private final Setting<Integer> lavaDownMult = sgShape.add(new IntSetting.Builder()
            .name("lava-down-mulipiler")
            .description("Controlls the shape of the cast")
            .defaultValue(40)
            .min(1)
            .sliderMax(100)
            .build()
    );
    private final Setting<Integer> lavaUpMult = sgShape.add(new IntSetting.Builder()
            .name("lava-up-mulipiler")
            .description("Controlls the shape of the cast")
            .defaultValue(8)
            .min(1)
            .sliderMax(100)
            .build()
    );
    private final Setting<Integer> waterDownMult = sgShape.add(new IntSetting.Builder()
            .name("water-down-mulipiler")
            .description("Controlls the shape of the cast")
            .defaultValue(4)
            .min(1)
            .sliderMax(100)
            .build()
    );
    private final Setting<Integer> waterUpMult = sgShape.add(new IntSetting.Builder()
            .name("water-up-mulipiler")
            .description("Controlls the shape of the cast")
            .defaultValue(1)
            .min(1)
            .sliderMax(100)
            .build()
    );

    public Lavacast() {
        super(MeteorRejectsAddon.CATEGORY, "lavacast", "Automatically Lavacasts");
    }

    @Override
    public void onActivate() {
        if (mc.player == null || mc.world == null) toggle();
        tick = 0;
        stage = Stage.None;
        placeFluidPos = getTargetBlockPos();
        if (placeFluidPos == null) {
            placeFluidPos = mc.player.getBlockPos().down(2);
        } else {
            placeFluidPos = placeFluidPos.up();
        }
        dist=-1;
        getDistance(new Vec3i(1,0,0));
        getDistance(new Vec3i(-1,0,0));
        getDistance(new Vec3i(0,0,1));
        getDistance(new Vec3i(1,0,-1));
        if (dist<1) {
            error("Couldn't locate bottom.");
            toggle();
            return;
        }
        info("Distance: (highlight)%d(default).", dist);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
        tick++;
        if (shouldBreakOnTick()) return;
        if (dist < distMin.get()) toggle();
        tick = 0;
        if (checkMineBlock()) return;
        switch (stage) {
            case None: {
                Rotations.rotate(Rotations.getYaw(placeFluidPos),Rotations.getPitch(placeFluidPos),100, this::placeLava);
                stage = Stage.LavaDown;
                break;
            }
            case LavaDown: {
                Rotations.rotate(Rotations.getYaw(placeFluidPos),Rotations.getPitch(placeFluidPos),100, this::pickupLiquid);
                stage = Stage.LavaUp;
                break;
            }
            case LavaUp: {
                Rotations.rotate(Rotations.getYaw(placeFluidPos),Rotations.getPitch(placeFluidPos),100, this::placeWater);
                stage = Stage.WaterDown;
                break;
            }
            case WaterDown: {
                Rotations.rotate(Rotations.getYaw(placeFluidPos),Rotations.getPitch(placeFluidPos),100, this::pickupLiquid);
                stage = Stage.WaterUp;
                break;
            }
            case WaterUp: {
                dist--;
                Rotations.rotate(Rotations.getYaw(placeFluidPos),Rotations.getPitch(placeFluidPos),100, this::placeLava);
                stage = Stage.LavaDown;
                break;
            }
            default:
                break;
        }
    }

    private boolean shouldBreakOnTick() {
        if (stage == Stage.LavaDown && tick < dist*lavaDownMult.get()) return true;
        if (stage == Stage.LavaUp && tick < dist*lavaUpMult.get()) return true;
        if (stage == Stage.WaterDown && tick < dist*waterDownMult.get()) return true;
        if (stage == Stage.WaterUp && tick < dist*waterUpMult.get()) return true;
        if (tick < tickInterval.get()) return true;
        return false;
    }

    private boolean checkMineBlock() {
        if (stage == Stage.None && mc.world.getBlockState(placeFluidPos).getBlock() != Blocks.AIR) {
            Rotations.rotate(Rotations.getYaw(placeFluidPos), Rotations.getPitch(placeFluidPos), 100, this::updateBlockBreakingProgress);
            return true;
        }
        return false;
    }

    @EventHandler
    private void onRender(RenderEvent event) {
        if (placeFluidPos == null) return;
        double x1 = placeFluidPos.getX();
        double y1 = placeFluidPos.getY();
        double z1 = placeFluidPos.getZ();
        double x2 = x1+1;
        double y2 = y1+1;
        double z2 = z1+1;

        SettingColor color = new SettingColor(128, 128, 128);
        if (stage == Stage.LavaDown) color = new SettingColor(255, 180, 10);
        if (stage == Stage.LavaUp) color = new SettingColor(255, 180, 128);
        if (stage == Stage.WaterDown) color = new SettingColor(10, 10, 255);
        if (stage == Stage.WaterUp) color = new SettingColor(128, 128, 255);
        SettingColor color1 = color;
        color1.a = 75;

        Renderer.boxWithLines(Renderer.NORMAL, Renderer.LINES, x1, y1, z1, x2, y2, z2, color1, color, ShapeMode.Both, 0);
    }

    private void placeLava() {
        int slot = InvUtils.findItemInHotbar(Items.LAVA_BUCKET);
        if (slot == -1) {
            error("No lava bucket found.");
            toggle();
            return;
        }
        int prevSlot = mc.player.inventory.selectedSlot;
        mc.player.inventory.selectedSlot = slot;
        mc.interactionManager.interactItem(mc.player,mc.world,Hand.MAIN_HAND);
        mc.player.inventory.selectedSlot = prevSlot;
    }

    private void placeWater() {
        int slot = InvUtils.findItemInHotbar(Items.WATER_BUCKET);
        if (slot == -1) {
            error("No water bucket found.");
            toggle();
            return;
        }
        int prevSlot = mc.player.inventory.selectedSlot;
        mc.player.inventory.selectedSlot = slot;
        mc.interactionManager.interactItem(mc.player,mc.world,Hand.MAIN_HAND);
        mc.player.inventory.selectedSlot = prevSlot;
    }

    private void pickupLiquid() {
        int slot = InvUtils.findItemInHotbar(Items.BUCKET);
        if (slot == -1) {
            error("No bucket found.");
            toggle();
            return;
        }
        int prevSlot = mc.player.inventory.selectedSlot;
        mc.player.inventory.selectedSlot = slot;
        mc.interactionManager.interactItem(mc.player,mc.world,Hand.MAIN_HAND);
        mc.player.inventory.selectedSlot = prevSlot;
    }

    private void updateBlockBreakingProgress() {
        mc.interactionManager.updateBlockBreakingProgress(placeFluidPos,Direction.UP);
    }

    private BlockPos getTargetBlockPos() {
        HitResult blockHit = mc.crosshairTarget;
        if (blockHit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        return ((BlockHitResult) blockHit).getBlockPos();
    }

    private void getDistance(Vec3i offset) {
        BlockPos pos = placeFluidPos.down().add(offset);
        int new_dist;
        final BlockHitResult result = mc.world.raycast(new RaycastContext(
                Vec3d.ofCenter(pos), Vec3d.ofCenter(pos.down(250)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, mc.player
        ));
        if (result == null || result.getType() != HitResult.Type.BLOCK) {
            return;
        }
        new_dist = placeFluidPos.getY() - result.getBlockPos().getY();
        if (new_dist>dist) dist = new_dist;
    }

    @Override
    public String getInfoString() {
        return stage.toString();
    }
}
