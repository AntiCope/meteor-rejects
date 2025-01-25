package anticope.rejects.commands;

import anticope.rejects.arguments.ClientPosArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SetBlockCommand extends Command {
    public SetBlockCommand() {
        super("setblock", "Sets client side blocks", "sblk");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("pos", ClientPosArgumentType.pos()).then(argument("block", BlockStateArgumentType.blockState(REGISTRY_ACCESS)).executes(ctx -> {
            Vec3d pos = ClientPosArgumentType.getPos(ctx, "pos");
            BlockState blockState = ctx.getArgument("block", BlockStateArgument.class).getBlockState();
            mc.world.setBlockState(new BlockPos((int) pos.getX(), (int) pos.getY(), (int) pos.getZ()), blockState);

            return SINGLE_SUCCESS;
        })));
    }
}
