package pickleballsystem.ui;

import pickleballsystem.service.AccountManager;
import pickleballsystem.PickleballMain;
import pickleballsystem.service.SystemData;

/**
 * Parent of every screen and dialog class. It gives each child the shared
 * application navigator (app), the booking data (data), and the accounts (accounts),
 * so child classes do not repeat this setup.
 */
public abstract class BaseScreen {

    protected final PickleballMain app;
    protected final SystemData data;
    protected final AccountManager accounts;

    protected BaseScreen(PickleballMain app) {
        this.app = app;
        this.data = app.getData();
        this.accounts = app.getAccounts();
    }
}
