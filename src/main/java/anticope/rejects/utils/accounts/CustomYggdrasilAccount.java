package anticope.rejects.utils.accounts;

import anticope.rejects.MeteorRejectsAddon;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import meteordevelopment.meteorclient.mixin.MinecraftClientAccessor;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.accounts.AccountType;
import meteordevelopment.meteorclient.utils.misc.NbtException;
import net.minecraft.client.session.Session;
import net.minecraft.nbt.NbtCompound;

import static meteordevelopment.meteorclient.MeteorClient.mc;

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
            Session session = CustomYggdrasilLogin.login(name, password, server);

            cache.username = session.getUsername();
            cache.uuid = session.getUuidOrNull().toString();

            return true;
        } catch (AuthenticationException e) {
            return false;
        }
    }

    @Override
    public boolean login() {
        try {
            CustomYggdrasilLogin.LocalYggdrasilAuthenticationService service = new CustomYggdrasilLogin.LocalYggdrasilAuthenticationService(((MinecraftClientAccessor) mc).getProxy(), server);
            MinecraftSessionService sessService = new CustomYggdrasilLogin.LocalYggdrasilMinecraftSessionService(service, service.server);
            applyLoginEnvironment(service, sessService);

            Session session = CustomYggdrasilLogin.login(name, password, server);
            setSession(session);
            cache.username = session.getUsername();
            cache.loadHead();
            return true;
        } catch (AuthenticationException e) {
            if (e.getMessage().contains("Invalid username or password") || e.getMessage().contains("account migrated"))
                MeteorRejectsAddon.LOG.error("Wrong password.");
            else MeteorRejectsAddon.LOG.error("Failed to contact the authentication server.");
            return false;
        }
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = super.toTag();

        tag.putString("password", password);
        tag.putString("server", server);

        return tag;
    }

    @Override
    public CustomYggdrasilAccount fromTag(NbtCompound tag) {
        super.fromTag(tag);
        if (!tag.contains("password")) throw new NbtException();

        password = String.valueOf(tag.getString("password"));
        server = String.valueOf(tag.getString("server"));

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CustomYggdrasilAccount)) return false;
        return ((CustomYggdrasilAccount) o).name.equals(this.name);
    }
}
