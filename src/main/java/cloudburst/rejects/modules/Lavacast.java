package cloudburst.rejects.modules;

import meteordevelopment.orbit.EventHandler;
import minegame159.meteorclient.events.render.RenderEvent;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.modules.Categories;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.rendering.Renderer;
import minegame159.meteorclient.rendering.ShapeMode;
import minegame159.meteorclient.settings.EnumSetting;
import minegame159.meteorclient.settings.IntSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.utils.player.ChatUtils;
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
        super(Categories.World, "lavacast", "Automatically Lavacasts");
    }

    @Override
    public void onActivate() {
        if (mc.player == null || mc.world == null) toggle();
        tick = 0;
        stage = Stage.None;
        placeFluidPos = getTargetBlockPos().up();
        if (placeFluidPos == null) {
            placeFluidPos = mc.player.getBlockPos().down();
        }
        final BlockHitResult result = mc.world.raycast(new RaycastContext(
                Vec3d.ofCenter(offsetByPlayerRotation(placeFluidPos.down())), Vec3d.ofCenter(offsetByPlayerRotation(placeFluidPos).down(250)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, mc.player
        ));
        if (result == null || result.getType() != HitResult.Type.BLOCK) {
            ChatUtils.moduleError(this,"No floor beneath you");
            toggle();
            return;
        }
        dist = placeFluidPos.getY() - result.getBlockPos().getY();
        ChatUtils.moduleInfo(this,"Distance: (highlight)%d(default).", dist);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
        tick++;
        if (stage == Stage.LavaDown && tick < dist*lavaDownMult.get()) return;
        if (stage == Stage.LavaUp && tick < dist*lavaUpMult.get()) return;
        if (stage == Stage.WaterDown && tick < dist*waterDownMult.get()) return;
        if (stage == Stage.WaterUp && tick < dist*waterUpMult.get()) return;
        if (dist < distMin.get()) toggle();
        if (tick < tickInterval.get()) {
            return;
        }
        tick = 0;
        if (stage == Stage.None && mc.world.getBlockState(placeFluidPos).getBlock() != Blocks.AIR) {
            Rotations.rotate(Rotations.getYaw(placeFluidPos), Rotations.getPitch(placeFluidPos), 100, this::updateBlockBreakingProgress);
            return;
        }
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
        }
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
            ChatUtils.moduleError(this,"No lava bucket found.");
            toggle();
            return;
        }
        int prevSlot = mc.player.inventory.selectedSlot;
        InvUtils.swap(slot);
        mc.interactionManager.interactItem(mc.player,mc.world,Hand.MAIN_HAND);
        InvUtils.swap(prevSlot);
    }

    private void placeWater() {
        int slot = InvUtils.findItemInHotbar(Items.WATER_BUCKET);
        if (slot == -1) {
            ChatUtils.moduleError(this,"No water bucket found.");
            toggle();
            return;
        }
        int prevSlot = mc.player.inventory.selectedSlot;
        InvUtils.swap(slot);
        mc.interactionManager.interactItem(mc.player,mc.world,Hand.MAIN_HAND);
        InvUtils.swap(prevSlot);
    }

    private void pickupLiquid() {
        int slot = InvUtils.findItemInHotbar(Items.BUCKET);
        if (slot == -1) {
            ChatUtils.moduleError(this,"No bucket found.");
            toggle();
            return;
        }
        int prevSlot = mc.player.inventory.selectedSlot;
        InvUtils.swap(slot);
        mc.interactionManager.interactItem(mc.player,mc.world,Hand.MAIN_HAND);
        InvUtils.swap(prevSlot);
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

    private BlockPos offsetByPlayerRotation(BlockPos pos) {
        double rotation = (mc.player.yaw - 90) % 360;
        if (rotation < 0) rotation += 360.0;
        if (0 <= rotation && rotation < 22.5) return pos.south();
        else if (22.5 <= rotation && rotation < 67.5) return pos.south().west();
        else if (67.5 <= rotation && rotation < 112.5) return pos.west();
        else if (112.5 <= rotation && rotation < 157.5) return pos.north().west();
        else if (157.5 <= rotation && rotation < 202.5) return pos.north();
        else if (202.5 <= rotation && rotation < 247.5) return pos.north().east();
        else if (247.5 <= rotation && rotation < 292.5) return pos.east();
        else if (292.5 <= rotation && rotation < 337.5) return pos.south().east();
        else if (337.5 <= rotation && rotation < 360.0) return pos.south();
        return pos;
    }

}
