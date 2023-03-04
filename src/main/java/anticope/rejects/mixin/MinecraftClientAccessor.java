package anticope.rejects.mixin;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.report.AbuseReportContext;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.util.ProfileKeys;
import net.minecraft.client.util.Session;
import net.minecraft.network.encryption.SignatureVerifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
    @Mutable
    @Accessor("session")
    void setSession(Session session);

    @Mutable
    @Accessor("sessionService")
    void setSessionService(MinecraftSessionService sessionService);

    @Mutable
    @Accessor("authenticationService")
    void setAuthenticationService(YggdrasilAuthenticationService authenticationService);

    @Mutable
    @Accessor("servicesSignatureVerifier")
    void setServicesSignatureVerifier(SignatureVerifier servicesSignatureVerifier);

    @Mutable
    @Accessor("skinProvider")
    void setSkinProvider(PlayerSkinProvider skinProvider);

    @Mutable
    @Accessor("socialInteractionsManager")
    void setSocialInteractionsManager(SocialInteractionsManager socialInteractionsManager);

    @Mutable
    @Accessor("userApiService")
    void setUserApiService(UserApiService userApiService);

    @Mutable
    @Accessor("abuseReportContext")
    void setAbuseReportContext(AbuseReportContext abuseReportContext);

    @Mutable
    @Accessor("profileKeys")
    void setProfileKeys(ProfileKeys profileKeys);
}