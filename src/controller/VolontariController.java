package src.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import lib.InputDati;
import src.model.AggiuntaUtilita;
import src.model.Disponibilita;
import src.model.ValidatoreVisite;
import src.model.Volontario;
import src.view.ConsoleIO;
import src.view.ViewUtilita;
import src.model.db.VolontariManager;

public class VolontariController {
    private final VolontariManager volontariManager;
    private final AggiuntaUtilita addUtilita;
    private final ConsoleIO consoleIO;
    private final Disponibilita disponibilita = new Disponibilita();
    Volontario volontarioCorrente;
    private final ValidatoreVisite validatore;
    private final ViewUtilita viewUtilita;

    public VolontariController(VolontariManager volontariManager, AggiuntaUtilita addUtilita, 
                                ConsoleIO consoleIO, Volontario volontarioCorrente, ValidatoreVisite validatore, 
                                ViewUtilita viewUtilita) {
        this.volontariManager = volontariManager;
        this.addUtilita = addUtilita;
        this.consoleIO = consoleIO;
        this.volontarioCorrente = volontarioCorrente;
        this.validatore = validatore;
        this.viewUtilita = viewUtilita;

        // sincronizza le disponibilità in memoria con il DB usando le istanze Volontario del manager
        try {
            this.disponibilita.sincronizzaDisponibilitaVolontari(this.volontariManager);
        } catch (Exception e) {
            System.err.println("Errore sincronizzazione disponibilità: " + e.getMessage());
        }
    }

    public void raccogliDisponibilitaVolontario() {
        LocalDate oggi = LocalDate.now();
        if (oggi.getDayOfMonth() > 15) {
            consoleIO.mostraMessaggio("Non è possibile inserire disponibilità dopo il 15 del mese corrente.");
            return;
        }
        YearMonth ym = YearMonth.now().plusMonths(1);
        List<Integer> giorniDisponibili = validatore.trovaGiorniDisponibili(volontarioCorrente, ym);
        if (giorniDisponibili.isEmpty()) {
            consoleIO.mostraMessaggio("Non ci sono giorni disponibili per dichiarare la disponibilità.");
            return;
        }
        consoleIO.mostraCalendarioMese(ym, giorniDisponibili);
        List<Integer> giorniSelezionati = consoleIO.chiediGiorniDisponibili(ym, new ArrayList<>(giorniDisponibili));
        List<LocalDate> dateDisponibili = validatore.filtraDateDisponibili(giorniSelezionati, ym);

        // Se non sono state selezionate date, esci
        if (dateDisponibili == null || dateDisponibili.isEmpty()) {
            consoleIO.mostraMessaggio("Nessuna data selezionata. Operazione annullata.");
            return;
        }

        // Controlla se esistono già disponibilità per il volontario
        List<LocalDate> esistenti = disponibilita.getDisponibilitaVolontario(volontarioCorrente);
        boolean hasEsistenti = esistenti != null && !esistenti.isEmpty();

        if (hasEsistenti) {
            consoleIO.mostraMessaggio("Trovate disponibilità precedenti: le nuove date verranno aggiunte (no duplicati).");
            // merge = true -> aggiunge le nuove date a quelle esistenti evitando duplicati
            disponibilita.salvaDisponibilita(volontarioCorrente, dateDisponibili, volontariManager, true);
        } else {
            consoleIO.mostraMessaggio("Nessuna disponibilità precedente: verranno create le nuove disponibilità.");
            // merge = false -> sostituisce / crea le disponibilità
            disponibilita.salvaDisponibilita(volontarioCorrente, dateDisponibili, volontariManager, false);
        }
    }

    public List<Volontario> getVolontari() {
        return List.copyOf(volontariManager.getVolontariMap().values());
    }

    public void modificaPassword() {
        String nuovaPassword;
        String vecchiaPassword = volontarioCorrente.getPassword();

        do {
            nuovaPassword = consoleIO.chiediNuovaPassword();
            if (nuovaPassword == null) {
                consoleIO.mostraMessaggio("Password non valida. Riprova.");
                continue;
            }
            if (nuovaPassword.equals(vecchiaPassword)) {
                consoleIO.mostraMessaggio("La nuova password non può essere uguale a quella attuale. Inserisci una password diversa.");
            }
        } while (nuovaPassword == null || nuovaPassword.equals(vecchiaPassword));

        volontariManager.modificaPsw(volontarioCorrente.getEmail(), nuovaPassword);
    }

    public void modificaDisponibilitaVolontario() {
        LocalDate oggi = LocalDate.now();
        if (oggi.getDayOfMonth() > 15) {
            consoleIO.mostraMessaggio("Non è possibile modificare le disponibilità dopo il 15 del mese corrente.");
            return;
        }
        List<LocalDate> disponibilitaEsistenti = disponibilita.getDisponibilitaVolontario(volontarioCorrente);
        if (disponibilitaEsistenti == null || disponibilitaEsistenti.isEmpty()) {
            consoleIO.mostraMessaggio("Non ci sono disponibilità da modificare.");
            return;
        }

        YearMonth ym = YearMonth.now().plusMonths(1);
        List<Integer> giorniDisponibili = validatore.trovaGiorniDisponibili(volontarioCorrente, ym);
        consoleIO.mostraDisponibilitaEsistenti(disponibilitaEsistenti);
        List<LocalDate> disponibilitaEsistentiMod = consoleIO.chiediNuoveDisponibilita(disponibilitaEsistenti, giorniDisponibili, validatore);
        if (disponibilitaEsistentiMod == null) {
            consoleIO.mostraMessaggio("Nessuna modifica effettuata.");
            return;
        }

        if(InputDati.yesOrNo("Confermi le modifiche alle disponibilità?: ")) {
            disponibilita.salvaDisponibilita(volontarioCorrente, disponibilitaEsistentiMod, 
                                                volontariManager, false);
        }
    }

    public void visualizzaTipiVisiteVolontario() {
        viewUtilita.stampaTipiVisiteAssegnateVolontario(volontarioCorrente);
    }
}