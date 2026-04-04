package anticope.rejects.mixin.meteor.modules;

import anticope.rejects.utils.ExploitPreventerCompat;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.misc.ServerSpoof;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerSpoof.class, remap = false)
public class ServerSpoofMixin extends Module {
    private static final boolean EP_LOADED = FabricLoader.getInstance().isModLoaded("exploitpreventer");

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
            .defaultValue(true)
            .onChanged(val -> {
                if (!EP_LOADED) { if (val) warning("ExploitPreventer is not installed."); return; }
                ExploitPreventerCompat.applyTranslationKey(val);
            })
            .build()
        );

        fingerprinting = sgExploitPreventer.add(new BoolSetting.Builder()
            .name("fingerprint-prevention")
            .description("Stops servers from using resource packs to uniquely identify your client.")
            .defaultValue(true)
            .onChanged(val -> {
                if (!EP_LOADED) { if (val) warning("ExploitPreventer is not installed."); return; }
                ExploitPreventerCompat.applyFingerprinting(val);
            })
            .build()
        );

        localHTTPRequest = sgExploitPreventer.add(new BoolSetting.Builder()
            .name("local-HTTP-request-prevention")
            .description("Blocks resource pack URLs that point to local IP addresses, prevents detection of locally running services.")
            .defaultValue(true)
            .onChanged(val -> {
                if (!EP_LOADED) { if (val) warning("ExploitPreventer is not installed."); return; }
                ExploitPreventerCompat.applyLocalHTTPRequest(val);
            })
            .build()
        );
    }

    @Inject(method = "onActivate", at = @At("TAIL"))
    private void onActivate(CallbackInfo ci) {
        if (!EP_LOADED) return;
        ExploitPreventerCompat.applyAll(translationKey.get(), fingerprinting.get(), localHTTPRequest.get());
    }

    @Inject(method = "onDeactivate", at = @At("TAIL"), remap = false)
    private void onDeactivate(CallbackInfo ci) {
        if (!EP_LOADED) return;
        ExploitPreventerCompat.disableAll();
    }
}
