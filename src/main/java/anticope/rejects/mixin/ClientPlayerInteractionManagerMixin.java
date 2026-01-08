package anticope.rejects.mixin;

import anticope.rejects.events.StopUsingItemEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class ClientPlayerInteractionManagerMixin {
    @Inject(method = "releaseUsingItem", at = @At("HEAD"))
    public void onStopUsingItem(Player player, CallbackInfo ci) {
        MeteorClient.EVENT_BUS.post(StopUsingItemEvent.get(player.getInventory().getSelectedItem()));
    }
}