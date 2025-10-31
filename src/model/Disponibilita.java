package src.model;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import src.model.db.VolontariManager;
import src.model.db.DisponibilitaManager;
import src.model.db.ApplicationSettingsDAO;

public class Disponibilita {
    private final Map<Volontario, List<LocalDate>> disponibilitaVolontari = new ConcurrentHashMap<>();
    private final DisponibilitaManager disponibilitaManager = new DisponibilitaManager();
    private final ApplicationSettingsDAO applicationSettings = new ApplicationSettingsDAO();
    
    
    public Boolean getStato_raccolta() {
        LocalDate oggi = LocalDate.now();
        int giorno = oggi.getDayOfMonth();
        // true se siamo tra il 1° e il 15° giorno del mese (inclusi)
        return giorno >= 1 && giorno <= 15;
    }

    // Sincronizza la mappa delle disponibilità con il DB (per ogni volontario del manager)
    public void sincronizzaDisponibilitaVolontari(VolontariManager volontariManager) {
        disponibilitaVolontari.clear();
        volontariManager.getVolontariMap().values().forEach(volontario -> {
            List<LocalDate> dates = leggiDisponibilita(volontario, volontariManager);
            disponibilitaVolontari.put(volontario, dates != null ? new ArrayList<>(dates) : new ArrayList<>());
        });
        if (getStato_raccolta()) {
            gestisciVolontariSenzaDisponibilita(volontariManager);
        }
    }

    // Assicura che i volontari senza disponibilità abbiano una lista vuota e salva (se richiesto)
    public void gestisciVolontariSenzaDisponibilita(VolontariManager volontariManager) {
        LocalDate oggi = LocalDate.now();
        
        if (oggi.getDayOfMonth() > 15) {
            for (Volontario volontario : volontariManager.getVolontariMap().values()) {
                if (!disponibilitaVolontari.containsKey(volontario)) {
                    disponibilitaVolontari.put(volontario, new ArrayList<>());
                }
            }
            // Salva tutte le disponibilità correnti
            salvaStatoERaccolta(disponibilitaVolontari, volontariManager);
        }
    }

    // Legge le disponibilità per il singolo volontario dal DB (usa VolontariManager per risolvere l'id)
    public List<LocalDate> leggiDisponibilita(Volontario volontario, VolontariManager volontariManager) {
        if (volontario == null) return new ArrayList<>();
        int id = volontariManager.getIdByEmail(volontario.getEmail());
        return disponibilitaManager.getDisponibilitaByVolontarioId(id);
    }

    // Salva in memoria e persiste per il singolo volontario
    public void salvaDisponibilita(Volontario volontario, List<LocalDate> dateDisponibili, VolontariManager volontariManager) {
        if (volontario == null) return;
        List<LocalDate> copy = dateDisponibili == null ? new ArrayList<>() : new ArrayList<>(dateDisponibili);
        disponibilitaVolontari.put(volontario, copy);

        salvaStatoERaccolta(disponibilitaVolontari, volontariManager);
    }

    // Salva le disponibilità (mappa Volontario -> List<LocalDate>) nel DB e aggiorna lo stato raccolta
    public void salvaStatoERaccolta(Map<Volontario, List<LocalDate>> disponibilita, VolontariManager volontariManager) {
        if (disponibilita == null) return;
        applicationSettings.setStatoRaccolta(getStato_raccolta());
        disponibilitaManager.salvaDisponibilitaVolontari(disponibilita, volontariManager);
    }

    // Utilità: trova giorni (int) disponibili per un volontario in uno YearMonth
    public List<Integer> trovaGiorniDisponibili(Volontario volontario, YearMonth ym) {
        List<Integer> giorni = new ArrayList<>();
        List<LocalDate> dates = disponibilitaVolontari.getOrDefault(volontario, new ArrayList<>());
        for (LocalDate d : dates) {
            if (d.getYear() == ym.getYear() && d.getMonthValue() == ym.getMonthValue()) {
                giorni.add(d.getDayOfMonth());
            }
        }
        return giorni;
    }
}