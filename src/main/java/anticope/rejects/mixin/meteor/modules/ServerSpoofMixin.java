package anticope.rejects.mixin.meteor.modules;

import com.nikoverflow.exploitpreventer.ExploitPreventer;
import com.nikoverflow.exploitpreventer.module.Modules;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.misc.ServerSpoof;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerSpoof.class, remap = false)
public class ServerSpoofMixin extends Module {

    private SettingGroup sgExploitPreventer;
    private Setting<Boolean> translationKey;
    private Setting<Boolean> fingerprinting;
    private Setting<Boolean> localHTTPRequest;

    public ServerSpoofMixin(Category category, String name, String description) {
        super(category, name, description);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        sgExploitPreventer = settings.createGroup("Exploit Preventer");

        translationKey = sgExploitPreventer.add(new BoolSetting.Builder()
            .name("translation-key-prevention")
            .description("Prevents the server from detecting installed mods via translation keys.")
            .defaultValue(false)
            .onChanged(val -> applyModule(Modules.TRANSLATION_KEY, val))
            .build()
        );

        fingerprinting = sgExploitPreventer.add(new BoolSetting.Builder()
            .name("fingerprint-prevention")
            .description("Stops servers from using resource packs to uniquely identify your client.")
            .defaultValue(false)
            .onChanged(val -> applyModule(Modules.FINGERPRINTING, val))
            .build()
        );

        localHTTPRequest = sgExploitPreventer.add(new BoolSetting.Builder()
            .name("local-HTTP-request-prevention")
            .description("Blocks resource pack URLs that point to local IP addresses, prevents detection of locally running services.")
            .defaultValue(false)
            .onChanged(val -> applyModule(Modules.LOCAL_HTTP_REQUEST, val))
            .build()
        );
    }

    @Inject(method = "onActivate", at = @At("TAIL"))
    private void onActivate(CallbackInfo ci) {
        applyModule(Modules.TRANSLATION_KEY, translationKey.get());
        applyModule(Modules.FINGERPRINTING, fingerprinting.get());
        applyModule(Modules.LOCAL_HTTP_REQUEST, localHTTPRequest.get());
    }

    @Inject(method = "onDeactivate", at=@At("TAIL"), remap = false)
    private void onDeactivate(CallbackInfo ci) {
        applyModule(Modules.TRANSLATION_KEY, false);
        applyModule(Modules.FINGERPRINTING, false);
        applyModule(Modules.LOCAL_HTTP_REQUEST, false);
    }

    private void applyModule(Modules module, boolean enabled) {
        if (ExploitPreventer.getAPI() == null) return;
        ExploitPreventer.getAPI().getModuleManager().getModule(module).setEnabled(enabled);
    }
}
