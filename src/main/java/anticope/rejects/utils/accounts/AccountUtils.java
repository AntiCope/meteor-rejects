package anticope.rejects.utils.accounts;

import anticope.rejects.mixin.MinecraftClientAccessor;
import anticope.rejects.mixin.PlayerSkinProviderAccessor;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.report.AbuseReportContext;
import net.minecraft.client.report.ReporterEnvironment;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.util.ProfileKeys;
import net.minecraft.client.util.Session;
import net.minecraft.network.encryption.SignatureVerifier;

import java.io.File;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class AccountUtils {
    public static MinecraftSessionService applyLoginEnvironment(YggdrasilAuthenticationService authService, MinecraftSessionService sessService, Session session) {
        File skinDir = ((PlayerSkinProviderAccessor) mc.getSkinProvider()).getSkinCacheDir();
        ((MinecraftClientAccessor) mc).setSession(session);
        ((MinecraftClientAccessor) mc).setAuthenticationService(authService);
        ((MinecraftClientAccessor) mc).setSessionService(sessService);
        ((MinecraftClientAccessor) mc).setServicesSignatureVerifier(SignatureVerifier.create(authService.getServicesKey()));
        ((MinecraftClientAccessor) mc).setSkinProvider(new PlayerSkinProvider(mc.getTextureManager(), skinDir, sessService));
        UserApiService apiService = createUserApiService(authService, session);
        ((MinecraftClientAccessor) mc).setUserApiService(apiService);
        ((MinecraftClientAccessor) mc).setSocialInteractionsManager(new SocialInteractionsManager(mc, apiService));
        ((MinecraftClientAccessor) mc).setProfileKeys(ProfileKeys.create(apiService, session, mc.runDirectory.toPath()));
        ((MinecraftClientAccessor) mc).setAbuseReportContext(AbuseReportContext.create(ReporterEnvironment.ofIntegratedServer(), apiService));
        return sessService;
    }

    public static UserApiService createUserApiService(YggdrasilAuthenticationService authService, Session session) {
        try {
            return authService.createUserApiService(session.getAccessToken());
        } catch (AuthenticationException e) {
            return UserApiService.OFFLINE;
        }
    }
}

