package anticope.rejects.utils.accounts;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.screens.AccountsScreen;
import meteordevelopment.meteorclient.gui.screens.AddAccountScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.systems.accounts.Accounts;

public class AddCustomYggdrasilAccountScreen extends AddAccountScreen {
    public AddCustomYggdrasilAccountScreen(GuiTheme theme, AccountsScreen parent) {
        super(theme, "Add Yggdrasil Account", parent);
    }

    @Override
    public void initWidgets() {
        WTable t = add(theme.table()).widget();

        // Email
        t.add(theme.label("Username / Email: "));
        WTextBox username = t.add(theme.textBox("")).minWidth(400).expandX().widget();
        username.setFocused(true);
        t.row();

        // Password
        t.add(theme.label("Password: "));
        WTextBox password = t.add(theme.textBox("")).minWidth(400).expandX().widget();
        t.row();

        // Password
        t.add(theme.label("Server: "));
        WTextBox server = t.add(theme.textBox("")).minWidth(400).expandX().widget();
        t.row();

        // Add
        add = t.add(theme.button("Add")).expandX().widget();
        add.action = () -> {
            CustomYggdrasilAccount account = new CustomYggdrasilAccount(username.get(), password.get(), server.get());
            if (!username.get().isEmpty() && !password.get().isEmpty() && !Accounts.get().exists(account)) {
                AccountsScreen.addAccount(this, parent, account);
            }
        };

        enterAction = add.action;
    }
}