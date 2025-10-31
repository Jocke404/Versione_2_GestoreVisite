package src.factory;

import src.view.Menu;
import src.view.MenuConfiguratore;
import src.view.MenuVolontario;
import src.controller.ConfiguratoriController;
import src.controller.VolontariController;

public class MenuFactory {

    public Menu creaMenuConfiguratore(ConfiguratoriController configuratoriController) {
        return new MenuConfiguratore(configuratoriController);
    }

    public Menu creaMenuVolontario(VolontariController volontariController) {
        return new MenuVolontario(volontariController);
    }
}