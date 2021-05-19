package cloudburst.rejects.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;

import cloudburst.rejects.arguments.EnumStringArgumentType;
import minegame159.meteorclient.systems.commands.Command;
import minegame159.meteorclient.utils.player.SlotUtils;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;

import static cloudburst.rejects.utils.GiveUtils.createPreset;
import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

import java.util.*;

public class GiveCommand extends Command {

    public GiveCommand() {
        super("give", "Gives items in creative", "item", "kit");
    }

    private final Collection<String> PRESETS = Arrays.asList("forceop", "negs", "stacked", "spawners", "bookban",
            "test", "eggs");
    private final Collection<String> CONTAINERS = Arrays.asList("chest", "shulker", "trapped_chest", "barrel",
            "dispenser", "egg");

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
            addItem(item);
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("holo").then(argument("message", StringArgumentType.greedyString()).executes(ctx -> {
            if (!mc.player.abilities.creativeMode) {
                error("Not In Creative Mode!");
                return SINGLE_SUCCESS;
            }
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
            addItem(stack);
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("firework").executes(ctx -> {
            if (!mc.player.abilities.creativeMode) {
                error("Not In Creative Mode!");
                return SINGLE_SUCCESS;
            }
            ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);
            CompoundTag baseCompound = new CompoundTag();
            CompoundTag tagCompound = new CompoundTag();
            ListTag explosionList = new ListTag();

            for(int i = 0; i < 5000; i++)
            {
                CompoundTag explosionCompound = new CompoundTag();

                Random rand = new Random();
                explosionCompound.putByte("Type", (byte)rand.nextInt(5));

                int colors[] = {1973019,11743532,3887386,5320730,2437522,8073150,2651799,11250603,4408131,14188952,4312372,14602026,6719955,12801229,15435844,15790320};

                explosionCompound.putIntArray("Colors", colors);
                explosionList.add(explosionCompound);
            }


            tagCompound.putInt("Flight", 0);
            tagCompound.put("Explosions", explosionList);
            baseCompound.put("Fireworks", tagCompound);
            firework.setTag(baseCompound);
            addItem(firework);
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("head").then(argument("owner",StringArgumentType.greedyString()).executes(ctx -> {
            if (!mc.player.abilities.creativeMode) {
                error("Not In Creative Mode!");
                return SINGLE_SUCCESS;
            }
            String playerName = ctx.getArgument("owner",String.class);
            ItemStack itemStack = new ItemStack(Items.PLAYER_HEAD);
            CompoundTag tag = new CompoundTag();
            tag.putString("SkullOwner", playerName);
            itemStack.setTag(tag);
            addItem(itemStack);
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("preset").then(argument("name", new EnumStringArgumentType(PRESETS))
                .then(argument("container", new EnumStringArgumentType(CONTAINERS)).executes(context -> {
                    if (!mc.player.abilities.creativeMode) {
                        error("Not In Creative Mode!");
                        return SINGLE_SUCCESS;
                    }
                    String name = context.getArgument("name", String.class);
                    String container = context.getArgument("container", String.class);
                    addItem(createPreset(name, container));
                    return SINGLE_SUCCESS;
                }))));
    }

    private void addItem(ItemStack item) {
		for(int i = 0; i < 36; i++) {
		    ItemStack stack = mc.player.inventory.getStack(SlotUtils.indexToId(i));
			if (!stack.isEmpty()) continue;
			mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(SlotUtils.indexToId(i), item));
			return;
		}
        error("No space in inventory.");
    }
}
