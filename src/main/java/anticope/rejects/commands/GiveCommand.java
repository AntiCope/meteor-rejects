package anticope.rejects.commands;

import anticope.rejects.arguments.EnumStringArgumentType;
import anticope.rejects.utils.GiveUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import java.util.Collection;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

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

            NbtCompound itemNbt = inHand
                    .getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT)
                    .copyNbt();

            if (inHand.getItem() instanceof BlockItem) {
                itemNbt.putInt("Time", 1);
                itemNbt.putString("id", "minecraft:falling_block");
                itemNbt.put("BlockState", new NbtCompound());
                itemNbt.getCompound("BlockState").putString("Name", Registries.ITEM.getId(inHand.getItem()).toString());
                if (inHand.getComponents().contains(DataComponentTypes.BLOCK_ENTITY_DATA)) {
                    itemNbt.put("TileEntityData", inHand.get(DataComponentTypes.BLOCK_ENTITY_DATA).copyNbt());
                }
                NbtCompound t = new NbtCompound();
                t.put("EntityTag", ct);
                item.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(itemNbt));
            } else {
                ct.putString("id", "minecraft:item");
                NbtCompound it = new NbtCompound();
                it.putString("id", Registries.ITEM.getId(inHand.getItem()).toString());
                it.putInt("Count", inHand.getCount());
                if (!inHand.getComponents().isEmpty()) {
                    it.put("tag", inHand.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt());
                }
                ct.put("Item", it);
            }
            NbtCompound t = new NbtCompound();
            t.put("EntityTag", ct);
            item.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(t));
            item.set(DataComponentTypes.CUSTOM_NAME, inHand.getName());
            GiveUtils.giveItem(item);
            return SINGLE_SUCCESS;
        }));

        //TODO: allow for custom cords to place oob
        builder.then(literal("holo").then(argument("message", StringArgumentType.greedyString()).executes(ctx -> {
            String message = ctx.getArgument("message", String.class).replace("&", "\247");
            ItemStack stack = new ItemStack(Items.ARMOR_STAND);
            NbtCompound tag = new NbtCompound();
            NbtList NbtList = new NbtList();
            NbtList.add(NbtDouble.of(mc.player.getX()));
            NbtList.add(NbtDouble.of(mc.player.getY()));
            NbtList.add(NbtDouble.of(mc.player.getZ()));
            tag.putBoolean("Invisible", true);
            tag.putBoolean("Invulnerable", true);
            tag.putBoolean("Interpret", true);
            tag.putBoolean("NoGravity", true);
            tag.putBoolean("CustomNameVisible", true);
            tag.putString("CustomName", Text.literal(message).toString());
            tag.put("Pos", NbtList);
            stack.set(DataComponentTypes.ENTITY_DATA, NbtComponent.of(tag));
            GiveUtils.giveItem(stack);
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("bossbar").then(argument("message", StringArgumentType.greedyString()).executes(ctx -> {
            String message = ctx.getArgument("message", String.class).replace("&", "\247");
            ItemStack stack = new ItemStack(Items.BAT_SPAWN_EGG);
            NbtCompound tag = new NbtCompound();
            tag.putString("CustomName", Text.literal(message).toString());
            tag.putBoolean("NoAI", true);
            tag.putBoolean("Silent", true);
            tag.putBoolean("PersistenceRequired", true);
            tag.putBoolean("Invisible", true);
            tag.put("id", NbtString.of("minecraft:wither"));
            stack.set(DataComponentTypes.ENTITY_DATA, NbtComponent.of(tag));
            GiveUtils.giveItem(stack);
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("head").then(argument("owner", StringArgumentType.greedyString()).executes(ctx -> {
            String playerName = ctx.getArgument("owner", String.class);
            ItemStack itemStack = new ItemStack(Items.PLAYER_HEAD);
            NbtCompound tag = new NbtCompound();
            tag.putString("SkullOwner", playerName);
            itemStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(tag));
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
