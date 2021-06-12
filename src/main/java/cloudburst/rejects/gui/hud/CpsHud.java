package cloudburst.rejects.gui.hud;

import cloudburst.rejects.utils.Utils;
import minegame159.meteorclient.systems.modules.render.hud.HUD;
import minegame159.meteorclient.systems.modules.render.hud.modules.DoubleTextHudElement;

public class CpsHud extends DoubleTextHudElement {
    public CpsHud(HUD hud) {
        super(hud, "cps", "Displays your CPS.", "CPS: ");
    }

    @Override
    protected String getRight() {
        return Integer.toString(Utils.CPS);
    }
}
