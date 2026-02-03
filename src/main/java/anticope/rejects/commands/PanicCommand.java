package anticope.rejects.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.commands.SharedSuggestionProvider;
import java.util.ArrayList;

public class PanicCommand extends Command {
    public PanicCommand() {
        super("panic", "Disables all modules.", "disable-all");
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        builder.executes(context -> {
            new ArrayList<>(Modules.get().getActive()).forEach(Module::toggle);

            return SINGLE_SUCCESS;
        });
    }
}
