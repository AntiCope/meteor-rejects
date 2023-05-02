package anticope.rejects.commands;

import anticope.rejects.arguments.ClientPosArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.Vec3d;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class TeleportCommand extends Command {


    public TeleportCommand() {
        super("teleport", "Sends a packet to the server with new position. Allows to teleport small distances.", "tp");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("pos", ClientPosArgumentType.pos()).executes(ctx -> {
            Vec3d pos = ClientPosArgumentType.getPos(ctx, "pos");
            mc.player.updatePosition(pos.getX(), pos.getY(), pos.getZ());
            return SINGLE_SUCCESS;
        }));

        builder.then(argument("pos", ClientPosArgumentType.pos()).then(argument("yaw", FloatArgumentType.floatArg()).then(argument("pitch", FloatArgumentType.floatArg()).executes(ctx -> {
            Vec3d pos = ClientPosArgumentType.getPos(ctx, "pos");
            float yaw = FloatArgumentType.getFloat(ctx, "yaw");
            float pitch = FloatArgumentType.getFloat(ctx, "pitch");
            mc.player.updatePositionAndAngles(pos.getX(), pos.getY(), pos.getZ(), yaw, pitch);
            return SINGLE_SUCCESS;
        }))));
    }
}
