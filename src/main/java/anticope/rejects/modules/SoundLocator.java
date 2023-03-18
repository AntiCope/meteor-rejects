package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.PlaySoundEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SoundLocator extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");
    
    // General
    private final Setting<Boolean> whitelist = sgGeneral.add(new BoolSetting.Builder()
        .name("whitelist")
        .description("Enable sounds filter whitelist.")
        .defaultValue(false)
        .build()
    );
    
    private final Setting<List<SoundEvent>> sounds = sgGeneral.add(new SoundEventListSetting.Builder()
        .name("sounds")
        .description("Sounds to find.")
        .defaultValue(new ArrayList<>(0))
        .visible(whitelist::get)
        .build()
    );
    
    private final Setting<Boolean> chatActive = sgGeneral.add(new BoolSetting.Builder()
        .name("log-chat")
        .description("Send the position of the sound in the chat.")
        .defaultValue(false)
        .build()
    );
    
    private final Setting<Integer> timeS = sgGeneral.add(new IntSetting.Builder()
        .name("time")
        .description("The time between the sounds verification.")
        .defaultValue(60)
        .build()
    );
    
    // Render
    
    private final Setting<Boolean> renderActive = sgRender.add(new BoolSetting.Builder()
        .name("render-positions")
        .description("Renders boxes where the sound was emitted.")
        .defaultValue(true)
        .build()
    );
    
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The side color of the target sound rendering.")
        .defaultValue(new SettingColor(255, 0, 0, 70))
        .build()
    );
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The line color of the target sound rendering.")
        .defaultValue(new SettingColor(255, 0, 0))
        .build()
    );

    public SoundLocator() {
        super(MeteorRejectsAddon.CATEGORY, "sound-locator", "Prints locations of sound events.");
    }

    private List<Vec3d> renderPos = new ArrayList<Vec3d>();
    private List<Integer> delay = new ArrayList<Integer>();

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        Iterator<Integer> iterator = delay.iterator();

        while (iterator.hasNext()) {
            int time = iterator.next();
            if (time <= 0) {
                iterator.remove();
                renderPos.remove(0);
            } else {
                delay.set(delay.indexOf(time), time - 1);
            }
        }
    }
    
    @EventHandler
    private void onPlaySound(PlaySoundEvent event) {
        if(whitelist.get()) {
            // Whitelist ON
            for (SoundEvent sound : sounds.get()) {
                if (sound.getId().equals(event.sound.getId())) {
                    printSound(event.sound);
                    break;
                }
            }
        } else {
            // Whitelist OFF (Allow all sounds)
            printSound(event.sound);
        }
    }

    private void printSound(SoundInstance sound) {
        WeightedSoundSet soundSet = mc.getSoundManager().get(sound.getId());

        Vec3d pos = new Vec3d(sound.getX() - 0.5, sound.getY() - 0.5, sound.getZ() - 0.5);
        if(!renderPos.contains(pos)) {
            renderPos.add(pos);
            delay.add(timeS.get());
            
            if(chatActive.get()) {
                MutableText text;
                if (soundSet == null || soundSet.getSubtitle() == null) {
                    text = Text.literal(sound.getId().toString());
                } else {
                    text = soundSet.getSubtitle().copy();
                }
                
                
                text.append(String.format("%s at ", Formatting.RESET));
                text.append(ChatUtils.formatCoords(pos));
                text.append(String.format("%s.", Formatting.RESET));
                info(text);
            }
            
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if(renderActive.get()) {
            renderPos.forEach(pos -> {
                event.renderer.box(Box.from(pos), sideColor.get(), lineColor.get(), shapeMode.get(), 0);
            });
        }
    }
}
