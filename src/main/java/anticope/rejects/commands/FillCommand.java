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

public class FillCommand extends Command {

    public FillCommand() {
        super("fill", "Fills a specified area with a block");
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        builder.then(argument("from-pos", ClientPosArgumentType.pos()).then(argument("to-pos", ClientPosArgumentType.pos()).then(argument("block", BlockStateArgument.block(REGISTRY_ACCESS)).executes(ctx -> {
            Vec3 fromPos = ClientPosArgumentType.getPos(ctx, "from-pos");
            Vec3 toPos = ClientPosArgumentType.getPos(ctx, "to-pos");
            BlockState blockState = ctx.getArgument("block", BlockInput.class).getState();

            fillArea(fromPos, toPos, blockState, null);

            return SINGLE_SUCCESS;
        }))));

        builder.then(argument("from-pos", ClientPosArgumentType.pos()).then(argument("to-pos", ClientPosArgumentType.pos()).then(argument("block", BlockStateArgument.block(REGISTRY_ACCESS)).then(literal("replace").then(argument("filter", BlockStateArgument.block(REGISTRY_ACCESS)).executes(ctx -> {
            Vec3 fromPos = ClientPosArgumentType.getPos(ctx, "from-pos");
            Vec3 toPos = ClientPosArgumentType.getPos(ctx, "to-pos");
            BlockState blockState = ctx.getArgument("block", BlockInput.class).getState();
            BlockState filterBlock = ctx.getArgument("filter", BlockInput.class).getState();

            fillArea(fromPos, toPos, blockState, filterBlock);

            return SINGLE_SUCCESS;
        }))))));
    }

    private void fillArea(Vec3 fromPos, Vec3 toPos, BlockState blockState, BlockState filterBlock) {
        MinMaxCoords coords = getMinMaxCoords(fromPos, toPos);

        for (int x = coords.minX; x <= coords.maxX; x++) {
            for (int y = coords.minY; y <= coords.maxY; y++) {
                for (int z = coords.minZ; z <= coords.maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);

                    if (filterBlock == null || mc.level.getBlockState(pos).equals(filterBlock)) {
                        mc.level.setBlockAndUpdate(pos, blockState);
                    }
                }
            }
        }
    }

    private MinMaxCoords getMinMaxCoords(Vec3 fromPos, Vec3 toPos) {
        int minX = Math.min((int) fromPos.x(), (int) toPos.x());
        int maxX = Math.max((int) fromPos.x(), (int) toPos.x());
        int minY = Math.min((int) fromPos.y(), (int) toPos.y());
        int maxY = Math.max((int) fromPos.y(), (int) toPos.y());
        int minZ = Math.min((int) fromPos.z(), (int) toPos.z());
        int maxZ = Math.max((int) fromPos.z(), (int) toPos.z());

        return new MinMaxCoords(minX, maxX, minY, maxY, minZ, maxZ);
    }

    private static class MinMaxCoords {
        public final int minX, maxX, minY, maxY, minZ, maxZ;

        public MinMaxCoords(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
            this.minZ = minZ;
            this.maxZ = maxZ;
        }
    }
}
