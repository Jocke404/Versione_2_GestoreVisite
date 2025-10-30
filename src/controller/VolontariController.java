package src.controller;

import java.util.List;

import src.model.AggiuntaUtilita;
import src.model.Disponibilita;
import src.model.ValidatoreVisite;
import src.model.Volontario;
import src.view.ConsoleIO;
import src.view.ViewUtilita;
import src.model.db.VolontariManager;

public class VolontariController {
    private final VolontariManager volontariManager;
    private final ConsoleIO consoleIO;
    private final Disponibilita disponibilitaManager = new Disponibilita();
    Volontario volontarioCorrente;

    public VolontariController(VolontariManager volontariManager, AggiuntaUtilita addUtilita, 
                                ConsoleIO consoleIO, Volontario volontarioCorrente, ValidatoreVisite validatore, 
                                ViewUtilita viewUtilita) {
        this.volontariManager = volontariManager;
        this.consoleIO = consoleIO;
        this.volontarioCorrente = volontarioCorrente;
    }

    public List<Volontario> getVolontari() {
        return List.copyOf(volontariManager.getVolontariMap().values());
    }
    
    //VER.3
    // public void eliminaVolontario(Volontario volontarioDaEliminare) {
    //     volontariManager.eliminaVolontario(volontarioDaEliminare);
    // }

}