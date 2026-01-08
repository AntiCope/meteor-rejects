package anticope.rejects.settings;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.world.level.GameType;
import java.util.List;

public class GameModeListSettingScreen extends WindowScreen {
    private final GameModeListSetting setting;
    private final WTable table;

    public GameModeListSettingScreen(GuiTheme theme, GameModeListSetting setting) {
        super(theme, "Select Gamemodes");
        this.setting = setting;
        table = super.add(theme.table()).expandX().widget();
    }

    @Override
    public void initWidgets() {
        List<GameType> gms = setting.get();
        for (GameType gameMode : GameType.values()) {
            table.add(theme.label(Utils.nameToTitle(gameMode.getName()))).expandCellX();

            boolean contains = setting.get().contains(gameMode);
            WCheckbox checkbox = table.add(theme.checkbox(contains)).widget();
            checkbox.action = () -> {
                if (contains) {
                    gms.remove(gameMode);
                } else {
                    gms.add(gameMode);
                }
            };

            table.row();
        }
    }
}
