package anticope.rejects.gui.themes.rounded.widgets.pressable;

import anticope.rejects.gui.themes.rounded.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WFavorite;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WMeteorFavorite extends WFavorite implements MeteorWidget {
    public WMeteorFavorite(boolean checked) {
        super(checked);
    }

    @Override
    protected Color getColor() {
        return theme().favoriteColor.get();
    }
}
