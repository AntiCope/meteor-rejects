package anticope.rejects.mixin;

import anticope.rejects.gui.servers.ServerManagerScreen;
import meteordevelopment.meteorclient.gui.GuiThemes;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JoinMultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen {
    protected MultiplayerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        addRenderableWidget(new Button.Builder(Component.literal("Servers"), button -> minecraft.setScreen(new ServerManagerScreen(GuiThemes.get(), (JoinMultiplayerScreen) (Object) this)))
                .size(75, 20)
                .pos(this.width - 75 - 3 - 75 - 2 - 75 - 2, 3)
                .build());
    }
}
