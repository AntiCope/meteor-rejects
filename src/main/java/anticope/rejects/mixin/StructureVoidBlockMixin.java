package anticope.rejects.mixin;

import anticope.rejects.modules.Rendering;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.StructureVoidBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructureVoidBlock.class)
public abstract class StructureVoidBlockMixin extends Block {
	public StructureVoidBlockMixin(Properties settings) {
		super(settings);
	}

	@Inject(at = @At("HEAD"), method = "getRenderShape", cancellable = true)
	public void getRenderType(BlockState state, CallbackInfoReturnable<RenderShape> info) {
		info.setReturnValue(RenderShape.MODEL);
	}

	@Override
	public boolean skipRendering(BlockState state, BlockState neighbor, Direction facing) {
		Rendering renderingModule = Modules.get().get(Rendering.class);
		return !(renderingModule != null && renderingModule.renderStructureVoid());
	}
}
