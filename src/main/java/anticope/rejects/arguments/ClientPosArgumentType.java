package anticope.rejects.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.world.phys.Vec3;

public class ClientPosArgumentType implements ArgumentType<Vec3> {
    private static final Minecraft mc = Minecraft.getInstance();

    public static ClientPosArgumentType pos() {
        return new ClientPosArgumentType();
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (!(context.getSource() instanceof SharedSuggestionProvider)) {
            return Suggestions.empty();
        } else {
            String string = builder.getRemaining();
            Collection<SharedSuggestionProvider.TextCoordinates> collection2 = ((SharedSuggestionProvider)context.getSource()).getRelevantCoordinates();

            return SharedSuggestionProvider.suggestCoordinates(string, collection2, builder, Commands.createValidator(this::parse));
        }
    }

    public static Vec3 getPos(final CommandContext<?> context, final String name) {
        return context.getArgument(name, Vec3.class);
    }


    public Vec3 parse(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();
        double x,y,z;
        WorldCoordinate coordinateArgument = WorldCoordinate.parseInt(reader);
        WorldCoordinate coordinateArgument2;
        WorldCoordinate coordinateArgument3;
        if (reader.canRead() && reader.peek() == ' ') {
            reader.skip();
            coordinateArgument2 = WorldCoordinate.parseInt(reader);
            if (reader.canRead() && reader.peek() == ' ') {
                reader.skip();
                coordinateArgument3 = WorldCoordinate.parseInt(reader);
            } else {
                reader.setCursor(i);
                throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(reader);
            }
        } else {
            reader.setCursor(i);
            throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(reader);
        }

        x = coordinateArgument.get(mc.player.getX());
        y = coordinateArgument2.get(mc.player.getY());
        z = coordinateArgument3.get(mc.player.getZ());

        return new Vec3(x,y,z);
    }

}
