package anticope.rejects.mixin;

import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerboundMoveVehiclePacket.class)
public interface VehicleMoveC2SPacketAccessor {
    @Accessor("position")
    Vec3 getPosition();

    @Invoker("<init>")
    static ServerboundMoveVehiclePacket create(Vec3 position, float yaw, float pitch, boolean onGround) {
        throw new AssertionError();
    }
}
