package cloudburst.rejects.modules;

import net.minecraft.util.shape.VoxelShapes;

import cloudburst.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.CollisionShapeEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class Phase extends Module {

    public Phase() {
        super(MeteorRejectsAddon.CATEGORY, "phase", "Lets you clip through ground sometimes.");
    }

    @EventHandler
    private void onCollisionShape(CollisionShapeEvent event) {
        if (mc.world == null || mc.player == null) return;
        if (event.type != CollisionShapeEvent.CollisionType.BLOCK) return;
        if (event.pos.getY() < mc.player.getY()) {
            if (mc.player.isSneaking()) {
                event.shape = VoxelShapes.empty();
            }
        } else {
            event.shape = VoxelShapes.empty();
        }
    }
}
