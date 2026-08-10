package anticope.rejects.mixininterface;

/**
 * ServerSpoof no longer overrides onActivate/onDeactivate as of Meteor 26.1, so the
 * Exploit Preventer hooks are driven from ModuleMixin through this interface instead.
 */
public interface IServerSpoof {
    void rejects$applyExploitPreventer();

    void rejects$disableExploitPreventer();
}
