package anticope.rejects.commands;

import anticope.rejects.arguments.ClientPosArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SetBlockCommand extends Command {
    public SetBlockCommand() {
        super("setblock", "Sets client side blocks", "sblk");
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        builder.then(argument("pos", ClientPosArgumentType.pos()).then(argument("block", BlockStateArgument.block(REGISTRY_ACCESS)).executes(ctx -> {
            Vec3 pos = ClientPosArgumentType.getPos(ctx, "pos");
            BlockState blockState = ctx.getArgument("block", BlockInput.class).getState();
            mc.level.setBlockAndUpdate(new BlockPos((int) pos.x(), (int) pos.y(), (int) pos.z()), blockState);

            return SINGLE_SUCCESS;
        })));
    }
}
