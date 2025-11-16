package anticope.rejects.commands;

import anticope.rejects.arguments.EnumStringArgumentType;
import anticope.rejects.utils.GiveUtils;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TypedEntityData;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import java.util.Collection;

import static anticope.rejects.utils.accounts.GetPlayerUUID.getUUID;

public class GiveCommand extends Command {

    private final Collection<String> PRESETS = GiveUtils.PRESETS.keySet();

    public GiveCommand() {
        super("give", "Gives items in creative", "item", "kit");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        // TODO : finish this
        builder.then(literal("egg").executes(ctx -> {
            ItemStack inHand = mc.player.getMainHandStack();
            ItemStack item = new ItemStack(Items.STRIDER_SPAWN_EGG);
            NbtCompound ct = new NbtCompound();

            if (inHand.getItem() instanceof BlockItem) {
                ct.putInt("Time", 1);
                ct.putString("id", "minecraft:falling_block");
                ct.put("BlockState", new NbtCompound());
                ct.getCompound("BlockState").ifPresent(compound ->
                    compound.put("Name", net.minecraft.nbt.NbtString.of(Registries.ITEM.getId(inHand.getItem()).toString()))
                );

            } else {
                ct.putString("id", "minecraft:item");
                NbtCompound itemTag = new NbtCompound();
                itemTag.putString("id", Registries.ITEM.getId(inHand.getItem()).toString());
                itemTag.putInt("Count", inHand.getCount());

                ct.put("Item", itemTag);
            }
            NbtCompound t = new NbtCompound();
            t.put("EntityTag", ct);

            var changes = ComponentChanges.builder()
                    .add(DataComponentTypes.CUSTOM_NAME, inHand.getName())
                    .add(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(t))
                    .build();

            item.applyChanges(changes);
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
                    net.minecraft.util.math.Vec3d pos = anticope.rejects.arguments.ClientPosArgumentType.getPos(ctx, "pos");
                    return createHolo(message, pos.x, pos.y, pos.z);
                })))
        );

        builder.then(literal("bossbar").then(argument("message", StringArgumentType.greedyString()).executes(ctx -> {
            String message = ctx.getArgument("message", String.class).replace("&", "\247");
            ItemStack stack = new ItemStack(Items.BAT_SPAWN_EGG);
            NbtCompound tag = new NbtCompound();
            tag.putBoolean("NoAI", true);
            tag.putBoolean("Silent", true);
            tag.putBoolean("PersistenceRequired", true);
            tag.putBoolean("Invulnerable", true);

            // Add invisibility potion effect
            NbtList effects = new NbtList();
            NbtCompound invisEffect = new NbtCompound();
            invisEffect.putString("id", "minecraft:invisibility");
            invisEffect.putInt("amplifier", 0);
            invisEffect.putInt("duration", 2147483647); // Max int value for permanent effect
            invisEffect.putBoolean("ambient", false);
            invisEffect.putBoolean("show_particles", false);
            effects.add(invisEffect);
            tag.put("active_effects", effects);

            var changes = ComponentChanges.builder()
                    .add(DataComponentTypes.CUSTOM_NAME, Text.literal(message))
                    .add(DataComponentTypes.ENTITY_DATA, TypedEntityData.create(EntityType.WITHER, tag))
                    .build();
            stack.applyChanges(changes);

            GiveUtils.giveItem(stack);
            return SINGLE_SUCCESS;
        })));

        // TODO : resolve textures, should be easy now that UUID is resolved
        builder.then(literal("head").then(argument("owner", StringArgumentType.greedyString()).executes(ctx -> {
            String playerName = ctx.getArgument("owner", String.class);
            ItemStack itemStack = new ItemStack(Items.PLAYER_HEAD);

            var changes = ComponentChanges.builder()
                    .add(DataComponentTypes.PROFILE, net.minecraft.component.type.ProfileComponent.ofStatic(new GameProfile(getUUID(playerName), playerName)))
                    .build();

            itemStack.applyChanges(changes);

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
        NbtCompound tag = new NbtCompound();
        NbtList pos = new NbtList();

        pos.add(NbtDouble.of(x));
        pos.add(NbtDouble.of(y));
        pos.add(NbtDouble.of(z));

        tag.put("Pos", pos);
        tag.putBoolean("Invisible", true);
        tag.putBoolean("Invulnerable", true);
        tag.putBoolean("NoGravity", true);
        tag.putBoolean("CustomNameVisible", true);

        var changes = ComponentChanges.builder()
                .add(DataComponentTypes.CUSTOM_NAME, Text.literal(message))
                .add(DataComponentTypes.ENTITY_DATA, TypedEntityData.create(EntityType.ARMOR_STAND, tag))
                .build();

        stack.applyChanges(changes);
        GiveUtils.giveItem(stack);
        return SINGLE_SUCCESS;
    }
}
