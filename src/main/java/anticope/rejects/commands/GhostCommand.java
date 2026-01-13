package anticope.rejects.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;

public class GhostCommand extends Command {
    public GhostCommand() {
        super("ghost", "Remove ghost blocks & bypass AntiXray", "aax", "anti-anti-xray");
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        builder.executes(ctx -> {
            execute(4);
            return SINGLE_SUCCESS;
        });
        builder.then(argument("radius", IntegerArgumentType.integer(1)).executes(ctx -> {
            int radius = IntegerArgumentType.getInteger(ctx, "radius");
            execute(radius);
            return SINGLE_SUCCESS;
        }));
    }

    private void execute(int radius) {
        ClientPacketListener conn = mc.getConnection();
        if (conn == null)
            return;
        BlockPos pos = mc.player.blockPosition();
        for (int dx = -radius; dx <= radius; dx++)
            for (int dy = -radius; dy <= radius; dy++)
                for (int dz = -radius; dz <= radius; dz++) {
                    ServerboundPlayerActionPacket packet = new ServerboundPlayerActionPacket(
                            ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK,
                            new BlockPos(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz), Direction.UP);
                    conn.send(packet);
                }
    }
}
