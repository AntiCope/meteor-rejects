package anticope.rejects.mixin;

import anticope.rejects.gui.servers.ServerManagerScreen;
import meteordevelopment.meteorclient.gui.GuiThemes;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen {
    protected MultiplayerScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        addDrawableChild(new ButtonWidget.Builder(Text.literal("Servers"), button -> client.setScreen(new ServerManagerScreen(GuiThemes.get(), (MultiplayerScreen) (Object) this)))
                .size(75, 20)
                .position(this.width - 75 - 3 - 75 - 2 - 75 - 2, 3)
                .build());
    }
}
