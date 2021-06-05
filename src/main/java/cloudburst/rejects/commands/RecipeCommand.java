package cloudburst.rejects.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import meteordevelopment.orbit.EventHandler;
import minegame159.meteorclient.events.packets.PacketEvent;
import minegame159.meteorclient.systems.commands.Command;

import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.RecipeBookDataC2SPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.network.packet.s2c.play.UnlockRecipesS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

import java.util.*;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class RecipeCommand extends Command {

    private final static SimpleCommandExceptionType NO_RECIPES = new SimpleCommandExceptionType(new LiteralText("Couldn't obtain any recipes."));
    public Iterable<Recipe<?>> recipes = null;

    public RecipeCommand() {
        super("recipe", "Grants or removes recipes");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("grant").executes(ctx -> {
            if (recipes == null) throw NO_RECIPES.create();

            List<Identifier> recipeIdentifiers = new ArrayList<>();

            recipes.forEach(recipe -> {
                recipeIdentifiers.add(recipe.getId());
            });

            new UnlockRecipesS2CPacket(UnlockRecipesS2CPacket.Action.INIT, recipeIdentifiers, recipeIdentifiers, mc.player.getRecipeBook().getOptions()).apply(mc.getNetworkHandler());
            new UnlockRecipesS2CPacket(UnlockRecipesS2CPacket.Action.ADD, recipeIdentifiers, recipeIdentifiers, mc.player.getRecipeBook().getOptions()).apply(mc.getNetworkHandler());

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("revoke").executes(ctx -> {
            if (recipes == null) throw NO_RECIPES.create();

            List<Identifier> recipeIdentifiers = new ArrayList<>();

            recipes.forEach(recipe -> {
                recipeIdentifiers.add(recipe.getId());
            });

            new UnlockRecipesS2CPacket(UnlockRecipesS2CPacket.Action.REMOVE, recipeIdentifiers, recipeIdentifiers, mc.player.getRecipeBook().getOptions()).apply(mc.getNetworkHandler());

            return SINGLE_SUCCESS;
        }));
    }
}
