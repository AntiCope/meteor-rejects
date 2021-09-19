package cloudburst.rejects.modules;

import cloudburst.rejects.MeteorRejectsAddon;
import cloudburst.rejects.gui.screens.InteractionScreen;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPlus;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;

import java.util.*;

public class InteractionMenu extends Module {
    
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgStyle = settings.createGroup("Style");
    
    private final Setting<Object2BooleanMap<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
            .name("entities")
            .description("Entities")
            .defaultValue(Utils.asObject2BooleanOpenHashMap(
                    EntityType.PLAYER))
            .build()
    );
    public final Setting<Keybind> keybind = sgGeneral.add(new KeybindSetting.Builder()
            .name("keybind")
            .description("The keybind to open.")
            .action(this::onKey)
            .build()
    );

    // Style
    public final Setting<SettingColor> selectedDotColor = sgStyle.add(new ColorSetting.Builder()
            .name("selected-dot-color")
            .description("Color of the dot when selected.")
            .defaultValue(new SettingColor(76, 255, 0))
            .build()
    );
    public final Setting<SettingColor> dotColor = sgStyle.add(new ColorSetting.Builder()
            .name("dot-color")
            .description("Color of the dot when.")
            .defaultValue(new SettingColor(0, 148, 255))
            .build()
    );
    public final Setting<SettingColor> backgroundColor = sgStyle.add(new ColorSetting.Builder()
            .name("background-color")
            .description("Color of the background.")
            .defaultValue(new SettingColor(128, 128, 128, 128))
            .build()
    );
    public final Setting<SettingColor> borderColor = sgStyle.add(new ColorSetting.Builder()
            .name("border-color")
            .description("Color of the border.")
            .defaultValue(new SettingColor(0,0,0))
            .build()
    );
    public final Setting<SettingColor> textColor = sgStyle.add(new ColorSetting.Builder()
            .name("text-color")
            .description("Color of the text.")
            .defaultValue(new SettingColor(255,255,255))
            .build()
    );
    
    public final HashMap<String,String> messages = new HashMap<>();
    private String currMsgK = "", currMsgV = "";
    
    public InteractionMenu() {
        super(MeteorRejectsAddon.CATEGORY,"interaction-menu","An interaction screen when looking at an entity.");
    }

    public void onKey() {
        if (mc.currentScreen != null) return;
        Optional<Entity> lookingAt = DebugRenderer.getTargetedEntity(mc.player, 20);
        if (lookingAt.isPresent()) {
            Entity e = lookingAt.get();
            if (entities.get().getBoolean(e.getType())) {
                mc.setScreen(new InteractionScreen(e, this));
            }
        }
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WTable table = theme.table();
        fillTable(theme, table);
        return table;
    }

    private void fillTable(GuiTheme theme, WTable table) {
        table.clear();
        messages.keySet().forEach((key) -> {
            table.add(theme.label(key)).expandCellX();
            table.add(theme.label(messages.get(key))).expandCellX();
            WMinus delete = table.add(theme.minus()).widget();
            delete.action = () -> {
                messages.remove(key);
                fillTable(theme,table);
            };
            table.row();
        });
        WTextBox textBoxK = table.add(theme.textBox(currMsgK)).minWidth(100).expandX().widget();
        textBoxK.action = () -> {
            currMsgK = textBoxK.get();
        };
        WTextBox textBoxV = table.add(theme.textBox(currMsgV)).minWidth(100).expandX().widget();
        textBoxV.action = () -> {
            currMsgV = textBoxV.get();
        };
        WPlus add = table.add(theme.plus()).widget();
        add.action = () -> {
            if (currMsgK != ""  && currMsgV != "") {
                messages.put(currMsgK, currMsgV);
                currMsgK = ""; currMsgV = "";
                fillTable(theme,table);
            }
        };
        table.row();
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = super.toTag();
        
        NbtCompound messTag = new NbtCompound();
        messages.keySet().forEach((key) -> {
            messTag.put(key, NbtString.of(messages.get(key)));
        });

        tag.put("messages", messTag);
        return tag;
    }

    @Override
    public Module fromTag(NbtCompound tag) {
        
        messages.clear();
        if (tag.contains("messages")) {
            NbtCompound msgs = tag.getCompound("messages");
            msgs.getKeys().forEach((key) -> {
                messages.put(key, msgs.getString(key));
            });
        }

        return super.fromTag(tag);
    }
}
