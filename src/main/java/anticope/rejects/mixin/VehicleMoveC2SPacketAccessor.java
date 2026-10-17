package anticope.rejects.mixin;

import net.minecraft.core.PositionAndRotation;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerboundMoveVehiclePacket.class)
public interface VehicleMoveC2SPacketAccessor {
    // 26.3: the packet is now a record whose position and rotation live in a single PositionAndRotation.
    @Accessor("movingTo")
    PositionAndRotation getMovingTo();

    @Invoker("<init>")
    static ServerboundMoveVehiclePacket create(PositionAndRotation movingTo, boolean onGround) {
        throw new AssertionError();
    }
}
