package cloudburst.rejects.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import minegame159.meteorclient.systems.commands.Command;

import net.minecraft.command.CommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class TerrainExport extends Command {

    private final PointerBuffer filters;

    private final static SimpleCommandExceptionType IO_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("An IOException occurred"));

    public TerrainExport() {
        super("terrain-export", "Export an area to the c++ terrain finder format (very popbob command).");

        filters = BufferUtils.createPointerBuffer(1);

        ByteBuffer txtFilter = MemoryUtil.memASCII("*.txt");

        filters.put(txtFilter);
        filters.rewind();
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("distance", IntegerArgumentType.integer(1)).executes(context -> {
            int distance = IntegerArgumentType.getInteger(context, "distance");

            StringBuilder stringBuilder = new StringBuilder();
            for (int x = -distance; x <= distance; x++) {
                for (int z = -distance; z <= distance; z++) {
                    for (int y = distance; y >= -distance; y--) {
                        BlockPos pos = mc.player.getBlockPos().add(x, y, z);
                        if (mc.world.getBlockState(pos).isFullCube(mc.world, pos)) {
                            stringBuilder.append(String.format("%d, %d, %d\n", x + distance, y + distance, z + distance));
                        }
                    }
                }
            }

            String path = TinyFileDialogs.tinyfd_saveFileDialog("Save data", null, filters, null);
            if (path == null) throw IO_EXCEPTION.create();
            if (!path.endsWith(".txt"))
                path += ".txt";
            try {
                FileWriter file = new FileWriter(path);
                file.write(stringBuilder.toString().trim());
                file.close();
            } catch (IOException e) {
                throw IO_EXCEPTION.create();
            }

            return SINGLE_SUCCESS;
        }));
    }
}
