package anticope.rejects.utils.accounts;

import anticope.rejects.MeteorRejectsAddon;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.accounts.AccountType;
import meteordevelopment.meteorclient.utils.misc.NbtException;
import net.minecraft.client.User;
import net.minecraft.nbt.CompoundTag;

public class CustomYggdrasilAccount extends Account<CustomYggdrasilAccount> {
    private String password, server;

    public CustomYggdrasilAccount(String name, String password, String server) {
        super(AccountType.Cracked, name);
        this.password = password;
        this.server = server;
    }

    @Override
    public boolean fetchInfo() {
        try {
            User session = CustomYggdrasilLogin.login(name, password, server);

            cache.username = session.getName();
            cache.uuid = session.getProfileId().toString();

            return true;
        } catch (AuthenticationException e) {
            return false;
        }
    }

    @Override
    public boolean login() {
        try {
            CustomYggdrasilLogin.LocalYggdrasilAuthenticationService service = new CustomYggdrasilLogin.LocalYggdrasilAuthenticationService(
                    java.net.Proxy.NO_PROXY, server);
            MinecraftSessionService sessService = new CustomYggdrasilLogin.LocalYggdrasilMinecraftSessionService(
                    service, service.server);
            applyLoginEnvironment(service);

            User session = CustomYggdrasilLogin.login(name, password, server);
            setSession(session);
            cache.username = session.getName();
            cache.loadHead();
            return true;
        } catch (AuthenticationException e) {
            if (e.getMessage().contains("Invalid username or password") || e.getMessage().contains("account migrated"))
                MeteorRejectsAddon.LOG.error("Wrong password.");
            else
                MeteorRejectsAddon.LOG.error("Failed to contact the authentication server.");
            return false;
        }
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();

        tag.putString("server", server);

        return tag;
    }

    @Override
    public CustomYggdrasilAccount fromTag(CompoundTag tag) {
        super.fromTag(tag);
        password = "";
        server = tag.getString("server").orElse("");

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CustomYggdrasilAccount))
            return false;
        return ((CustomYggdrasilAccount) o).name.equals(this.name);
    }
}
