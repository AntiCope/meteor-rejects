package anticope.rejects.commands;

import anticope.rejects.arguments.ClientPosArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.phys.Vec3;

public class TeleportCommand extends Command {


    public TeleportCommand() {
        super("teleport", "Sends a packet to the server with new position. Allows to teleport small distances.", "tp");
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        builder.then(argument("pos", ClientPosArgumentType.pos()).executes(ctx -> {
            Vec3 pos = ClientPosArgumentType.getPos(ctx, "pos");
            mc.player.absSnapTo(pos.x(), pos.y(), pos.z());
            return SINGLE_SUCCESS;
        }));

        builder.then(argument("pos", ClientPosArgumentType.pos()).then(argument("yaw", FloatArgumentType.floatArg()).then(argument("pitch", FloatArgumentType.floatArg()).executes(ctx -> {
            Vec3 pos = ClientPosArgumentType.getPos(ctx, "pos");
            float yaw = FloatArgumentType.getFloat(ctx, "yaw");
            float pitch = FloatArgumentType.getFloat(ctx, "pitch");
            mc.player.absSnapTo(pos.x(), pos.y(), pos.z(), yaw, pitch);
            return SINGLE_SUCCESS;
        }))));
    }
}
