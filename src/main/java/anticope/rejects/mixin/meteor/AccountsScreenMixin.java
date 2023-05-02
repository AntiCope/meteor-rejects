package anticope.rejects.mixin.meteor;

import anticope.rejects.utils.accounts.AddCustomYggdrasilAccountScreen;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.screens.accounts.AccountsScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(value = AccountsScreen.class, remap = false)
public abstract class AccountsScreenMixin extends WindowScreen {
    public AccountsScreenMixin(GuiTheme theme, WWidget icon, String title) {
        super(theme, icon, title);
    }

    @Shadow
    protected abstract void addButton(WContainer c, String text, Runnable action);

    @Inject(method = "initWidgets", at = @At(value = "INVOKE", target = "Lmeteordevelopment/meteorclient/gui/screens/accounts/AccountsScreen;addButton(Lmeteordevelopment/meteorclient/gui/widgets/containers/WContainer;Ljava/lang/String;Ljava/lang/Runnable;)V", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void afterAddCrackedButton(CallbackInfo info, WHorizontalList l) {
        addButton(l, "Yggdrasil", () -> mc.setScreen(new AddCustomYggdrasilAccountScreen(theme, (AccountsScreen) (Object) this)));
    }
}
