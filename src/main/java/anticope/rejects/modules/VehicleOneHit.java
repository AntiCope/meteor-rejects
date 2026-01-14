package anticope.rejects.modules;


import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.phys.EntityHitResult;

public class VehicleOneHit extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> amount = sgGeneral.add(new IntSetting.Builder()
            .name("amount")
            .description("The number of packets to send.")
            .defaultValue(16)
            .range(1, 100)
            .sliderRange(1, 20)
            .build()
    );

    public VehicleOneHit() {
        super(MeteorRejectsAddon.CATEGORY, "vehicle-one-hit", "Destroy vehicles with one hit.");
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (!(event.packet instanceof ServerboundInteractPacket)
            || !(mc.hitResult instanceof EntityHitResult ehr)
            || (!(ehr.getEntity() instanceof AbstractMinecart) && !(ehr.getEntity() instanceof Boat))
        ) return;

        for (int i = 0; i < amount.get() - 1; i++) {
            mc.player.connection.getConnection().send(event.packet, null);
        }
    }
}