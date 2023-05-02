package anticope.rejects.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class SetVelocityCommand extends Command {
    public SetVelocityCommand() {
        super("set-velocity", "Sets player velocity", "velocity", "vel");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("y", DoubleArgumentType.doubleArg()).executes(ctx -> {
            var currentVelocity = mc.player.getVelocity();
            mc.player.setVelocity(currentVelocity.x, DoubleArgumentType.getDouble(ctx, "y"), currentVelocity.z);
            return SINGLE_SUCCESS;
        }));

        builder.then(argument("x", DoubleArgumentType.doubleArg()).then(argument("z", DoubleArgumentType.doubleArg()).executes(ctx -> {
            double x = DoubleArgumentType.getDouble(ctx, "x");
            double z = DoubleArgumentType.getDouble(ctx, "z");
            mc.player.setVelocity(x, mc.player.getVelocity().y, z);
            return SINGLE_SUCCESS;
        })));

        builder.then(argument("x", DoubleArgumentType.doubleArg()).then(argument("y", DoubleArgumentType.doubleArg()).then(argument("z", DoubleArgumentType.doubleArg()).executes(ctx -> {
            double x = DoubleArgumentType.getDouble(ctx, "x");
            double y = DoubleArgumentType.getDouble(ctx, "y");
            double z = DoubleArgumentType.getDouble(ctx, "z");
            mc.player.setVelocity(x, y, z);
            return SINGLE_SUCCESS;
        }))));
    }
}
