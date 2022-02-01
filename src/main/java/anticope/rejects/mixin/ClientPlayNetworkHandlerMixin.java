package anticope.rejects.mixin;

import anticope.rejects.events.ChunkPosDataEvent;
import anticope.rejects.events.PlayerRespawnEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    //the normal onChunkDataEvent should just provide coords aswell
    @Inject(method = "onChunkData", at = @At("TAIL"))
    public void onChunkData(ChunkDataS2CPacket packet, CallbackInfo ci) {
        MeteorClient.EVENT_BUS.post(ChunkPosDataEvent.get(packet.getX(), packet.getZ()));
    }

    //called on dimension change too
    @Inject(method = "onPlayerRespawn", at = @At("TAIL"))
    public void onPlayerRespawn(PlayerRespawnS2CPacket packet, CallbackInfo ci) {
        MeteorClient.EVENT_BUS.post(PlayerRespawnEvent.get());
    }
}
