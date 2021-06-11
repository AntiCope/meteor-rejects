package cloudburst.rejects.modules;

import java.util.Random;

import cloudburst.rejects.MeteorRejectsAddon;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import meteordevelopment.orbit.EventHandler;
import minegame159.meteorclient.events.render.Render3DEvent;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.EnumSetting;
import minegame159.meteorclient.settings.IntSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.systems.modules.Module;
import minegame159.meteorclient.utils.render.RenderUtils;
import minegame159.meteorclient.utils.render.color.Color;

public class Confuse extends Module {

    public enum Mode {
        RandomTP,
        Switch,
        Circle
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .defaultValue(Mode.RandomTP)
        .description("Mode")
        .build()
    );
    private final Setting<Integer> delay  = sgGeneral.add(new IntSetting.Builder()
            .name("delay")
            .description("Delay")
            .defaultValue(3)
            .min(0)
            .sliderMax(20)
            .build()
    );
    private final Setting<Integer> circleSpeed  = sgGeneral.add(new IntSetting.Builder()
            .name("circle-speed")
            .description("Circle mode speed")
            .defaultValue(10)
            .min(1)
            .sliderMax(180)
            .build()
    );
    private final Setting<Boolean> moveThroughBlocks = sgGeneral.add(new BoolSetting.Builder()
            .name("move-through-blocks")
            .defaultValue(false)
            .build()
    );
    private final Setting<Boolean> budgetGraphics = sgGeneral.add(new BoolSetting.Builder()
            .name("budget-graphics")
            .defaultValue(false)
            .build()
    );
    
    int delayWaited = 0;
    double circleProgress = 0;
    double addition = 0.0;
    Entity target = null;
    
    public Confuse() {
        super(MeteorRejectsAddon.CATEGORY, "confuse", "Makes your enemies shit themselves");
    }

    @Override
    public void onActivate() {
        delayWaited = 0;
        circleProgress = 0;
        addition = 0.0;
        target = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        delayWaited++;
        if (delayWaited < delay.get()) return;
        delayWaited = 0;
        assert mc.player != null;
        Vec3d sel1 = mc.player.getPos().add(-4, -4, -4);
        Vec3d sel2 = sel1.add(8, 8, 8);
        Box selector = new Box(sel1, sel2);
        assert mc.world != null;
        for (Entity e : mc.world.getEntities()) {
            if (e.getUuid() == mc.player.getUuid()) continue;
            if (!e.isAlive()
                    || !e.isAttackable()) continue;
            if (e.getBoundingBox().intersects(selector)) {
                target = e;
                break;
            }
        }
        if (target == null) return;
        if (!target.isAlive()) {
            target = null;
            return;
        }
        Vec3d entityPos = target.getPos();
        Vec3d playerPos = mc.player.getPos();
        if (playerPos.distanceTo(entityPos) > 6) {
            target = null;
            return;
        }
        Random r = new Random();
        BlockHitResult hit;
        switch (mode.get()) {
            case RandomTP:
                double x = r.nextDouble() * 6 - 3;
                double y = 0;
                double z = r.nextDouble() * 6 - 3;
                Vec3d addend = new Vec3d(x, y, z);
                Vec3d goal = entityPos.add(addend);
                if (mc.world.getBlockState(new BlockPos(goal.x, goal.y, goal.z)).getBlock() != Blocks.AIR) {
                    goal = new Vec3d(x, playerPos.y, z);
                }
                if (mc.world.getBlockState(new BlockPos(goal.x, goal.y, goal.z)).getBlock() == Blocks.AIR) {
                    hit = mc.world.raycast(new RaycastContext(
                        mc.player.getPos(),goal, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, mc.player
                    ));
                    if (!moveThroughBlocks.get() && hit.isInsideBlock()) {
                        delayWaited = (int) (delay.get() - 1);
                        break;
                    }
                    mc.player.updatePosition(goal.x, goal.y, goal.z);
                } else {
                    delayWaited = (int) (delay.get() - 1);
                }
                break;
            case Switch:
                Vec3d diff = entityPos.subtract(playerPos);
                Vec3d diff1 = new Vec3d(clamp(diff.x, -3, 3), clamp(diff.y, -3, 3), clamp(diff.z, -3, 3));
                Vec3d goal2 = entityPos.add(diff1);
                hit = mc.world.raycast(new RaycastContext(
                    mc.player.getPos(), goal2, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, mc.player
                ));
                if (!moveThroughBlocks.get() && hit.isInsideBlock()) {
                    delayWaited = (int) (delay.get() - 1);
                    break;
                }
                mc.player.updatePosition(goal2.x, goal2.y, goal2.z);
                break;
            case Circle:
                delay.set(0);
                circleProgress += circleSpeed.get();
                if (circleProgress > 360) circleProgress -= 360;
                double rad = Math.toRadians(circleProgress);
                double sin = Math.sin(rad) * 3;
                double cos = Math.cos(rad) * 3;
                Vec3d current = new Vec3d(entityPos.x + sin, playerPos.y, entityPos.z + cos);
                hit = mc.world.raycast(new RaycastContext(
                    mc.player.getPos(), current, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, mc.player
                ));
                if (!moveThroughBlocks.get() && hit.isInsideBlock())
                    break;
                mc.player.updatePosition(current.x, current.y, current.z);
                break;
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (target == null) return;

        boolean flag = budgetGraphics.get();
            Vec3d last = null;
            addition += flag ? 0 : 1.0;
            if (addition > 360) addition = 0;
            for (int i = 0; i < 360; i += flag ? 7 : 1) {
                Color c1;
                if (flag) c1 = new Color(0, 255, 0);
                else {
                    double rot = (255.0 * 3) * (((((double) i) + addition) % 360) / 360.0);
                    int seed = (int) Math.floor(rot / 255.0);
                    double current = rot % 255;
                    double red = seed == 0 ? current : (seed == 1 ? Math.abs(current - 255) : 0);
                    double green = seed == 1 ? current : (seed == 2 ? Math.abs(current - 255) : 0);
                    double blue = seed == 2 ? current : (seed == 0 ? Math.abs(current - 255) : 0);
                    c1 = new Color((int) red, (int) green, (int) blue);
                }
                Vec3d tp = target.getPos();
                double rad = Math.toRadians(i);
                double sin = Math.sin(rad) * 3;
                double cos = Math.cos(rad) * 3;
                Vec3d c = new Vec3d(tp.x + sin, tp.y + target.getHeight() / 2, tp.z + cos);
                if (last != null) RenderUtils.drawLine(last, c.x, c.y, c.z, c1, event);
                last = c;
            }
    }

    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}
