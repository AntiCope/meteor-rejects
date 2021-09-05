package cloudburst.rejects.gui.screens;

import cloudburst.rejects.utils.GiveUtils;
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
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.LiteralText;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static meteordevelopment.meteorclient.utils.Utils.mc;

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
                t.add(theme.label(head.getName().asString()));
                WButton give = t.add(theme.button("Give")).widget();
                give.action = () -> {
                    try {
                        GiveUtils.giveItem(head);
                    } catch (CommandSyntaxException e) {
                        ChatUtils.error("Heads", e.getMessage());
                    }
                };
                WButton equip = t.add(theme.button("Equip")).widget();
                equip.tooltip = "Equip client-side.";
                equip.action = () -> {
                    mc.player.getInventory().armor.set(3, head);
                };
                t.row();
            }
            set();
            add(t).expandX().minWidth(400).widget();
        });
    }

    private ItemStack createHeadStack(String uuid, String value, String name) {
        ItemStack head = Items.PLAYER_HEAD.getDefaultStack();
        NbtCompound tag = new NbtCompound();
        NbtCompound skullOwner = new NbtCompound();
        skullOwner.putUuid("Id", UUID.fromString(uuid));
        NbtCompound properties = new NbtCompound();
        NbtList textures = new NbtList();
        NbtCompound Value = new NbtCompound();
        Value.putString("Value", value);
        textures.add(Value);
        properties.put("textures", textures);
        skullOwner.put("Properties", properties);
        tag.put("SkullOwner", skullOwner);
        head.setNbt(tag);
        head.setCustomName(new LiteralText(name));
        return head;
    }
    
    @Override
    public void initWidgets() {}
}
