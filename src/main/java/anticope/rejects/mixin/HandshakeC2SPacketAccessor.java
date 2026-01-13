package anticope.rejects.mixin;

import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientIntentionPacket.class)
public interface HandshakeC2SPacketAccessor {
    @Mutable
    @Accessor
    void setHostName(String address);
}
