package cloudburst.rejects.modules;

import cloudburst.rejects.MeteorRejectsAddon;
import meteordevelopment.orbit.EventHandler;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.systems.modules.Categories;
import minegame159.meteorclient.systems.modules.Module;
import minegame159.meteorclient.systems.modules.Modules;
import minegame159.meteorclient.systems.modules.world.Timer;
import minegame159.meteorclient.utils.world.TickRate;

public class TPSSync extends Module {
    public TPSSync() {
        super(MeteorRejectsAddon.CATEGORY, "tps-sync", "Attemps to sync client tickrate with server's");
    }

    @Override
    public void onDeactivate() {
        Timer timer = Modules.get().get(Timer.class);
        timer.setOverride(1);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        Timer timer = Modules.get().get(Timer.class);
        timer.setOverride(Math.max(TickRate.INSTANCE.getTickRate(), 1) / 20);   // prevent client just dying alongside with server
    }
}