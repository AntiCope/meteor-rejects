package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Random;

// Too much much spaghetti!
// -StormyBytes

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

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
            .name("delay")
            .description("Delay")
            .defaultValue(3)
            .min(0)
            .sliderMax(20)
            .build()
    );

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
            .name("radius")
            .description("Range to confuse opponents")
            .defaultValue(6)
            .min(0).sliderMax(10)
            .build()
    );

    private final Setting<SortPriority> priority = sgGeneral.add(new EnumSetting.Builder<SortPriority>()
            .name("priority")
            .description("Targetting priority")
            .defaultValue(SortPriority.LowestHealth)
            .build()
    );

    private final Setting<Integer> circleSpeed = sgGeneral.add(new IntSetting.Builder()
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

    private final Setting<SettingColor> circleColor = sgGeneral.add(new ColorSetting.Builder()
            .name("circle-color")
            .description("Color for circle rendering")
            .defaultValue(new SettingColor(0, 255, 0))
            .visible(budgetGraphics::get)
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

        // Delay
        delayWaited++;
        if (delayWaited < delay.get()) return;
        delayWaited = 0;


        // Targetting
        target = TargetUtils.getPlayerTarget(range.get(), priority.get());

        if (target == null) return;

        Vec3d entityPos = target.getPos();
        Vec3d playerPos = mc.player.getPos();
        Random r = new Random();
        BlockHitResult hit;
        int halfRange = range.get() / 2;

        switch (mode.get()) {
            case RandomTP:
                double x = r.nextDouble() * range.get() - halfRange;
                double y = 0;
                double z = r.nextDouble() * range.get() - halfRange;
                Vec3d addend = new Vec3d(x, y, z);
                Vec3d goal = entityPos.add(addend);
                if (mc.world.getBlockState(BlockPos.ofFloored(goal.x, goal.y, goal.z)).getBlock() != Blocks.AIR) {
                    goal = new Vec3d(x, playerPos.y, z);
                }
                if (mc.world.getBlockState(BlockPos.ofFloored(goal.x, goal.y, goal.z)).getBlock() == Blocks.AIR) {
                    hit = mc.world.raycast(new RaycastContext(mc.player.getPos(), goal, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, mc.player));
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
                Vec3d diff1 = new Vec3d(MathHelper.clamp(diff.x, -halfRange, halfRange), MathHelper.clamp(diff.y, -halfRange, halfRange), MathHelper.clamp(diff.z, -halfRange, halfRange));
                Vec3d goal2 = entityPos.add(diff1);
                hit = mc.world.raycast(new RaycastContext(mc.player.getPos(), goal2, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, mc.player));
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
                hit = mc.world.raycast(new RaycastContext(mc.player.getPos(), current, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, mc.player));
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
            if (flag) c1 = circleColor.get();
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
            if (last != null) event.renderer.line(last.x, last.y, last.z, c.x, c.y, c.z, c1);
            last = c;
        }
    }
}
