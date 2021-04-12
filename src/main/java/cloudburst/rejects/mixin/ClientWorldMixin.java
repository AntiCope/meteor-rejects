package cloudburst.rejects.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

import cloudburst.rejects.modules.RenderInvisible;
import minegame159.meteorclient.systems.modules.Modules;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {

    @Shadow
    MinecraftClient client;

    @Inject(at = @At("HEAD"), method = "doRandomBlockDisplayTicks", cancellable = true)
    public void doRandomBlockDisplayTicks(int xCenter, int yCenter, int i, CallbackInfo info) {
        Random random = new Random();
        boolean showBarrierParticles = this.client.interactionManager.getCurrentGameMode() == GameMode.CREATIVE && (this.client.player.inventory.getMainHandStack().getItem() == Items.BARRIER || this.client.player.inventory.offHand.get(0).getItem() == Items.BARRIER);
        if (Modules.get().get(RenderInvisible.class).isActive()) showBarrierParticles = true;

        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for(int k = 0; k < 667; ++k) {
            client.world.randomBlockDisplayTick(xCenter, yCenter, i, 16, random, showBarrierParticles, mutable);
            client.world.randomBlockDisplayTick(xCenter, yCenter, i, 32, random, showBarrierParticles, mutable);
        }

        info.cancel();
    }
}
