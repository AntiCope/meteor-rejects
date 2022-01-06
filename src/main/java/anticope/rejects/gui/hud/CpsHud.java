package anticope.rejects.gui.hud;

import anticope.rejects.utils.RejectsUtils;
import meteordevelopment.meteorclient.systems.hud.HUD;
import meteordevelopment.meteorclient.systems.hud.modules.DoubleTextHudElement;

public class CpsHud extends DoubleTextHudElement {
    public CpsHud(HUD hud) {
        super(hud, "cps", "Displays your CPS.", "CPS: ", false);
    }

    @Override
    protected String getRight() {
        return Integer.toString(RejectsUtils.CPS);
    }
}
