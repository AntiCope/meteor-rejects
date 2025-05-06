package anticope.rejects.gui.screens;

import anticope.rejects.utils.GiveUtils;
import anticope.rejects.utils.NetworkUtils;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
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
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
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
            .onChanged((v) -> {
                category = v;
                this.loadHeads();
            })
            .build()
    );

    public HeadScreen(GuiTheme theme) {
        super(theme, "Heads");
        set(); // Initialize UI first
        loadHeads();
    }

    private void set() {
        clear();
        add(theme.settings(settings)).expandX();
        add(theme.horizontalSeparator()).expandX();
    }

    private String getCat() {
        return category.toString().replace("_", "-");
    }

    private void loadHeads() {
        ChatUtils.info("Heads", "Loading heads from category: " + getCat());

        MeteorExecutor.execute(() -> {
            try {
                String url = "https://minecraft-heads.com/scripts/api.php?cat=" + getCat();
                List<Map<String, String>> res = Http.get(url).sendJson(gsonType);

                if (res == null || res.isEmpty()) {
                    ChatUtils.error("Heads", "Failed to load heads or no heads found in category: " + getCat());
                    return;
                }

                List<ItemStack> heads = new ArrayList<>();
                for (Map<String, String> headData : res) {
                    try {
                        String uuid = headData.get("uuid");
                        String value = headData.get("value");
                        String name = headData.get("name");

                        if (uuid != null && value != null && name != null) {
                            ItemStack head = createHeadStack(uuid, value, name);
                            if (head != null) {
                                heads.add(head);
                            }
                        }
                    } catch (Exception e) {
                        ChatUtils.error("Heads", "Error processing head: " + e.getMessage());
                    }
                }

                if (heads.isEmpty()) {
                    ChatUtils.error("Heads", "No valid heads found in category: " + getCat());
                    return;
                }

                // Update UI on the main thread
                mc.execute(() -> {
                    set();
                    WTable t = theme.table();
                    for (ItemStack head : heads) {
                        t.add(theme.item(head));
                        t.add(theme.label(head.getName().getString()));
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
                            mc.player.getInventory().setStack(39, head);
                        };
                        t.row();
                    }
                    add(t).expandX().minWidth(400).widget();
                });
            } catch (Exception e) {
                ChatUtils.error("Heads", "Failed to load heads: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    // Using a method for heads from one of my personal mods
    private ItemStack createHeadStack(String uuid, String value, String name) {
        try {
            ItemStack head = Items.PLAYER_HEAD.getDefaultStack();

            // Format UUID properly if needed
            String formattedUuid = uuid;
            if (!uuid.contains("-")) {
                formattedUuid = uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
            }

            // Create game profile directly
            GameProfile gameProfile = new GameProfile(UUID.fromString(formattedUuid), name);
            gameProfile.getProperties().put("textures", new Property("textures", value));

            // Set profile component
            head.set(DataComponentTypes.PROFILE, new ProfileComponent(gameProfile));
            head.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name));

            return head;
        } catch (Exception e) {
            ChatUtils.error("Heads", "Error creating head: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void initWidgets() {
        // This method should be implemented if WindowScreen requires it
    }
}
