package src.controller;

import src.model.Luogo;
import src.model.db.LuoghiManager;
import src.view.ViewUtilita;

import java.util.List;

public class LuoghiController {
    private final LuoghiManager luoghiManager;
    private final ViewUtilita viewUtilita;

    public LuoghiController(LuoghiManager luoghiManager, ViewUtilita viewUtilita) {
        this.luoghiManager = luoghiManager;
        this.viewUtilita = viewUtilita;
    }

    public void mostraLuoghi() {
        viewUtilita.stampaLuoghi(this);
    }

    public List<Luogo> getLuoghi() {
        return List.copyOf(luoghiManager.getLuoghiMap().values());
    }

    public void aggiornaLuoghi(Luogo luogoDaModificare) {
        luoghiManager.aggiornaLuoghi(luogoDaModificare);
    }


}
