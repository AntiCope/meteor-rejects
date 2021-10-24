package anticope.rejects.mixin.meteor;

import anticope.rejects.utils.RejectsConfig;
import meteordevelopment.meteorclient.utils.network.Http;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;


@Mixin(Http.class)
public class HttpMixin {
    
    @ModifyArg(method="get", at= @At(value = "INVOKE", target = "Lmeteordevelopment/meteorclient/utils/network/Http$Request;<init>(Lmeteordevelopment/meteorclient/utils/network/Http$Method;Ljava/lang/String;)V"), remap = false)
    private static String onGet(String url) {
        if (RejectsConfig.get().httpAllowed == RejectsConfig.HttpAllowed.Nothing) return "http://0.0.0.0";
        else if (RejectsConfig.get().httpAllowed == RejectsConfig.HttpAllowed.NotMeteorApi && url.startsWith("https://meteorclient.com/api")) return "http://0.0.0.0";
        else if (RejectsConfig.get().httpAllowed == RejectsConfig.HttpAllowed.NotMeteorPing && url.startsWith("https://meteorclient.com/api/online")) return "http://0.0.0.0";
        return url;
    }

    @ModifyArg(method="post", at= @At(value = "INVOKE", target = "Lmeteordevelopment/meteorclient/utils/network/Http$Request;<init>(Lmeteordevelopment/meteorclient/utils/network/Http$Method;Ljava/lang/String;)V"), remap = false)
    private static String onPost(String url) {
        if (RejectsConfig.get().httpAllowed == RejectsConfig.HttpAllowed.Nothing) return "http://0.0.0.0";
        else if (RejectsConfig.get().httpAllowed == RejectsConfig.HttpAllowed.NotMeteorApi && url.startsWith("https://meteorclient.com/api")) return "http://0.0.0.0";
        else if (RejectsConfig.get().httpAllowed == RejectsConfig.HttpAllowed.NotMeteorPing && url.startsWith("https://meteorclient.com/api/online")) return "http://0.0.0.0";
        return url;
    }
}
