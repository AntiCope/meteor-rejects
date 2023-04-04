package anticope.rejects.mixin.meteor;

import anticope.rejects.utils.RejectsConfig;
import meteordevelopment.meteorclient.utils.network.Http;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.http.HttpRequest;

@Mixin(Http.Request.class)
public class HttpRequestMixin {
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/net/http/HttpRequest$Builder;header(Ljava/lang/String;Ljava/lang/String;)Ljava/net/http/HttpRequest$Builder;"))
    private HttpRequest.Builder onAddUAHeader(HttpRequest.Builder builder, String userAgent, String value) {
        if (RejectsConfig.get().httpUserAgent.isBlank()) return builder;
        return builder.header("User-Agent", value);
    }
}
