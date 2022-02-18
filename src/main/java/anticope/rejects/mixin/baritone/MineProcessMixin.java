package anticope.rejects.mixin.baritone;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.util.math.BlockPos;

import anticope.rejects.modules.OreSim;
import baritone.pathing.movement.CalculationContext;
import baritone.process.MineProcess;
import meteordevelopment.meteorclient.systems.modules.Modules;

@Mixin(MineProcess.class)
public class MineProcessMixin {
    @Shadow
    private List<BlockPos> a;

    //rescan
    @Inject(method = "Lbaritone/process/MineProcess;b(Ljava/util/List;Lbaritone/pathing/movement/CalculationContext;)V", at=@At("HEAD"), cancellable = true, remap = false)
    private void onRescan(List<BlockPos> already, CalculationContext context, CallbackInfo ci) {
        OreSim oreSim = Modules.get().get(OreSim.class);
        if (oreSim == null || !oreSim.baritone.get()) return;
        a = oreSim.oreGoals;
        ci.cancel();
    }
}
