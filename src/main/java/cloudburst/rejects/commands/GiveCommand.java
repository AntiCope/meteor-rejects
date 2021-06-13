package cloudburst.rejects.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;

import cloudburst.rejects.arguments.EnumStringArgumentType;
import cloudburst.rejects.utils.GiveUtils;
import minegame159.meteorclient.systems.commands.Command;
import minegame159.meteorclient.utils.player.SlotUtils;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

import java.util.*;

public class GiveCommand extends Command {

    public GiveCommand() {
        super("give", "Gives items in creative", "item", "kit");
    }

    private final Collection<String> PRESETS = GiveUtils.PRESETS.keySet();

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("egg").executes(ctx -> {
            ItemStack inHand = mc.player.getMainHandStack();
            ItemStack item = new ItemStack(Items.STRIDER_SPAWN_EGG);
            CompoundTag ct = new CompoundTag();
            if (inHand.getItem() instanceof BlockItem) {
                ct.putInt("Time",1);
                ct.putString("id", "minecraft:falling_block");
                ct.put("BlockState", new CompoundTag());
                ct.getCompound("BlockState").putString("Name", Registry.ITEM.getId(inHand.getItem()).toString());
                if (inHand.hasTag() && inHand.getTag().contains("BlockEntityTag")) {
                    ct.put("TileEntityData", inHand.getTag().getCompound("BlockEntityTag"));
                }
                CompoundTag t = new CompoundTag();
                t.put("EntityTag", ct);
                item.setTag(t);
            } else {
                ct.putString("id", "minecraft:item");
                CompoundTag it = new CompoundTag();
                it.putString("id", Registry.ITEM.getId(inHand.getItem()).toString());
                it.putInt("Count",inHand.getCount());
                if (inHand.hasTag()) {
                    it.put("tag", inHand.getTag());
                }
                ct.put("Item",it);
            }
            CompoundTag t = new CompoundTag();
            t.put("EntityTag", ct);
            item.setTag(t);
            item.setCustomName(inHand.getName());
            GiveUtils.giveItem(item);
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("holo").then(argument("message", StringArgumentType.greedyString()).executes(ctx -> {
            String message = ctx.getArgument("message", String.class);
            message = message.replace("&", "\247");
            ItemStack stack = new ItemStack(Items.ARMOR_STAND);
            CompoundTag tag = new CompoundTag();
            ListTag listTag = new ListTag();
            listTag.add(DoubleTag.of(mc.player.getX()));
            listTag.add(DoubleTag.of(mc.player.getY()));
            listTag.add(DoubleTag.of(mc.player.getZ()));
            tag.putBoolean("Invisible", true);
            tag.putBoolean("Invulnerable", true);
            tag.putBoolean("Interpret", true);
            tag.putBoolean("NoGravity", true);
            tag.putBoolean("CustomNameVisible", true);
            tag.putString("CustomName", Text.Serializer.toJson(new LiteralText(message)));
            tag.put("Pos", listTag);
            stack.putSubTag("EntityTag", tag);
            GiveUtils.giveItem(stack);
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("head").then(argument("owner",StringArgumentType.greedyString()).executes(ctx -> {
            String playerName = ctx.getArgument("owner",String.class);
            ItemStack itemStack = new ItemStack(Items.PLAYER_HEAD);
            CompoundTag tag = new CompoundTag();
            tag.putString("SkullOwner", playerName);
            itemStack.setTag(tag);
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
