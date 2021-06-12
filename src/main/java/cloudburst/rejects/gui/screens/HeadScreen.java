package cloudburst.rejects.gui.screens;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.LiteralText;

import minegame159.meteorclient.gui.GuiTheme;
import minegame159.meteorclient.gui.WidgetScreen;
import minegame159.meteorclient.gui.WindowScreen;
import minegame159.meteorclient.gui.widgets.containers.WTable;
import minegame159.meteorclient.gui.widgets.pressable.WButton;
import minegame159.meteorclient.settings.EnumSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.settings.Settings;
import minegame159.meteorclient.utils.network.HttpUtils;
import minegame159.meteorclient.utils.network.MeteorExecutor;
import minegame159.meteorclient.utils.player.ChatUtils;
import minegame159.meteorclient.utils.player.SlotUtils;

import static minegame159.meteorclient.utils.Utils.mc;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.lang.reflect.Type;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

    private static final Gson gson = new GsonBuilder()
        .create();

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
            InputStream in = HttpUtils.get("https://minecraft-heads.com/scripts/api.php?cat="+getCat());
            List<Map<String, String>> res = gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), gsonType);
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
                    addItem(head);
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
        head.setTag(tag);
        head.setCustomName(new LiteralText(name));
        return head;
    }

    private void addItem(ItemStack item) {
        if (!mc.player.getAbilities().creativeMode) {
            ChatUtils.error("Heads", "You must be in creative mode to use this.");
            return;
        }
		for(int i = 0; i < 36; i++) {
		    ItemStack stack = mc.player.getInventory().getStack(SlotUtils.indexToId(i));
			if (stack == null || !stack.isEmpty() || stack.getItem() != Items.AIR) continue;
			mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(SlotUtils.indexToId(i), item));
			return;
		}
        ChatUtils.error("Heads", "No space in hotbar.");
    }
}
