package anticope.rejects.utils;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.Function;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.Triple;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class GiveUtils {

    public static final Map<String, Function<Boolean, ItemStack>> PRESETS = new HashMap<>();

    private final static SimpleCommandExceptionType NOT_IN_CREATIVE = new SimpleCommandExceptionType(Text.literal("You must be in creative mode to use this."));
    private final static SimpleCommandExceptionType NO_SPACE = new SimpleCommandExceptionType(Text.literal("No space in hotbar."));

    private static final List<Identifier> HIDDEN_ENTITIES = Arrays.asList(
        Identifier.of("giant"),
        Identifier.of("ender_dragon"),
        Identifier.of("wither"),
        Identifier.of("iron_golem"),
        Identifier.of("ender_dragon"),
        Identifier.of("tnt_minecart"),
        Identifier.of("lightning_bolt"));

    // Some ported from: https://github.com/BleachDrinker420/BleachHack/blob/master/BleachHack-Fabric-1.16/src/main/java/bleach/hack/command/commands/CmdGive.java
    private static final List<Triple<String, Item, String>> ENTITY_PRESETS = Arrays.asList(
        Triple.of("pigs_egg", Items.CHICKEN_SPAWN_EGG, "{MaxNearbyEntities:1000,RequiredPlayerRange:100,CustomDisplayTile:1b,DisplayState:{Properties:{hinge:\"left\",half:\"upper\",open:\"true\"},Name:\"minecraft:acacia_door\"},SpawnData:{id:\"minecraft:minecart\"},id:\"minecraft:spawner_minecart\",MaxSpawnDelay:0,Delay:1,MinSpawnDelay:0}"),
        Triple.of("end_portal_arrow", Items.ELDER_GUARDIAN_SPAWN_EGG, "{SoundEvent:\"block.end_portal.spawn\",pickup:1b,id:\"minecraft:arrow\"}"),
        Triple.of("wither_spawn_arrow", Items.ELDER_GUARDIAN_SPAWN_EGG, "{SoundEvent:\"entity.wither.spawn\",pickup:1b,id:\"minecraft:arrow\"}"),
        Triple.of("eg_curse_arrow", Items.ELDER_GUARDIAN_SPAWN_EGG, "{SoundEvent:\"entity.elder_guardian.curse\",pickup:1b,id:\"minecraft:arrow\"}"),
        Triple.of("big_slime", Items.SLIME_SPAWN_EGG, "{Size:50,id:\"minecraft:slime\"}"),
        Triple.of("particle_area_expand", Items.SKELETON_SPAWN_EGG, "{Particle:\"angry_villager\",Radius:1.0f,RadiusOnUse:1.0f,Duration:10000,id:\"minecraft:area_effect_cloud\",RadiusPerTick:10.0f}"),
        Triple.of("armor_stand_spawner_minecart", Items.BAT_SPAWN_EGG, "{SpawnData:{id:\"minecraft:armor_stand\"},id:\"minecraft:spawner_minecart\"}"),
        Triple.of("dud_tnt", Items.DROWNED_SPAWN_EGG, "{Fuse:30000,Invulnerable:1b,id:\"minecraft:tnt\"}")
    );

    private static final List<Triple<String, Item, String>> BLOCK_PRESETS = Arrays.asList(
        Triple.of("lag_spawner", Items.SPAWNER, "{MaxNearbyEntities:32767,RequiredPlayerRange:32767,SpawnCount:50,MaxSpawnDelay:0,id:\"minecraft:spawner\",SpawnRange:32767,Delay:0,MinSpawnDelay:0}"),
        Triple.of("tnt_spawner", Items.SPAWNER, "{MaxNearbyEntities:32767,RequiredPlayerRange:32767,SpawnCount:50,SpawnData:{entity:{id:\"minecraft:tnt\",fuse:1}},MaxSpawnDelay:0,id:\"minecraft:mob_spawner\",SpawnRange:10,Delay:0,MinSpawnDelay:0}"),
        Triple.of("boat_spawner", Items.SPAWNER, "{SpawnCount:50,SpawnData:{entity:{Type:\"jungle\",CustomName:'{\"bold\":true,\"color\":\"aqua\",\"italic\":true,\"text\":\"Boat\",\"underlined\":true}',Invulnerable:1b,id:\"minecraft:boat\",Glowing:1b,CustomNameVisible:1b}},id:\"minecraft:spawner\",SpawnRange:10}")
    );

    private static final Random random = new Random();

    public static void giveItem(ItemStack item) throws CommandSyntaxException {
        if (!mc.player.getAbilities().creativeMode) throw NOT_IN_CREATIVE.create();

        if (!mc.player.getInventory().insertStack(item)) {
            throw NO_SPACE.create();
        }
    }

    static {
        ENTITY_PRESETS.forEach((preset) -> {
            PRESETS.put(preset.getLeft(), (preview) -> {
                if (preview) preset.getMiddle().getDefaultStack();
                ItemStack item = preset.getMiddle().getDefaultStack();
                try {
                    item.set(DataComponentTypes.ENTITY_DATA, NbtComponent.of(StringNbtReader.parse(preset.getRight())));
                } catch (CommandSyntaxException e) { }
                item.set(DataComponentTypes.CUSTOM_NAME, Text.literal(toName(preset.getLeft())));
                return item;
            });
        });

        BLOCK_PRESETS.forEach((preset) -> {
            PRESETS.put(preset.getLeft(), (preview) -> {
                if (preview) preset.getMiddle().getDefaultStack();
                ItemStack item = preset.getMiddle().getDefaultStack();
                try {
                    item.set(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.of(StringNbtReader.parse(preset.getRight())));
                } catch (CommandSyntaxException e) { }
                item.set(DataComponentTypes.CUSTOM_NAME, Text.literal(toName(preset.getLeft())));
                return item;
            });
        });

        // TODO update
        PRESETS.put("force_op", (preview) -> {
            if (preview) Items.SPIDER_SPAWN_EGG.getDefaultStack();
            ItemStack item = Items.SPIDER_SPAWN_EGG.getDefaultStack();
            String nick = mc.player.getName().getString();

            try {
                item.set(DataComponentTypes.ENTITY_DATA, NbtComponent.of(StringNbtReader.parse("{Time:1,BlockState:{Name:\"minecraft:spawner\"},id:\"minecraft:falling_block\",TileEntityData:{SpawnCount:20,SpawnData:{id:\"minecraft:villager\",Passengers:[{Time:1,BlockState:{Name:\"minecraft:redstone_block\"},id:\"minecraft:falling_block\",Passengers:[{id:\"minecraft:fox\",Passengers:[{Time:1,BlockState:{Name:\"minecraft:activator_rail\"},id:\"minecraft:falling_block\",Passengers:[{Command:\"execute as @e run op "+nick+"\",id:\"minecraft:command_block_minecart\"}]}],NoAI:1b,Health:1.0f,ActiveEffects:[{Duration:1000,Id:20b,Amplifier:4b}]}]}],NoAI:1b,Health:1.0f,ActiveEffects:[{Duration:1000,Id:20b,Amplifier:4b}]},MaxSpawnDelay:100,SpawnRange:10,Delay:1,MinSpawnDelay:100}}")));
            } catch (CommandSyntaxException e) { }
            item.set(DataComponentTypes.CUSTOM_NAME, Text.of("Force OP"));
            return item;
        });

        // Thanks wurst !
        PRESETS.put("troll_potion", (preview) -> {
            if (preview) Items.LINGERING_POTION.getDefaultStack();
            ItemStack stack = Items.LINGERING_POTION.getDefaultStack();
            ArrayList<StatusEffectInstance> effects = new ArrayList<>();
            for(int i = 1; i <= 31; i++)
            {
                StatusEffect effect =
                        Registries.STATUS_EFFECT.getEntry(i).get().value();
                RegistryEntry<StatusEffect> entry =
                        Registries.STATUS_EFFECT.getEntry(effect);
                effects.add(new StatusEffectInstance(entry, Integer.MAX_VALUE,
                        Integer.MAX_VALUE));
            }

            stack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(), Optional.empty(),
                    effects));
            stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Lingering Potion of Trolling"));
            return stack;
        });

        PRESETS.put("32k", (preview) -> {
            if (preview) return Items.DIAMOND_SWORD.getDefaultStack();
            ItemStack stack = Items.DIAMOND_SWORD.getDefaultStack();

            stack.apply(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT, component -> {
                ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(component);
                builder.add(Enchantments.SHARPNESS, 255);
                builder.add(Enchantments.KNOCKBACK, 255);
                builder.add(Enchantments.FIRE_ASPECT, 255);
                builder.add(Enchantments.LOOTING, 10);
                builder.add(Enchantments.SWEEPING_EDGE, 3);
                builder.add(Enchantments.UNBREAKING, 255);
                builder.add(Enchantments.MENDING, 1);
                builder.add(Enchantments.VANISHING_CURSE, 1);
                return builder.build();
            });

            stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Bonk"));
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
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbtCompound));
            stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Copy Me"));
            return stack;
        });

        PRESETS.put("firework", (preview) -> {
            if (preview) return Items.FIREWORK_ROCKET.getDefaultStack();
            ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);
            IntList colors = new IntArrayList(new int[]{1973019,11743532,3887386,5320730,2437522,8073150,2651799,11250603,4408131,14188952,4312372,14602026,6719955,12801229,15435844,15790320});
            ArrayList<FireworkExplosionComponent> explosions = new ArrayList<>();
            for(int i = 0; i < 200; i++) {
                explosions.add(new FireworkExplosionComponent(FireworkExplosionComponent.Type.byId(random.nextInt(5)), colors, colors, true, true));
            }

            var changes = ComponentChanges.builder()
                    .add(DataComponentTypes.FIREWORKS, new FireworksComponent(1, explosions))
                    .build();

            firework.applyChanges(changes);

            return firework;
        });

        HIDDEN_ENTITIES.forEach((id) -> {
            PRESETS.put(id.getPath()+"_spawn_egg", (preview) -> {
                if (preview) return Items.PIG_SPAWN_EGG.getDefaultStack();
                ItemStack egg = Items.PIG_SPAWN_EGG.getDefaultStack();

                NbtCompound entityTag = new NbtCompound();
                entityTag.putString("id", id.toString());

                var changes = ComponentChanges.builder()
                        .add(DataComponentTypes.CUSTOM_NAME, Text.literal(String.format("%s", toName(id.getPath()))))
                        .add(DataComponentTypes.ENTITY_DATA, NbtComponent.of(entityTag))
                        .build();

                egg.applyChanges(changes);
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

}
