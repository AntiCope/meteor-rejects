package anticope.rejects.mixin;

import anticope.rejects.events.PlayerRespawnEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {

    //called on dimension change too
    @Inject(method = "handleRespawn", at = @At("TAIL"))
    public void onPlayerRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
        MeteorClient.EVENT_BUS.post(PlayerRespawnEvent.get());
    }
}
