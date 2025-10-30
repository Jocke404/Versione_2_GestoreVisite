package src.factory;

import src.view.Menu;
import src.view.MenuConfiguratore;
import src.controller.ConfiguratoriController;

public class MenuFactory {

    public Menu creaMenuConfiguratore(ConfiguratoriController configuratoriController) {
        return new MenuConfiguratore(configuratoriController);
    }
}