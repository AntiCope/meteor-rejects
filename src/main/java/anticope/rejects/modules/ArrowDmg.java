package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.events.StopUsingItemEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class ArrowDmg extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Double> strength = sgGeneral.add(new DoubleSetting.Builder()
            .name("strength")
            .description("More strength = higher damage.")
            .defaultValue(5)
            .min(0.1)
            .sliderMax(25)
            .build()
    );

    public final Setting<Boolean> tridents = sgGeneral.add(new BoolSetting.Builder()
            .name("tridents")
            .description("When enabled, tridents fly much further. Doesn't seem to affect damage or Riptide. WARNING: You can easily lose your trident by enabling this option!")
            .defaultValue(false)
            .build()
    );


    public ArrowDmg() {
        super(MeteorRejectsAddon.CATEGORY, "arrow-damage", "Massively increases arrow damage, but reduces accuracy and consume more hunger. Does not work with crossbows and is patched on Paper servers.");
    }

    @EventHandler
    private void onStopUsingItem(StopUsingItemEvent event) {
        if (!isValidItem(event.itemStack.getItem()))
            return;

        LocalPlayer p = mc.player;

        p.connection.send(
                new ServerboundPlayerCommandPacket(p, ServerboundPlayerCommandPacket.Action.START_SPRINTING));

        double x = p.getX();
        double y = p.getY();
        double z = p.getZ();

        double adjustedStrength = strength.get() / 10.0 * Math.sqrt(500);
        Vec3 lookVec = p.getViewVector(1).scale(adjustedStrength);

        for (int i = 0; i < 4; i++) {
            sendPos(x, y, z, true);
        }
        sendPos(x - lookVec.x, y, z - lookVec.z, true);
        sendPos(x, y, z, false);
    }

    private void sendPos(double x, double y, double z, boolean onGround) {
        ClientPacketListener clientPacketListener = mc.player.connection;
        clientPacketListener.send(new Pos(x, y, z, onGround, mc.player.horizontalCollision));
    }

    private boolean isValidItem(Item item) {
        return tridents.get() && item == Items.TRIDENT || item == Items.BOW;
    }
}
