package src.model;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
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

    // Sincronizza la mappa delle disponibilità con il DB usando le istanze Volontario del manager
    public void sincronizzaDisponibilitaVolontari(VolontariManager volontariManager) {
        disponibilitaVolontari.clear();
        if (volontariManager == null) return;

        // per ogni Volontario conosciuto dal manager, carica le sue disponibilità dal DB
        for (Volontario v : volontariManager.getVolontariMap().values()) {
            if (v == null || v.getEmail() == null) continue;
            List<LocalDate> dates = Collections.emptyList();
            try {
                // usa il manager DB per leggere per id/email (adatta se il tuo DisponibilitaManager espone API diverse)
                dates = disponibilitaManager.getDisponibilitaByVolontarioId(volontariManager.getIdByEmail(v.getEmail()));
            } catch (Exception e) {
                // se fallisce, non interrompere la sincronizzazione
            }
            disponibilitaVolontari.put(v, new ArrayList<>(dates == null ? List.of() : dates));
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

    // Lookup sicuro per email (utile se l'istanza Volontario non corrisponde)
    public List<LocalDate> getDisponibilitaByEmail(String email) {
        if (email == null) return new ArrayList<>();
        String target = email.trim().toLowerCase();
        for (Volontario v : disponibilitaVolontari.keySet()) {
            String e = v == null ? null : v.getEmail();
            if (e != null && e.trim().toLowerCase().equals(target)) {
                return Collections.unmodifiableList(disponibilitaVolontari.getOrDefault(v, new ArrayList<>()));
            }
        }
        return new ArrayList<>();
    }

    public List<LocalDate> getDisponibilitaVolontario(Volontario volontarioCorrente) {
        return disponibilitaVolontari.getOrDefault(volontarioCorrente, new ArrayList<>());
    }

    /**
     * Unico punto di gestione delle disponibilità.
     * @param volontario volontario interessato
     * @param nuoveDisponibilita lista di date (null => lista vuota se replace; ignorata se merge && null)
     * @param volontariManager manager DB necessario per la persistenza
     * @param merge se true aggiunge le date a quelle esistenti (evita duplicati); se false sostituisce
     */
    public void salvaDisponibilita(Volontario volontario, List<LocalDate> nuoveDisponibilita,
            VolontariManager volontariManager, boolean merge) {
        if (volontario == null) return;

        List<LocalDate> corrente = new ArrayList<>(disponibilitaVolontari.getOrDefault(volontario, new ArrayList<>()));

        if (merge) {
            if (nuoveDisponibilita != null) {
                for (LocalDate d : nuoveDisponibilita) {
                    if (d == null) continue;
                    if (!corrente.contains(d)) corrente.add(d);
                }
            }
        } else {
            // replace: se nuoveDisponibilita è null -> usa lista vuota
            corrente = nuoveDisponibilita == null ? new ArrayList<>() : new ArrayList<>(nuoveDisponibilita);
            corrente.removeIf(d -> d == null);
        }

        corrente.sort(LocalDate::compareTo);
        disponibilitaVolontari.put(volontario, corrente);

        // persisti su DB
        salvaStatoERaccolta(disponibilitaVolontari, volontariManager);
    }

}