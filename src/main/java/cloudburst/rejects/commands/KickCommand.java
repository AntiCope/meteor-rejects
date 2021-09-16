package cloudburst.rejects.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.text.LiteralText;

import meteordevelopment.meteorclient.systems.commands.Command;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class KickCommand extends Command {

    public KickCommand() {
        super("kick", "Kick or disconnect yourself from the server", "disconnect", "quit);
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("disconnect").executes(ctx -> {
			mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(new LiteralText("Disconnected via .kick command")));
            return SINGLE_SUCCESS;
        }));
        builder.then(literal("pos").executes(ctx -> {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, !mc.player.isOnGround()));
            return SINGLE_SUCCESS;
        }));
        builder.then(literal("hurt").executes(ctx -> {
            mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(mc.player, mc.player.isSneaking()));
            return SINGLE_SUCCESS;
        }));
        builder.then(literal("chat").executes(ctx -> {
            mc.player.sendChatMessage("§0§1§");
            return SINGLE_SUCCESS;
        }));
    }
    
}
