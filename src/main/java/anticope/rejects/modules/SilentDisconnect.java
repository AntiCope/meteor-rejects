package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.annotation.AutoRegister;
import meteordevelopment.meteorclient.systems.modules.Module;

@AutoRegister
public class SilentDisconnect extends Module {
    public SilentDisconnect() {
        super(MeteorRejectsAddon.CATEGORY, "silent-disconnect", "Won't show a disconnect screen when you disconnect.");
    }
}
