package src.model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import src.model.db.ApplicationSettingsDAO;
import src.model.db.VisiteManagerDB;
import src.controller.LuoghiController;
import src.controller.VisiteController;
import src.controller.VolontariController;

public class ModificaUtilita {

    private final VisiteManagerDB visiteManagerDB;
    private static final String NUMERO_PERSONE_FILE = "src/utility/max_persone_iscrivibili.config";


    public ModificaUtilita(VisiteManagerDB visiteManagerDB) {
        this.visiteManagerDB = visiteManagerDB;
    }

    public boolean aggiornaStatoVisita(int visitaId, String nuovoStato) {
        Visita visita = visiteManagerDB.getVisiteMap().get(visitaId);
        if (visita == null) return false;
        visita.setStato(nuovoStato);
        visiteManagerDB.aggiornaVisita(visitaId, visita);
        return true;
    }

    public boolean aggiornaDataVisita(int visitaId, LocalDate nuovaData) {
        Visita visita = visiteManagerDB.getVisiteMap().get(visitaId);
        if (visita == null) return false;
        visita.setData(nuovaData);
        visiteManagerDB.aggiornaVisita(visitaId, visita);
        return true;
    }


    public boolean aggiornaMaxPersone(int numeroMax) {
        if (numeroMax < 2) return false;
        visiteManagerDB.aggiornaMaxPersone(numeroMax);
        return true;
    }


    public boolean eliminaDataPreclusa(LocalDate data) {
        visiteManagerDB.eliminaData(data);
        return true;
    }

    public void aggiornaLuogo(Luogo luogo, String nuovoNome, String nuovaDescrizione, 
                            String nuovaCollocazione, List<TipiVisitaClass> nuoviTipi, LuoghiController luoghiController) {
        if (!nuovoNome.isEmpty()) luogo.setName(nuovoNome);
        if (!nuovaDescrizione.isEmpty()) luogo.setDescrizione(nuovaDescrizione);
        if (!nuovaCollocazione.isEmpty()) luogo.setCollocazione(nuovaCollocazione);
        luogo.setTipiVisitaClass(nuoviTipi);
        luoghiController.aggiornaLuoghi(luogo);
    }

    public void eliminaVisita(Visita visita, VisiteController visiteController) {
        visiteController.eliminaVisita(visita);
    }

    public boolean aggiornaNumeroPersoneIscrivibili(int numeroMax) {
        if (numeroMax < 1) return false;

        boolean ok = false;
        try {
            ok = ApplicationSettingsDAO.setMaxPeoplePerVisit(numeroMax);
        } catch (Throwable t) {
            ok = false;
        }

        if (!ok) {
            // fallback legacy: scrivi ancora il file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(NUMERO_PERSONE_FILE))) {
                writer.write(String.valueOf(numeroMax));
                ok = true;
            } catch (IOException e) {
                ok = false;
            }
        }
        return ok;
    }

}