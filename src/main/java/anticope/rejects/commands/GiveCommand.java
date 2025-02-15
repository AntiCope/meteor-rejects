package anticope.rejects.commands;

import anticope.rejects.arguments.ClientPosArgumentType;
import anticope.rejects.arguments.EnumStringArgumentType;
import anticope.rejects.utils.GiveUtils;
import anticope.rejects.utils.accounts.PlayerProfileUtils;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;
import java.util.UUID;

import static anticope.rejects.utils.accounts.PlayerProfileUtils.getUUID;

public class GiveCommand extends Command {

    private final Collection<String> PRESETS = GiveUtils.PRESETS.keySet();

    public GiveCommand() {
        super("give", "Gives items in creative", "item", "kit");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("egg").executes(ctx -> {
            ItemStack inHand = mc.player.getMainHandStack();
            ItemStack item = new ItemStack(Items.STRIDER_SPAWN_EGG);
            NbtCompound ct = new NbtCompound();

            if (inHand.getItem() instanceof BlockItem) {
                ct.putInt("Time", 1);
                ct.putString("id", "minecraft:falling_block");
                ct.put("BlockState", new NbtCompound());
                ct.getCompound("BlockState").putString("Name", Registries.ITEM.getId(inHand.getItem()).toString());

            } else {
                ct.putString("id", "minecraft:item");
                NbtCompound itemTag = new NbtCompound();
                itemTag.putString("id", Registries.ITEM.getId(inHand.getItem()).toString());
                itemTag.putInt("Count", inHand.getCount());

                ct.put("Item", itemTag);
            }

            var changes = ComponentChanges.builder()
                    .add(DataComponentTypes.CUSTOM_NAME, inHand.getName())
                    .add(DataComponentTypes.ENTITY_DATA, NbtComponent.of(ct))
                    .build();

            item.applyChanges(changes);
            GiveUtils.giveItem(item);
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("holo").then(argument("pos", ClientPosArgumentType.pos()).then(argument("message", StringArgumentType.greedyString()).executes(ctx -> {
            String message = ctx.getArgument("message", String.class).replace("&", "\247");
            ItemStack stack = new ItemStack(Items.STRIDER_SPAWN_EGG);
            NbtCompound tag = new NbtCompound();
            NbtList pos = new NbtList();
            Vec3d poss = ClientPosArgumentType.getPos(ctx, "pos");

            pos.add(NbtDouble.of(poss.getX()));
            pos.add(NbtDouble.of(poss.getY()));
            pos.add(NbtDouble.of(poss.getZ()));

            tag.putString("id", "minecraft:armor_stand");
            tag.put("Pos", pos);
            tag.putBoolean("Invisible", true);
            tag.putBoolean("Invulnerable", true);
            tag.putBoolean("NoGravity", true);
            tag.putBoolean("CustomNameVisible", true);

            var changes = ComponentChanges.builder()
                    .add(DataComponentTypes.CUSTOM_NAME, Text.literal(message))
                    .add(DataComponentTypes.ENTITY_DATA, NbtComponent.of(tag))
                    .build();

            stack.applyChanges(changes);
            GiveUtils.giveItem(stack);
            return SINGLE_SUCCESS;
        }))));

        builder.then(literal("bossbar").then(argument("message", StringArgumentType.greedyString()).executes(ctx -> {
            String message = ctx.getArgument("message", String.class).replace("&", "\247");
            ItemStack stack = new ItemStack(Items.BAT_SPAWN_EGG);
            NbtCompound effect = new NbtCompound();
            effect.put("amplifier", NbtInt.of(1));
            effect.put("duration", NbtInt.of(-1));
            effect.put("id", NbtString.of("minecraft:invisibility"));

            NbtList effects = new NbtList();
            effects.add(effect);

            NbtCompound tag = new NbtCompound();
            tag.putBoolean("NoAI", true);
            tag.putBoolean("Silent", true);
            tag.putBoolean("PersistenceRequired", true);
            tag.put("id", NbtString.of("minecraft:wither"));
            tag.put("active_effects", effects);

            var changes = ComponentChanges.builder()
                    .add(DataComponentTypes.CUSTOM_NAME, Text.literal(message))
                    .add(DataComponentTypes.ENTITY_DATA, NbtComponent.of(tag))
                    .build();
            stack.applyChanges(changes);

            GiveUtils.giveItem(stack);
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("head").then(argument("owner", StringArgumentType.greedyString()).executes(ctx -> {
            String playerName = ctx.getArgument("owner", String.class);
            ItemStack itemStack = new ItemStack(Items.PLAYER_HEAD);
            UUID uuid = getUUID(playerName);
            Property property = new Property("textures", PlayerProfileUtils.getPlayerProfile(uuid));
            ProfileComponent profile = new ProfileComponent(new GameProfile(uuid, playerName));
            profile.properties().put("textures", property);

            var changes = ComponentChanges.builder()
                    .add(DataComponentTypes.PROFILE, profile)
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
}
