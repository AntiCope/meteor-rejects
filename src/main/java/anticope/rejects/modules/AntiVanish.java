package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.utils.NameLookup;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AntiVanish extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Integer> removeInvisible = sgGeneral.add(new IntSetting.Builder()
            .name("remove-invisible")
            .description("Removes bot only if they are invisible.")
            .defaultValue(100)
            .min(0)
            .sliderMax(300)
            .build()
    );

    private final Queue<UUID> toLookup = new ConcurrentLinkedQueue<>();
    private long lastTick = 0;

    public AntiVanish() {
        super(MeteorRejectsAddon.CATEGORY, "anti-vanish", "Notifies user when a admin uses /vanish");
    }

    @Override
    public void onDeactivate() {
        toLookup.clear();
    }

    @EventHandler
    public void onLeave(GameLeftEvent event) {
        toLookup.clear();
    }

    @EventHandler
    public void onPacket(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerListS2CPacket packet) {
            if (packet.getActions().contains(PlayerListS2CPacket.Action.UPDATE_LATENCY)) {
                try {
                    for (PlayerListS2CPacket.Entry entry : packet.getEntries()) {
                        if (mc.getNetworkHandler().getPlayerListEntry(entry.profileId()) != null)
                            continue;
                        toLookup.add(entry.profileId());
                    }
                } catch (Exception ignore) {
                }
            }
        }
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        long time = mc.world.getTime();
        UUID lookup;

        if (Math.abs(lastTick - time) > 100 && (lookup = toLookup.poll()) != null) {
            try {
                String name = getPlayerNameFromUUID(lookup);
                if (name != null) {
                    warning(name + " has gone into vanish.");
                }
            } catch (Exception ignore) {
            }
            lastTick = time;
        }
    }

    public String getPlayerNameFromUUID(UUID id) {
        try {
            final NameLookup process = new NameLookup(id, mc);
            final Thread thread = new Thread(process);
            thread.start();
            thread.join();
            return process.getName();
        } catch (Exception ignored) {
            return null;
        }
    }

    public enum Mode {
        MojangAPI,
        LeaveMessage,
    }
}


