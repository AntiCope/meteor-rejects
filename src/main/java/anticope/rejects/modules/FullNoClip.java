package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.utils.RejectsUtils;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class FullNoClip extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
            .name("speed")
            .description("Your speed when noclipping.")
            .defaultValue(0.3)
            .min(0.0)
            .sliderMax(10)
            .build()
    );

    private final Setting<Boolean> verticalSpeedMatch = sgGeneral.add(new BoolSetting.Builder()
            .name("vertical-speed-match")
            .description("Matches your vertical speed to your horizontal speed, otherwise uses vanilla ratio.")
            .defaultValue(false)
            .build()
    );

    public FullNoClip() {
        super(MeteorRejectsAddon.CATEGORY, "fullnoclip", "FullNoClip.");
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        RejectsUtils.fullFlightMove(event, speed.get(), verticalSpeedMatch.get());
    }
}