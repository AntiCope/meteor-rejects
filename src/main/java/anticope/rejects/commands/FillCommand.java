package anticope.rejects.commands;

import anticope.rejects.arguments.ClientPosArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class FillCommand extends Command {

    public FillCommand() {
        super("fill", "Fills a specified area with a block");
    }
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("from-pos", ClientPosArgumentType.pos()).then(argument("to-pos", ClientPosArgumentType.pos()).then(argument("block", BlockStateArgumentType.blockState(REGISTRY_ACCESS)).executes(ctx -> {
            Vec3d fromPos = ClientPosArgumentType.getPos(ctx, "from-pos");
            Vec3d toPos = ClientPosArgumentType.getPos(ctx, "to-pos");
            BlockState blockState = ctx.getArgument("block", BlockStateArgument.class).getBlockState();

            fillArea(fromPos, toPos, blockState, null);

            return SINGLE_SUCCESS;
        }))));

        builder.then(argument("from-pos", ClientPosArgumentType.pos()).then(argument("to-pos", ClientPosArgumentType.pos()).then(argument("block", BlockStateArgumentType.blockState(REGISTRY_ACCESS)).then(argument("replace", StringArgumentType.string()).then(argument("filter", BlockStateArgumentType.blockState(REGISTRY_ACCESS)).executes(ctx -> {
            Vec3d fromPos = ClientPosArgumentType.getPos(ctx, "from-pos");
            Vec3d toPos = ClientPosArgumentType.getPos(ctx, "to-pos");
            BlockState findBlock = ctx.getArgument("block", BlockStateArgument.class).getBlockState();
            BlockState filterBlock = ctx.getArgument("filter", BlockStateArgument.class).getBlockState();

            fillArea(fromPos, toPos, findBlock, filterBlock);

            return SINGLE_SUCCESS;
        }))))));
    }

    private void fillArea(Vec3d fromPos, Vec3d toPos, BlockState blockState, BlockState filterBlock) {
        MinMaxCoords coords = getMinMaxCoords(fromPos, toPos);

        for (int x = coords.minX; x <= coords.maxX; x++) {
            for (int y = coords.minY; y <= coords.maxY; y++) {
                for (int z = coords.minZ; z <= coords.maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);

                    if (filterBlock == null || mc.world.getBlockState(pos).equals(filterBlock)) {
                        mc.world.setBlockState(pos, blockState);
                    }
                }
            }
        }
    }

    //Send help
    private MinMaxCoords getMinMaxCoords(Vec3d fromPos, Vec3d toPos) {
        int minX = Math.min((int) fromPos.getX(), (int) toPos.getX());
        int maxX = Math.max((int) fromPos.getX(), (int) toPos.getX());
        int minY = Math.min((int) fromPos.getY(), (int) toPos.getY());
        int maxY = Math.max((int) fromPos.getY(), (int) toPos.getY());
        int minZ = Math.min((int) fromPos.getZ(), (int) toPos.getZ());
        int maxZ = Math.max((int) fromPos.getZ(), (int) toPos.getZ());

        return new MinMaxCoords(minX, maxX, minY, maxY, minZ, maxZ);
    }
    //gotta have that clean code
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
