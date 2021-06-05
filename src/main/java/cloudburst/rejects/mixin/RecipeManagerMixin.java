package cloudburst.rejects.mixin;

import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;

import cloudburst.rejects.commands.RecipeCommand;
import minegame159.meteorclient.systems.commands.Commands;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
    @Inject(method = "setRecipes", at = @At(value = "HEAD"))
    public void setRecipes(Iterable<Recipe<?>> recipes, CallbackInfo ci) {
        if (Commands.get().get(RecipeCommand.class).recipes == null) {
            Commands.get().get(RecipeCommand.class).recipes = recipes;
        }
    }
}
