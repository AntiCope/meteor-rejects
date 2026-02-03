package anticope.rejects.commands;

import anticope.rejects.arguments.EnumStringArgumentType;
import anticope.rejects.utils.GiveUtils;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TypedEntityData;
import java.util.Collection;

import static anticope.rejects.utils.accounts.GetPlayerUUID.getUUID;

public class GiveCommand extends Command {

    private final Collection<String> PRESETS = GiveUtils.PRESETS.keySet();

    public GiveCommand() {
        super("give", "Gives items in creative", "item", "kit");
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        // TODO : finish this
        builder.then(literal("egg").executes(ctx -> {
            ItemStack inHand = mc.player.getMainHandItem();
            ItemStack item = new ItemStack(Items.STRIDER_SPAWN_EGG);
            CompoundTag ct = new CompoundTag();

            if (inHand.getItem() instanceof BlockItem) {
                ct.putInt("Time", 1);
                ct.putString("id", "minecraft:falling_block");
                ct.put("BlockState", new CompoundTag());
                ct.getCompound("BlockState").ifPresent(compound ->
                    compound.put("Name", net.minecraft.nbt.StringTag.valueOf(BuiltInRegistries.ITEM.getKey(inHand.getItem()).toString()))
                );

            } else {
                ct.putString("id", "minecraft:item");
                CompoundTag itemTag = new CompoundTag();
                itemTag.putString("id", BuiltInRegistries.ITEM.getKey(inHand.getItem()).toString());
                itemTag.putInt("Count", inHand.getCount());

                ct.put("Item", itemTag);
            }
            CompoundTag t = new CompoundTag();
            t.put("EntityTag", ct);

            var changes = DataComponentPatch.builder()
                    .set(DataComponents.CUSTOM_NAME, inHand.getHoverName())
                    .set(DataComponents.CUSTOM_DATA, CustomData.of(t))
                    .build();

            item.applyComponentsAndValidate(changes);
            GiveUtils.giveItem(item);
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("holo")
            .then(argument("message", StringArgumentType.greedyString()).executes(ctx -> {
                String message = ctx.getArgument("message", String.class).replace("&", "\247");
                return createHolo(message, mc.player.getX(), mc.player.getY(), mc.player.getZ());
            }))
            .then(argument("pos", anticope.rejects.arguments.ClientPosArgumentType.pos())
                .then(argument("message", StringArgumentType.greedyString()).executes(ctx -> {
                    String message = ctx.getArgument("message", String.class).replace("&", "\247");
                    net.minecraft.world.phys.Vec3 pos = anticope.rejects.arguments.ClientPosArgumentType.getPos(ctx, "pos");
                    return createHolo(message, pos.x, pos.y, pos.z);
                })))
        );

        builder.then(literal("bossbar").then(argument("message", StringArgumentType.greedyString()).executes(ctx -> {
            String message = ctx.getArgument("message", String.class).replace("&", "\247");
            ItemStack stack = new ItemStack(Items.BAT_SPAWN_EGG);
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("NoAI", true);
            tag.putBoolean("Silent", true);
            tag.putBoolean("PersistenceRequired", true);
            tag.putBoolean("Invulnerable", true);

            // Add invisibility potion effect
            ListTag effects = new ListTag();
            CompoundTag invisEffect = new CompoundTag();
            invisEffect.putString("id", "minecraft:invisibility");
            invisEffect.putInt("amplifier", 0);
            invisEffect.putInt("duration", 2147483647); // Max int value for permanent effect
            invisEffect.putBoolean("ambient", false);
            invisEffect.putBoolean("show_particles", false);
            effects.add(invisEffect);
            tag.put("active_effects", effects);

            var changes = DataComponentPatch.builder()
                    .set(DataComponents.CUSTOM_NAME, Component.literal(message))
                    .set(DataComponents.ENTITY_DATA, TypedEntityData.of(EntityType.WITHER, tag))
                    .build();
            stack.applyComponentsAndValidate(changes);

            GiveUtils.giveItem(stack);
            return SINGLE_SUCCESS;
        })));

        // TODO : resolve textures, should be easy now that UUID is resolved
        builder.then(literal("head").then(argument("owner", StringArgumentType.greedyString()).executes(ctx -> {
            String playerName = ctx.getArgument("owner", String.class);
            ItemStack itemStack = new ItemStack(Items.PLAYER_HEAD);

            var changes = DataComponentPatch.builder()
                    .set(DataComponents.PROFILE, net.minecraft.world.item.component.ResolvableProfile.createResolved(new GameProfile(getUUID(playerName), playerName)))
                    .build();

            itemStack.applyComponentsAndValidate(changes);

            GiveUtils.giveItem(itemStack);
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("preset").then(argument("name", new EnumStringArgumentType(PRESETS)).executes(context -> {
            String name = context.getArgument("name", String.class);
            GiveUtils.giveItem(GiveUtils.getPreset(name));
            return SINGLE_SUCCESS;
        })));
    }

    private int createHolo(String message, double x, double y, double z) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ItemStack stack = new ItemStack(Items.STRIDER_SPAWN_EGG);
        CompoundTag tag = new CompoundTag();
        ListTag pos = new ListTag();

        pos.add(DoubleTag.valueOf(x));
        pos.add(DoubleTag.valueOf(y));
        pos.add(DoubleTag.valueOf(z));

        tag.put("Pos", pos);
        tag.putBoolean("Invisible", true);
        tag.putBoolean("Invulnerable", true);
        tag.putBoolean("NoGravity", true);
        tag.putBoolean("CustomNameVisible", true);

        var changes = DataComponentPatch.builder()
                .set(DataComponents.CUSTOM_NAME, Component.literal(message))
                .set(DataComponents.ENTITY_DATA, TypedEntityData.of(EntityType.ARMOR_STAND, tag))
                .build();

        stack.applyComponentsAndValidate(changes);
        GiveUtils.giveItem(stack);
        return SINGLE_SUCCESS;
    }
}
