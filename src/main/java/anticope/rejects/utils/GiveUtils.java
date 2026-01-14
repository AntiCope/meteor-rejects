package anticope.rejects.utils;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import java.util.*;
import java.util.function.Function;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.Triple;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class GiveUtils {

    public static final Map<String, Function<Boolean, ItemStack>> PRESETS = new HashMap<>();

    private final static SimpleCommandExceptionType NOT_IN_CREATIVE = new SimpleCommandExceptionType(Component.literal("You must be in creative mode to use this."));
    private final static SimpleCommandExceptionType NO_SPACE = new SimpleCommandExceptionType(Component.literal("No space in hotbar."));

    private static final List<ResourceLocation> HIDDEN_ENTITIES = Arrays.asList(
        ResourceLocation.parse("giant"),
        ResourceLocation.parse("ender_dragon"),
        ResourceLocation.parse("wither"),
        ResourceLocation.parse("iron_golem"),
        ResourceLocation.parse("ender_dragon"),
        ResourceLocation.parse("tnt_minecart"),
        ResourceLocation.parse("lightning_bolt"));

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
    private static Registry<Enchantment> enchantmentRegistry;

    public static void giveItem(ItemStack item) throws CommandSyntaxException {
        if (!mc.player.getAbilities().instabuild) throw NOT_IN_CREATIVE.create();

        if (!mc.player.getInventory().add(item)) {
            throw NO_SPACE.create();
        }
    }

    static {
        ENTITY_PRESETS.forEach((preset) -> {
            PRESETS.put(preset.getLeft(), (preview) -> {
                if (preview) preset.getMiddle().getDefaultInstance();
                ItemStack item = preset.getMiddle().getDefaultInstance();
                try {
                    CompoundTag compound = TagParser.parseCompoundFully(preset.getRight());
                    String entityId = compound.getString("id").orElse("minecraft:pig");
                    net.minecraft.world.entity.EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(ResourceLocation.parse(entityId));
                    item.set(DataComponents.ENTITY_DATA, net.minecraft.world.item.component.TypedEntityData.of(entityType, compound));
                } catch (CommandSyntaxException e) { }
                item.set(DataComponents.CUSTOM_NAME, Component.literal(toName(preset.getLeft())));
                return item;
            });
        });

        BLOCK_PRESETS.forEach((preset) -> {
            PRESETS.put(preset.getLeft(), (preview) -> {
                if (preview) preset.getMiddle().getDefaultInstance();
                ItemStack item = preset.getMiddle().getDefaultInstance();
                try {
                    CompoundTag compound = TagParser.parseCompoundFully(preset.getRight());
                    String blockEntityId = compound.getString("id").orElse("minecraft:spawner");
                    net.minecraft.world.level.block.entity.BlockEntityType<?> blockEntityType = BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(ResourceLocation.parse(blockEntityId));
                    item.set(DataComponents.BLOCK_ENTITY_DATA, net.minecraft.world.item.component.TypedEntityData.of(blockEntityType, compound));
                } catch (CommandSyntaxException e) { }
                item.set(DataComponents.CUSTOM_NAME, Component.literal(toName(preset.getLeft())));
                return item;
            });
        });

        // TODO update
        PRESETS.put("force_op", (preview) -> {
            if (preview) Items.SPIDER_SPAWN_EGG.getDefaultInstance();
            ItemStack item = Items.SPIDER_SPAWN_EGG.getDefaultInstance();
            String nick = mc.player.getName().getString();

            try {
                CompoundTag compound = TagParser.parseCompoundFully("{Time:1,BlockState:{Name:\"minecraft:spawner\"},id:\"minecraft:falling_block\",TileEntityData:{SpawnCount:20,SpawnData:{id:\"minecraft:villager\",Passengers:[{Time:1,BlockState:{Name:\"minecraft:redstone_block\"},id:\"minecraft:falling_block\",Passengers:[{id:\"minecraft:fox\",Passengers:[{Time:1,BlockState:{Name:\"minecraft:activator_rail\"},id:\"minecraft:falling_block\",Passengers:[{Command:\"execute as @e run op "+nick+"\",id:\"minecraft:command_block_minecart\"}]}],NoAI:1b,Health:1.0f,ActiveEffects:[{Duration:1000,Id:20b,Amplifier:4b}]}]}],NoAI:1b,Health:1.0f,ActiveEffects:[{Duration:1000,Id:20b,Amplifier:4b}]},MaxSpawnDelay:100,SpawnRange:10,Delay:1,MinSpawnDelay:100}}");
                item.set(DataComponents.ENTITY_DATA, net.minecraft.world.item.component.TypedEntityData.of(net.minecraft.world.entity.EntityType.FALLING_BLOCK, compound));
            } catch (CommandSyntaxException e) { }
            item.set(DataComponents.CUSTOM_NAME, Component.nullToEmpty("Force OP"));
            return item;
        });

        // Thanks wurst !
        PRESETS.put("troll_potion", (preview) -> {
            if (preview) Items.LINGERING_POTION.getDefaultInstance();
            ItemStack stack = Items.LINGERING_POTION.getDefaultInstance();
            ArrayList<MobEffectInstance> effects = new ArrayList<>();
            for(int i = 1; i <= 31; i++)
            {
                MobEffect effect =
                        BuiltInRegistries.MOB_EFFECT.get(i).get().value();
                Holder<MobEffect> entry =
                        BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect);
                effects.add(new MobEffectInstance(entry, Integer.MAX_VALUE,
                        Integer.MAX_VALUE));
            }

            stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.empty(),
                    effects, Optional.empty()));
            stack.set(DataComponents.CUSTOM_NAME, Component.literal("Lingering Potion of Trolling"));
            return stack;
        });

        PRESETS.put("32k", (preview) -> {
            enchantmentRegistry = mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

            if (preview || enchantmentRegistry == null) return Items.DIAMOND_SWORD.getDefaultInstance();
            ItemStack stack = Items.DIAMOND_SWORD.getDefaultInstance();

            stack.update(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY, component -> {
                ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(component);
                builder.upgrade(enchantmentRegistry.getOrThrow(Enchantments.SHARPNESS), 255);
                builder.upgrade(enchantmentRegistry.getOrThrow(Enchantments.KNOCKBACK), 255);
                builder.upgrade(enchantmentRegistry.getOrThrow(Enchantments.FIRE_ASPECT), 255);
                builder.upgrade(enchantmentRegistry.getOrThrow(Enchantments.LOOTING), 10);
                builder.upgrade(enchantmentRegistry.getOrThrow(Enchantments.SWEEPING_EDGE), 3);
                builder.upgrade(enchantmentRegistry.getOrThrow(Enchantments.UNBREAKING), 255);
                builder.upgrade(enchantmentRegistry.getOrThrow(Enchantments.MENDING), 1);
                builder.upgrade(enchantmentRegistry.getOrThrow(Enchantments.VANISHING_CURSE), 1);
                return builder.toImmutable();
            });

            stack.set(DataComponents.CUSTOM_NAME, Component.literal("Bonk"));
            return stack;
        });

        PRESETS.put("crash_chest", (preview) -> {
            if (preview) return Items.CHEST.getDefaultInstance();
            ItemStack stack = Items.CHEST.getDefaultInstance();
            CompoundTag nbtCompound = new CompoundTag();
            ListTag nbtList = new ListTag();
            for(int i = 0; i < 40000; i++)
                nbtList.add(new ListTag());
            nbtCompound.put("nothingsuspicioushere", nbtList);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbtCompound));
            stack.set(DataComponents.CUSTOM_NAME, Component.literal("Copy Me"));
            return stack;
        });

        PRESETS.put("firework", (preview) -> {
            if (preview) return Items.FIREWORK_ROCKET.getDefaultInstance();
            ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);
            IntList colors = new IntArrayList(new int[]{1973019,11743532,3887386,5320730,2437522,8073150,2651799,11250603,4408131,14188952,4312372,14602026,6719955,12801229,15435844,15790320});
            ArrayList<FireworkExplosion> explosions = new ArrayList<>();
            for(int i = 0; i < 200; i++) {
                explosions.add(new FireworkExplosion(FireworkExplosion.Shape.byId(random.nextInt(5)), colors, colors, true, true));
            }

            var changes = DataComponentPatch.builder()
                    .set(DataComponents.FIREWORKS, new Fireworks(1, explosions))
                    .build();

            firework.applyComponentsAndValidate(changes);

            return firework;
        });

        HIDDEN_ENTITIES.forEach((id) -> {
            PRESETS.put(id.getPath()+"_spawn_egg", (preview) -> {
                if (preview) return Items.PIG_SPAWN_EGG.getDefaultInstance();
                ItemStack egg = Items.PIG_SPAWN_EGG.getDefaultInstance();

                CompoundTag entityTag = new CompoundTag();
                net.minecraft.world.entity.EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(id);

                var changes = DataComponentPatch.builder()
                        .set(DataComponents.CUSTOM_NAME, Component.literal(String.format("%s", toName(id.getPath()))))
                        .set(DataComponents.ENTITY_DATA, net.minecraft.world.item.component.TypedEntityData.of(entityType, entityTag))
                        .build();

                egg.applyComponentsAndValidate(changes);
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
