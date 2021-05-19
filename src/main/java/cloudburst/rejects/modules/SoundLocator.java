package cloudburst.rejects.modules;

import cloudburst.rejects.MeteorRejectsAddon;
import meteordevelopment.orbit.EventHandler;
import minegame159.meteorclient.events.world.PlaySoundEvent;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.settings.SoundEventListSetting;
import minegame159.meteorclient.systems.modules.Categories;
import minegame159.meteorclient.systems.modules.Module;
import minegame159.meteorclient.utils.player.ChatUtils;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class SoundLocator extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<SoundEvent>> sounds = sgGeneral.add(new SoundEventListSetting.Builder()
            .name("sounds")
            .description("Sounds to find.")
            .defaultValue(new ArrayList<>(0))
            .build()
    );

    public SoundLocator() {
        super(MeteorRejectsAddon.CATEGORY, "sound-locator", "Prints locations of sound events.");
    }

    @EventHandler
    private void onPlaySound(PlaySoundEvent event) {
        for (SoundEvent sound : sounds.get()) {
            if (sound.getId().equals(event.sound.getId())) {
                printSound(event.sound);
                break;
            }
        }
    }

    private void printSound(SoundInstance sound) {
        WeightedSoundSet soundSet = mc.getSoundManager().get(sound.getId());
        MutableText text = soundSet.getSubtitle().copy();
        text.append(String.format("%s at ", Formatting.RESET));
        Vec3d pos = new Vec3d(sound.getX(), sound.getY(), sound.getZ());
        text.append(ChatUtils.formatCoords(pos));
        text.append(String.format("%s.", Formatting.RESET));
        info(text);
    }
}
