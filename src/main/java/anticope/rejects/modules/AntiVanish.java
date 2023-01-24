package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.utils.NameLookup;
import com.mojang.brigadier.suggestion.Suggestion;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AntiVanish extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> interval = sgGeneral.add(new IntSetting.Builder()
            .name("interval")
            .description("Vanish check interval.")
            .defaultValue(100)
            .min(0)
            .sliderMax(300)
            .build()
    );

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
            .name("mode")
            .defaultValue(Mode.LeaveMessage)
            .build()
    );

    private final Setting<String> command = sgGeneral.add(new StringSetting.Builder()
            .name("command")
            .description("The completion command.")
            .defaultValue("minecraft:msg")
            .visible(() -> mode.get() == Mode.RealJoinMessage)
            .build()
    );

    private final Queue<UUID> toLookup = new ConcurrentLinkedQueue<>();

    private Map<UUID, String> playerCache = new HashMap<>();
    private final List<String> messageCache = new ArrayList<>();

    private final Random random = new Random();
    private final List<Integer> completionIDs = new ArrayList<>();
    private List<String> completionPlayerCache = new ArrayList<>();

    private int timer = 0;

    public AntiVanish() {
        super(MeteorRejectsAddon.CATEGORY, "anti-vanish", "Notifies user when a admin uses /vanish");
    }

    @Override
    public void onActivate() {
        timer = 0;
        toLookup.clear();
        completionIDs.clear();
        messageCache.clear();
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WVerticalList l = theme.verticalList();
        l.add(theme.label("MojangAPI: Detect player status. (for online servers)"));
        l.add(theme.label("LeaveMessage: If client didn't receive a quit game message (like essentials)."));
        l.add(theme.label("RealJoinMessage: Tell whether the player is really left."));
        return l;
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        if (mode.get() == Mode.RealJoinMessage && event.packet instanceof CommandSuggestionsS2CPacket packet) {
            if (completionIDs.contains(packet.getCompletionId())) {
                var lastUsernames = completionPlayerCache.stream().toList();

                completionPlayerCache = packet.getSuggestions().getList().stream()
                        .map(Suggestion::getText)
                        .toList();

                if (lastUsernames.isEmpty()) {
                    return;
                }

                Predicate<String> joinedOrQuit = playerName -> lastUsernames.contains(playerName) != completionPlayerCache.contains(playerName);

                for (String playerName : completionPlayerCache) {
                    if (Objects.equals(playerName, mc.player.getName().getString())) continue;
                    if (joinedOrQuit.test(playerName)) {
                        info("Player joined: " + playerName);
                    }
                }

                for (String playerName : lastUsernames) {
                    if (Objects.equals(playerName, mc.player.getName().getString())) continue;
                    if (joinedOrQuit.test(playerName)) {
                        info("Player left: " + playerName);
                    }
                }

                completionIDs.remove(Integer.valueOf(packet.getCompletionId()));
                event.cancel();
            }
        } else if (mode.get() == Mode.MojangAPI && event.packet instanceof PlayerListS2CPacket packet) {
            for (PlayerListS2CPacket.Entry entry : packet.getEntries()) {
                UUID profID = entry.profileId();
                PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(profID);
                if (playerListEntry != null) continue;
                toLookup.add(entry.profileId());
            }
        }
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        messageCache.add(event.getMessage().getString());
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        timer++;
        if (timer < interval.get()) return;

        switch (mode.get()) {
            case MojangAPI -> {
                UUID uuid = toLookup.poll();
                if (uuid != null)
                    MeteorExecutor.execute(new NameLookup(uuid, mc, s -> warning(s + " has gone into vanish.")));
            }
            case LeaveMessage -> {
                Map<UUID, String> oldPlayers = Map.copyOf(playerCache);
                playerCache = mc.getNetworkHandler().getPlayerList().stream().collect(Collectors.toMap(e -> e.getProfile().getId(), e -> e.getProfile().getName()));

                for (UUID uuid : oldPlayers.keySet()) {
                    if (playerCache.containsKey(uuid)) continue;
                    String name = oldPlayers.get(uuid);
                    if (messageCache.stream().noneMatch(s -> s.contains(name))) {
                        warning(name + " has gone into vanish.");
                    }
                }
            }
            case RealJoinMessage -> {
                int id = random.nextInt(200);
                completionIDs.add(id);
                mc.getNetworkHandler().sendPacket(new RequestCommandCompletionsC2SPacket(id, command.get() + " "));
            }
        }
        timer = 0;
        messageCache.clear();
    }

    public enum Mode {
        LeaveMessage,
        MojangAPI,
        RealJoinMessage//https://github.com/xtrm-en/meteor-antistaff/blob/main/src/main/java/me/xtrm/meteorclient/antistaff/modules/AntiStaff.java
    }
}