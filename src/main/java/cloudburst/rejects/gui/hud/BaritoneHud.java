package cloudburst.rejects.gui.hud;

import baritone.api.BaritoneAPI;
import baritone.api.process.IBaritoneProcess;
import meteordevelopment.meteorclient.systems.modules.render.hud.HUD;
import meteordevelopment.meteorclient.systems.modules.render.hud.modules.DoubleTextHudElement;

public class BaritoneHud extends DoubleTextHudElement {
	public BaritoneHud(HUD hud) {
		super(hud, "Baritone", "Displays what baritone is doing.", "Baritone: ");
	}
	
	@Override
	protected String getRight() {
		IBaritoneProcess process = BaritoneAPI.getProvider().getPrimaryBaritone().getPathingControlManager().mostRecentInControl().orElse(null);
		
		if (process == null) return "-";
		
		return process.displayName();
		
		
	}
}