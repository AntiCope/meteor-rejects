package cloudburst.rejects.gui.hud;

import cloudburst.rejects.utils.RejectsUtils;
import meteordevelopment.meteorclient.systems.modules.render.hud.HUD;
import meteordevelopment.meteorclient.systems.modules.render.hud.modules.DoubleTextHudElement;

public class CpsHud extends DoubleTextHudElement {
    public CpsHud(HUD hud) {
        super(hud, "cps", "Displays your CPS.", "CPS: ", false);
    }

    @Override
    protected String getRight() {
        return Integer.toString(RejectsUtils.CPS);
    }
}
