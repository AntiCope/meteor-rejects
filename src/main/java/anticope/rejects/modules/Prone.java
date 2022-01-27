package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.CollisionShapeEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;

import net.minecraft.util.shape.VoxelShapes;

public class Prone extends Module {

    public enum Mode {
		Maintain,
		Collision
	}

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
            .name("mode")
            .description("The mode used.")
            .defaultValue(Mode.Maintain)
            .build()
    );

    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("blocks")
        .description("Selected blocks.")
        .visible(() -> (mode.get() == Mode.Maintain))
        .build()
    );


    public Prone() {
        super(MeteorRejectsAddon.CATEGORY, "prone", "Become prone on demand.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mode.get() == Mode.Maintain && mc.player.isInSwimmingPose() && !mc.player.isSubmergedInWater()) {
            BlockUtils.place(mc.player.getBlockPos().up(), InvUtils.find((itemstack) -> {return (itemstack.getItem() instanceof BlockItem blockitem && blocks.get().contains(blockitem.getBlock()));}), true, 1);
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
}
