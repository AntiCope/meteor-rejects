package anticope.rejects.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import anticope.rejects.modules.FullFlight;
import anticope.rejects.modules.FullNoClip;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Inject(method = "slowMovement", at = @At("HEAD"), cancellable = true)
	public void slowMovement(BlockState blockState, Vec3d multiplier, CallbackInfo ci) {
		if (Modules.get().isActive(FullFlight.class) || Modules.get().isActive(FullNoClip.class)) ci.cancel();
	}
}