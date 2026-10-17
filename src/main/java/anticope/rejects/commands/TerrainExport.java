package anticope.rejects.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.lwjgl.sdl.SDLDialog;
import org.lwjgl.sdl.SDL_DialogFileFilter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.FileWriter;
import java.io.IOException;

public class TerrainExport extends Command {

    private final static SimpleCommandExceptionType IO_EXCEPTION = new SimpleCommandExceptionType(Component.literal("An IOException occurred"));
    private final SDL_DialogFileFilter.Buffer filters;

    public TerrainExport() {
        super("terrain-export", "Export an area to the c++ terrain finder format (very popbob command).");

        // 26.3: GLFW/tinyfd was replaced by SDL. The dialog is asynchronous, so the filter
        // strings have to stay allocated after this constructor returns.
        MemoryStack stack = MemoryStack.stackPush();
        filters = SDL_DialogFileFilter.malloc(1, stack);
        filters.name(stack.UTF8("Text Files")).pattern(stack.UTF8("txt"));
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(argument("distance", IntegerArgumentType.integer(1)).executes(context -> {
            int distance = IntegerArgumentType.getInteger(context, "distance");

            StringBuilder stringBuilder = new StringBuilder();
            for (int x = -distance; x <= distance; x++) {
                for (int z = -distance; z <= distance; z++) {
                    for (int y = distance; y >= -distance; y--) {
                        BlockPos pos = mc.player.blockPosition().offset(x, y, z);
                        if (mc.level.getBlockState(pos).isCollisionShapeFullBlock(mc.level, pos)) {
                            stringBuilder.append(String.format("%d, %d, %d\n", x + distance, y + distance, z + distance));
                        }
                    }
                }
            }

            String data = stringBuilder.toString().trim();

            SDLDialog.SDL_ShowSaveFileDialog((_, file, _) -> {
                if (file == 0) return;

                long filePointer = MemoryUtil.memGetAddress(file);
                if (filePointer == 0) return;

                String path = MemoryUtil.memUTF8(filePointer);
                if (path.isBlank()) return;
                if (!path.endsWith(".txt")) path += ".txt";

                try {
                    FileWriter out = new FileWriter(path);
                    out.write(data);
                    out.close();
                } catch (IOException e) {
                    error("An IOException occurred.");
                }
            }, mc.getWindow().handle(), 0, filters, "");

            return SINGLE_SUCCESS;
        }));
    }
}
