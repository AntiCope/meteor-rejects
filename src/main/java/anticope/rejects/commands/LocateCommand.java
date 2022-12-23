package anticope.rejects.commands;

import anticope.rejects.arguments.EnumArgumentType;
import anticope.rejects.utils.WorldGenUtils;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import meteordevelopment.meteorclient.systems.commands.Command;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class LocateCommand extends Command {

    private final static DynamicCommandExceptionType NOT_FOUND = new DynamicCommandExceptionType(o -> {
        if (o instanceof WorldGenUtils.Feature) {
            return Text.literal(String.format(
                    "%s not found.",
                    Utils.nameToTitle(o.toString().replaceAll("_", "-")))
            );
        }
        return Text.literal("Not found.");
    });

    public LocateCommand() {
        super("locate", "Locates structures.", "loc");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("feature").then(argument("feature", EnumArgumentType.enumArgument(WorldGenUtils.Feature.stronghold)).executes(ctx -> {
            WorldGenUtils.Feature feature = EnumArgumentType.getEnum(ctx, "feature", WorldGenUtils.Feature.stronghold);
            BlockPos pos = WorldGenUtils.locateFeature(feature, mc.player.getBlockPos());
            if (pos != null) {
                MutableText text = Text.literal(String.format(
                        "%s located at ",
                        Utils.nameToTitle(feature.toString().replaceAll("_", "-"))
                ));
                Vec3d coords = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
                text.append(ChatUtils.formatCoords(coords));
                text.append(".");
                info(text);
                return SINGLE_SUCCESS;
            }
            throw NOT_FOUND.create(feature);
        })));
    }
}
