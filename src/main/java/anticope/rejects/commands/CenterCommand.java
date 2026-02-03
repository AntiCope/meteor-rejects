package anticope.rejects.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;

public class CenterCommand extends Command {
    public CenterCommand() {
        super("center", "Centers the player on a block.");
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        builder.then(literal("middle").executes(context -> {
            double x = Mth.floor(mc.player.getX()) + 0.5;
            double z = Mth.floor(mc.player.getZ()) + 0.5;
            mc.player.setPos(x, mc.player.getY(), z);
            mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.onGround(), mc.player.horizontalCollision));

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("center").executes(context -> {
            double x = Mth.floor(mc.player.getX());
            double z = Mth.floor(mc.player.getZ());
            mc.player.setPos(x, mc.player.getY(), z);
            mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.onGround(), mc.player.horizontalCollision));

            return SINGLE_SUCCESS;
        }));
    }
}