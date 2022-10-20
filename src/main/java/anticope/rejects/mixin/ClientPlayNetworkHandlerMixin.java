package anticope.rejects.mixin;

import anticope.rejects.events.ChunkPosDataEvent;
import anticope.rejects.events.PlayerRespawnEvent;
import anticope.rejects.modules.SilentDisconnect;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static meteordevelopment.meteorclient.MeteorClient.mc;

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

    @Inject(method = "onDisconnected", at = @At("HEAD"), cancellable = true)
    private void onDisconnected(Text reason, CallbackInfo info) {
        if (Modules.get().isActive(SilentDisconnect.class) && mc.world != null && mc.player != null) {
            ChatUtils.info(Text.translatable("disconnect.lost").getString() + ":");
            ChatUtils.sendMsg(reason);
            info.cancel();
        }
    }
}
