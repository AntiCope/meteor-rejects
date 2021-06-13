package cloudburst.rejects.utils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.Triple;

import static minegame159.meteorclient.utils.Utils.mc;

public class GiveUtils {

    public static final Map<String, Function<Boolean, ItemStack>> PRESETS = new HashMap<>();

    private final static SimpleCommandExceptionType NOT_IN_CREATIVE = new SimpleCommandExceptionType(new LiteralText("You must be in creative mode to use this."));
    private final static SimpleCommandExceptionType NO_SPACE = new SimpleCommandExceptionType(new LiteralText("No space in hotbar."));

    private static final List<Identifier> HIDDEN_ENTITIES = Arrays.asList(
        new Identifier("giant"),
        new Identifier("ender_dragon"),
        new Identifier("wither"),
        new Identifier("iron_golem"),
        new Identifier("ender_dragon"),
        new Identifier("tnt_minecart"),
        new Identifier("lightning_bolt"));

    // Ported from: https://github.com/BleachDrinker420/BleachHack/blob/master/BleachHack-Fabric-1.16/src/main/java/bleach/hack/command/commands/CmdGive.java
    private static final List<Triple<String, Item, String>> STRING_PRESETS = Arrays.asList(
        Triple.of("lag_spawner", Items.SPAWNER, "{BlockEntityTag:{MaxNearbyEntities:32767,RequiredPlayerRange:32767,SpawnCount:32767,MaxSpawnDelay:0,SpawnRange:32767,Delay:0,MinSpawnDelay:0}}"),
        Triple.of("tnt_spawner", Items.SPAWNER, "{BlockEntityTag:{MaxNearbyEntities:32767,RequiredPlayerRange:32767,SpawnCount:50,SpawnData:{Fuse:1,id:\"minecraft:tnt\"},MaxSpawnDelay:0,SpawnRange:10,Delay:0,MinSpawnDelay:0}}"),
        Triple.of("boat_spawner", Items.SPAWNER, "{BlockEntityTag:{SpawnData:{Type:\"jungle\",CustomName:'{\"text\":\"Boat\",\"color\":\"aqua\",\"bold\":true,\"italic\":true,\"underlined\":true}',Invulnerable:1b,id:\"minecraft:boat\",Glowing:1b,CustomNameVisible:1b},SpawnRange:10,SpawnCount:50}}"),
        Triple.of("pigs_egg", Items.CHICKEN_SPAWN_EGG, "{EntityTag:{MaxNearbyEntities:1000,RequiredPlayerRange:100,CustomDisplayTile:1b,DisplayState:{Properties:{hinge:\"left\",half:\"upper\",open:\"true\"},Name:\"minecraft:acacia_door\"},SpawnData:{id:\"minecraft:minecart\"},id:\"minecraft:spawner_minecart\",MaxSpawnDelay:0,Delay:1,MinSpawnDelay:0}}"),
        Triple.of("end_portal_arrow", Items.ELDER_GUARDIAN_SPAWN_EGG, "{EntityTag:{SoundEvent:\"block.end_portal.spawn\",pickup:1b,id:\"minecraft:arrow\"}}"),
        Triple.of("wither_spawn_arrow", Items.ELDER_GUARDIAN_SPAWN_EGG, "{EntityTag:{SoundEvent:\"entity.wither.spawn\",pickup:1b,id:\"minecraft:arrow\"}}"),
        Triple.of("eg_curse_arrow", Items.ELDER_GUARDIAN_SPAWN_EGG, "{EntityTag:{SoundEvent:\"entity.elder_guardian.curse\",pickup:1b,id:\"minecraft:arrow\"}}"),
        Triple.of("big_slime", Items.SLIME_SPAWN_EGG, "{EntityTag:{Size:50,id:\"minecraft:slime\"}}"),
        Triple.of("particle_area_expand", Items.SKELETON_SPAWN_EGG, "{EntityTag:{Particle:\"angry_villager\",Radius:1.0f,RadiusOnUse:1.0f,Duration:10000,id:\"minecraft:area_effect_cloud\",RadiusPerTick:10.0f}}"),
        Triple.of("armor_stand_spawner_minecart", Items.BAT_SPAWN_EGG, "{EntityTag:{SpawnData:{id:\"minecraft:armor_stand\"},id:\"minecraft:spawner_minecart\"}}")
    );

    private static final Random random = new Random();

    public static void giveItem(ItemStack item) throws CommandSyntaxException {
        if (!mc.player.getAbilities().creativeMode) throw NOT_IN_CREATIVE.create();
        
        if (!mc.player.getInventory().insertStack(item)) {
            throw NO_SPACE.create();
        }
    }

