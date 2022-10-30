package anticope.rejects.mixin.meteor;

import anticope.rejects.utils.RejectsConfig;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.render.FontUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Mixin(FontUtils.class)
public class FontUtilsMixin {
    @Inject(method = "getSearchPaths", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onGetSearchPaths(CallbackInfoReturnable<Set<String>> info) {
        if (!RejectsConfig.get().loadSystemFonts) {
            File dir = new File(MeteorClient.FOLDER, "fonts");
            info.setReturnValue(!dir.mkdirs() ? Collections.singleton(dir.getAbsolutePath()) : new HashSet<>());
        }
    }
}
