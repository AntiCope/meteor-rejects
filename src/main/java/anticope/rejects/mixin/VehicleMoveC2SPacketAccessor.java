package anticope.rejects.mixin;

import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(VehicleMoveC2SPacket.class)
public interface VehicleMoveC2SPacketAccessor {
    @Accessor("position")
    Vec3d getPosition();

    @Invoker("<init>")
    static VehicleMoveC2SPacket create(Vec3d position, float yaw, float pitch, boolean onGround) {
        throw new AssertionError();
    }
}
