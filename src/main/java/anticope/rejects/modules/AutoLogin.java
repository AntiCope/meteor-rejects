package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.settings.StringMapSetting;
import anticope.rejects.utils.RejectsUtils;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import java.util.*;

public class AutoLogin extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
            .name("delay")
            .description("Delay in ms before executing the command.")
            .defaultValue(1000)
            .min(0)
            .sliderMax(10000)
            .build()
    );

    private final Setting<Boolean> smart = sgGeneral.add(new BoolSetting.Builder()
            .name("smart")
            .description("Auto add the entries.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Map<String, String>> commands = sgGeneral.add(new StringMapSetting.Builder()
            .name("commands")
            .description("Server and commands. (* for universal)")
            .defaultValue(new LinkedHashMap<>() {{
                put("localhost", "/login 123456");
            }})
            .build()
    );

    private final Timer timer = new Timer();

    public AutoLogin() {
        super(MeteorRejectsAddon.CATEGORY, "auto-login", "Runs command when joining specified server.");
        runInMainMenu = true;
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WHorizontalList l = theme.horizontalList();
        WButton btn = l.add(theme.button("Random generate password")).widget();
        btn.action = () -> {
            String password = RejectsUtils.getRandomPassword(16);
            MutableComponent text = Component.literal(ChatFormatting.BOLD + "Click here to register securely.");
            text.setStyle(text.getStyle().withClickEvent(new net.minecraft.network.chat.ClickEvent.RunCommand(String.format("/register %s %s", password, password))));
            info(text);
        };
        return l;
    }

    @EventHandler
    private void onGameJoined(GameJoinedEvent event) {
        if (!isActive()) return;
        String command = commands.get().getOrDefault("*", commands.get().get(Utils.getWorldName()));
        if (command != null) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (mc.player != null) ChatUtils.sendPlayerMsg(command);
                }
            }, delay.get());
        }
    }

    @EventHandler
    private void onPacketSent(PacketEvent.Send event) {
        if (!smart.get()) return;

        if (event.packet instanceof ServerboundChatCommandPacket packet) {
            String command = packet.command();
            List<String> hint = Arrays.asList("reg", "register", "l", "login", "log");
            String[] cmds = command.split(" ");
            if (cmds.length >= 2 && hint.contains(cmds[0])) {
                commands.get().put(Utils.getWorldName(), "/login " + cmds[1]);
            }
        }
    }
}
