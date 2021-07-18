package cloudburst.rejects.events;

import cloudburst.rejects.mixin.CustomPayloadS2CPacketMixin;
import meteordevelopment.meteorclient.events.Cancellable;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;

public class CustomPayloadEvent extends PacketEvent {
    public CustomPayloadS2CPacket packet;

    private static final CustomPayloadEvent INSTANCE = new CustomPayloadEvent();

    public static CustomPayloadEvent get(CustomPayloadS2CPacket packet) {
        INSTANCE.setCancelled(false);
        INSTANCE.packet = packet;
        return INSTANCE;
    }
}