    public static void init() {
        STRING_PRESETS.forEach((preset) -> {
            PRESETS.put(preset.getLeft(), (preview) -> {
                if (preview) preset.getMiddle().getDefaultStack();
                ItemStack item = preset.getMiddle().getDefaultStack();
                try {
                    item.setTag(StringNbtReader.parse(preset.getRight()));
                } catch (CommandSyntaxException e) { }
                item.setCustomName(new LiteralText(toName(preset.getLeft())));
                return item;
            });
        });

        PRESETS.put("force_op", (preview) -> {
            if (preview) Items.SPIDER_SPAWN_EGG.getDefaultStack();
            ItemStack item = Items.SPIDER_SPAWN_EGG.getDefaultStack();
            String nick = mc.player.getName().asString();
            try {
                item.setTag(StringNbtReader.parse("{EntityTag:{Time:1,BlockState:{Name:\"minecraft:spawner\"},id:\"minecraft:falling_block\",TileEntityData:{SpawnCount:20,SpawnData:{id:\"minecraft:villager\",Passengers:[{Time:1,BlockState:{Name:\"minecraft:redstone_block\"},id:\"minecraft:falling_block\",Passengers:[{id:\"minecraft:fox\",Passengers:[{Time:1,BlockState:{Name:\"minecraft:activator_rail\"},id:\"minecraft:falling_block\",Passengers:[{Command:\"execute as @e run op "+nick+"\",id:\"minecraft:command_block_minecart\"}]}],NoAI:1b,Health:1.0f,ActiveEffects:[{Duration:1000,Id:20b,Amplifier:4b}]}]}],NoAI:1b,Health:1.0f,ActiveEffects:[{Duration:1000,Id:20b,Amplifier:4b}]},MaxSpawnDelay:100,SpawnRange:10,Delay:1,MinSpawnDelay:100}}}"));
            } catch (CommandSyntaxException e) { }
            item.setCustomName(new LiteralText("Force OP"));
            return item;
        });

        PRESETS.put("troll_potion", (preview) -> {
            if (preview) Items.LINGERING_POTION.getDefaultStack();
            ItemStack stack = Items.LINGERING_POTION.getDefaultStack();
            NbtList effects = new NbtList();
            for(int i = 1; i <= 31; i++)
            {
                NbtCompound effect = new NbtCompound();
                effect.putByte("Amplifier", (byte)127);
                effect.putInt("Duration", Integer.MAX_VALUE);
                effect.putInt("Id", i);
                effects.add(effect);
            }
            NbtCompound nbt = new NbtCompound();
            nbt.put("CustomPotionEffects", effects);
            stack.setTag(nbt);
            stack.setCustomName(new LiteralText("Lingering Potion of Trolling"));
            return stack;
        });

        PRESETS.put("32k", (preview) -> {
            if (preview) return Items.DIAMOND_SWORD.getDefaultStack();
            ItemStack stack =Items.DIAMOND_SWORD.getDefaultStack();
            NbtList enchants = new NbtList();
            addEnchant(enchants, "minecraft:sharpness");
            addEnchant(enchants, "minecraft:knockback");
            addEnchant(enchants, "minecraft:fire_aspect");
            addEnchant(enchants, "minecraft:looting", (short)10);
            addEnchant(enchants, "minecraft:sweeping", (short)3);
            addEnchant(enchants, "minecraft:unbreaking");
            addEnchant(enchants, "minecraft:mending", (short)1);
            addEnchant(enchants, "minecraft:vanishing_curse", (short)1);
            NbtCompound nbt = new NbtCompound();
            nbt.put("Enchantments", enchants);
            stack.setTag(nbt);
            stack.setCustomName(new LiteralText("Bonk"));
            return stack;
        });

        PRESETS.put("crash_chest", (preview) -> {
            if (preview) return Items.CHEST.getDefaultStack();
            ItemStack stack = Items.CHEST.getDefaultStack();
            NbtCompound nbtCompound = new NbtCompound();
            NbtList nbtList = new NbtList();
            for(int i = 0; i < 40000; i++)
                nbtList.add(new NbtList());
            nbtCompound.put("nothingsuspicioushere", nbtList);
            stack.setTag(nbtCompound);
            stack.setCustomName(new LiteralText("Copy Me"));
            return stack;
        });

        PRESETS.put("firework", (preview) -> {
            if (preview) return Items.FIREWORK_ROCKET.getDefaultStack();

            ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);
            NbtCompound baseCompound = new NbtCompound();
            NbtCompound tagCompound = new NbtCompound();
            NbtList explosionList = new NbtList();
            for(int i = 0; i < 5000; i++)
            {
                NbtCompound explosionCompound = new NbtCompound();
                explosionCompound.putByte("Type", (byte)random.nextInt(5));

                int colors[] = {1973019,11743532,3887386,5320730,2437522,8073150,2651799,11250603,4408131,14188952,4312372,14602026,6719955,12801229,15435844,15790320};

                explosionCompound.putIntArray("Colors", colors);
                explosionList.add(explosionCompound);
            }
            tagCompound.putInt("Flight", 0);
            tagCompound.put("Explosions", explosionList);
            baseCompound.put("Fireworks", tagCompound);
            firework.setTag(baseCompound);
            return firework;
        });
    
        HIDDEN_ENTITIES.forEach((id) -> {
            PRESETS.put(id.getPath()+"_spawn_egg", (preview) -> {
                if (preview) return Items.PIG_SPAWN_EGG.getDefaultStack();
                ItemStack egg = Items.PIG_SPAWN_EGG.getDefaultStack();
                NbtCompound tag = new NbtCompound();
                NbtCompound entityTag = new NbtCompound();
                entityTag.putString("id", id.toString());
                tag.put("EntityTag", entityTag);
                egg.setTag(tag);
                egg.setCustomName(new LiteralText(String.format("%s", toName(id.getPath()))));
                return egg;
            });
        });
    }

    public static ItemStack getPreset(String name, boolean preview) {
        return PRESETS.get(name).apply(preview);
    }

    public static ItemStack getPreset(String name) {
        return getPreset(name, false);
    }

    private static String toName(Object id) {
        return WordUtils.capitalizeFully(id.toString().replace("_", " "));
    }

    private static void addEnchant(NbtList tag, String id, short v) {
        NbtCompound enchant = new NbtCompound();
        enchant.putShort("lvl", v);
        enchant.putString("id", id);
        tag.add(enchant);
    }

    private static void addEnchant(NbtList tag, String id) {
        addEnchant(tag, id, Short.MAX_VALUE);
    }
}
