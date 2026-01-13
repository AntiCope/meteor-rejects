package anticope.rejects.gui.screens;

import anticope.rejects.utils.GiveUtils;
import com.google.common.reflect.TypeToken;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.utils.network.Http;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class HeadScreen extends WindowScreen {

    public enum Categories {
        Alphabet,
        Animals,
        Blocks,
        Decoration,
        Food_Drinks,
        Humanoid,
        Miscellaneous,
        Monsters,
        Plants
    }

    private static final Type gsonType = new TypeToken<List<Map<String, String>>>() {}.getType();

    private final Settings settings = new Settings();
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private static Categories category = Categories.Decoration;
    private final Setting<Categories> categorySetting = sgGeneral.add(new EnumSetting.Builder<Categories>()
        .name("Category")
        .defaultValue(category)
        .description("Category")
        .onChanged((v) -> this.loadHeads())
        .build()
    );

    public HeadScreen(GuiTheme theme) {
        super(theme, "Heads");
        loadHeads();
    }

    private void set() {
        clear();
        add(theme.settings(settings)).expandX();
        add(theme.horizontalSeparator()).expandX();
    }

    private String getCat() {
        category = categorySetting.get();
        return category.toString().replace("_", "-");
    }

    private void loadHeads() {
        MeteorExecutor.execute(() -> {
            List<Map<String, String>> res = Http.get("https://minecraft-heads.com/scripts/api.php?cat="+getCat()).sendJson(gsonType);
            List<ItemStack> heads = new ArrayList<>();
            res.forEach(a -> {
                try {
                    heads.add(createHeadStack(a.get("uuid"), a.get("value"), a.get("name")));
                } catch (Exception e) { }
            });

            WTable t = theme.table();
            for (ItemStack head : heads) {
                t.add(theme.item(head));
                t.add(theme.label(head.getHoverName().getString()));
                WButton give = t.add(theme.button("Give")).widget();
                give.action = () -> {
                    try {
                        GiveUtils.giveItem(head);
                    } catch (CommandSyntaxException e) {
                        ChatUtils.errorPrefix("Heads", e.getMessage());
                    }
                };
                WButton equip = t.add(theme.button("Equip")).widget();
                equip.tooltip = "Equip client-side.";
                equip.action = () -> {
                    mc.player.getInventory().setItem(39, head); // 39 is helmet slot (36 + 3)
                };
                t.row();
            }
            set();
            add(t).expandX().minWidth(400).widget();
        });
    }

    private ItemStack createHeadStack(String uuid, String value, String name) {
        ItemStack head = Items.PLAYER_HEAD.getDefaultInstance();
        CompoundTag tag = new CompoundTag();
        CompoundTag skullOwner = new CompoundTag();
        UUID parsedUuid = UUID.fromString(uuid);
        skullOwner.put("Id", new IntArrayTag(new int[]{
            (int)(parsedUuid.getMostSignificantBits() >> 32),
            (int)parsedUuid.getMostSignificantBits(),
            (int)(parsedUuid.getLeastSignificantBits() >> 32),
            (int)parsedUuid.getLeastSignificantBits()
        }));
        CompoundTag properties = new CompoundTag();
        ListTag textures = new ListTag();
        CompoundTag Value = new CompoundTag();
        Value.put("Value", net.minecraft.nbt.StringTag.valueOf(value));
        textures.add(Value);
        properties.put("textures", textures);
        skullOwner.put("Properties", properties);
        tag.put("SkullOwner", skullOwner);
        head.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        head.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return head;
    }

    @Override
    public void initWidgets() {}
}
