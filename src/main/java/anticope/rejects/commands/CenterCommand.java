package anticope.rejects.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.systems.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class CenterCommand extends Command {
    public CenterCommand() {
        super("center", "Centers the player on a block.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("middle").executes(context -> {
            double x = MathHelper.floor(mc.player.getX()) + 0.5;
            double z = MathHelper.floor(mc.player.getZ()) + 0.5;
            mc.player.setPosition(x, mc.player.getY(), z);
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()));

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("center").executes(context -> {
            double x = MathHelper.floor(mc.player.getX());
            double z = MathHelper.floor(mc.player.getZ());
            mc.player.setPosition(x, mc.player.getY(), z);
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()));

            return SINGLE_SUCCESS;
        }));
    }
}