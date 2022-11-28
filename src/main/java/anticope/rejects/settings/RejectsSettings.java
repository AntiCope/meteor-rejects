package anticope.rejects.settings;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;

import java.util.Map;

public class RejectsSettings {
    private final Map<Class<?>, SettingsWidgetFactory.Factory> factories;

    private final GuiTheme theme;

    public RejectsSettings(Map<Class<?>, SettingsWidgetFactory.Factory> factories, GuiTheme theme) {
        this.factories = factories;
        this.theme = theme;
    }

    public void addSettings() {
        factories.put(StringMapSetting.class, (table, setting) -> stringMapW(table, (StringMapSetting) setting));
    }

    private void stringMapW(WTable table, StringMapSetting setting) {
        WTable wtable = table.add(theme.table()).expandX().widget();
        StringMapSetting.fillTable(theme, wtable, setting);
    }
}
