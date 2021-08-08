package cloudburst.rejects.mixin.meteor;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import meteordevelopment.meteorclient.systems.commands.Command;
import meteordevelopment.meteorclient.systems.commands.Commands;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;

import java.util.List;
import java.util.Map;


@Mixin(Commands.class)
public class CommandsMixin {
    @Shadow
    @Final
    private List<Command> commands;

    @Shadow
    @Final
    private Map<Class<? extends Command>, Command> commandInstances;
    

    @Shadow
    @Final
    private CommandDispatcher<CommandSource> DISPATCHER = new CommandDispatcher<>();

    @Inject(method = "add", at=@At("HEAD"), remap = false, cancellable = true)
    private void onAdd(Command cmd, CallbackInfo ci) {
        if (cmd instanceof meteordevelopment.meteorclient.systems.commands.commands.LocateCommand) {
            Command command = new cloudburst.rejects.commands.LocateCommand();
            commands.removeIf(command1 -> command1.getName().equals(command.getName()));
            commandInstances.values().removeIf(command1 -> command1.getName().equals(command.getName()));

            command.registerTo(DISPATCHER);
            commands.add(command);
            commandInstances.put(command.getClass(), command);
            ci.cancel();
        }
    }
}
