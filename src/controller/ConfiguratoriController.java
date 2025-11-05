package src.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lib.InputDati;
import src.model.AggiuntaUtilita;
import src.model.ModificaUtilita;
import src.model.TipiVisitaClass;
import src.model.Visita;
import src.model.Volontario;
import src.model.db.LuoghiManager;
import src.model.db.VisiteManagerDB;
import src.model.db.VolontariManager;
import src.view.ViewUtilita;
import src.model.AmbitoTerritoriale;
import src.model.Luogo;
import src.view.ConsoleIO;

public class ConfiguratoriController {
    private final AggiuntaUtilita addUtilita;
    private final ModificaUtilita modificaUtilita;
    private final ViewUtilita viewUtilita;
    private final AmbitoTerritoriale ambitoTerritoriale = new AmbitoTerritoriale();

    private final VolontariController volontariController;
    private final LuoghiController luoghiController;
    private final VisiteController visiteController;
    private ConsoleIO consoleIO = new ConsoleIO();

    private VolontariManager volontariManager;
    private LuoghiManager luoghiManager;
    private VisiteManagerDB visiteManagerDB;


    public ConfiguratoriController(
        AggiuntaUtilita addUtilita, 
        ModificaUtilita modificaUtilita, 
        ViewUtilita viewUtilita, 
        VolontariController volontariController,
        LuoghiController luoghiController,
        VisiteController visiteController,
        VisiteManagerDB visiteManagerDB,
        VolontariManager volontariManager,
        LuoghiManager luoghiManager
    ) {
        this.addUtilita = addUtilita;
        this.modificaUtilita = modificaUtilita;
        this.viewUtilita = viewUtilita;
        this.volontariController = volontariController;
        this.luoghiController = luoghiController;
        this.visiteController = visiteController;
        this.visiteManagerDB = visiteManagerDB;
        this.volontariManager = volontariManager;   
        this.luoghiManager = luoghiManager;
    }

    public void aggiungiVolontario() {
        consoleIO.mostraElencoConOggetti(volontariManager.getVolontariMap().values().stream().toList());
         
        Volontario nuovoVolontario = consoleIO.chiediDatiNuovoVolontario();
        if (nuovoVolontario != null && InputDati.yesOrNo("Vuoi confermare e aggiungere il volontario?")) {
            addUtilita.aggiungiVolontario(nuovoVolontario);
        } else {
            consoleIO.mostraMessaggio("Operazione annullata.");
        }
    }

    public void mostraVolontari() {
        viewUtilita.stampaVolontari(volontariController);
    }

    public void aggiungiLuogo() {
        consoleIO.mostraElencoConOggetti(luoghiManager.getLuoghiMap().values().stream().toList());
         
        Luogo nuovoLuogo = consoleIO.chiediDatiNuovoLuogo(ambitoTerritoriale);
        if (nuovoLuogo != null && InputDati.yesOrNo("Vuoi confermare e aggiungere il luogo?")) {
            addUtilita.aggiungiLuogo(nuovoLuogo);
        } else {
            consoleIO.mostraMessaggio("Operazione annullata.");
        }
    }

    public void mostraLuoghi() {
        viewUtilita.stampaLuoghi(luoghiController);
    }

    public void mostraVisite() {
        viewUtilita.stampaVisite(visiteController);
    }
    
    public void visualizzaVisitePerStato(){
        viewUtilita.stampaVisitePerStato();
    }

    public void modificaMaxPersone() {
        consoleIO.mostraMaxPersonePerVisita(visiteManagerDB);
        if (consoleIO.chiediAnnullaOperazione()) return;
        int numeroMax = consoleIO.chiediNumeroMaxPersone(visiteManagerDB);
        if (consoleIO.chiediConfermaNumeroMax(numeroMax)) {
            modificaUtilita.aggiornaMaxPersone(numeroMax);
            consoleIO.mostraRisultatoAggiornamentoMaxPersone(true, numeroMax);
        } else {
            consoleIO.mostraMessaggio("Operazione annullata.");
        }
    }

    public void modificaDataVisita() {
        List<Visita> visite = new ArrayList<>(visiteManagerDB.getVisiteMap().values());
        if (visite.isEmpty()) {
            consoleIO.mostraMessaggio("Non ci sono visite disponibili da modificare.");
            return;
        }
        int scelta = consoleIO.chiediSelezioneVisita(visite);
        Visita visitaSelezionata = visite.get(scelta);
        LocalDate dataOriginale = visitaSelezionata.getData();
        LocalDate nuovaData = consoleIO.chiediNuovaDataVisita(dataOriginale);
        if (consoleIO.chiediConfermaModificaData(dataOriginale, nuovaData)) {
            boolean successo = modificaUtilita.aggiornaDataVisita(visitaSelezionata.getId(), nuovaData);
            consoleIO.mostraRisultatoModificaData(successo);
        } else {
            consoleIO.mostraMessaggio("Modifica annullata. Nessun cambiamento effettuato.");
        }
    }

    public void aggiungiVisita() {
        consoleIO.mostraElencoConOggetti(visiteManagerDB.getVisiteMap().values().stream().toList());
        Visita nuovaVisita = null;
        if (consoleIO.chiediAnnullaOperazione())
            return;

        if (InputDati.yesOrNo("Vuoi pianificare la visita usando le disponibilità dei volontari? (s/n)")) {
            nuovaVisita = consoleIO.pianificazioneGuidata(visiteManagerDB, volontariManager, luoghiManager);
        } else {
            nuovaVisita = consoleIO.pianificazioneLibera(visiteManagerDB, volontariManager, luoghiManager);
        }


        if (nuovaVisita != null && InputDati.yesOrNo("Vuoi confermare e aggiungere la visita?")) {
            addUtilita.aggiungiVisita(nuovaVisita);
        } else {
            consoleIO.mostraMessaggio("Operazione annullata.");
        }
    }

    public void modificaStatoVisita() {
        List<Visita> visite = new ArrayList<>(visiteManagerDB.getVisiteMap().values());
        if (visite.isEmpty()) {
            consoleIO.mostraMessaggio("Non ci sono visite disponibili da modificare.");
            return;
        }
        consoleIO.mostraVisiteDisponibili(visite);

        if (consoleIO.chiediAnnullaOperazione()) {
            return;
        }
        int scelta = consoleIO.chiediSelezioneVisita(visite.size());
        Visita visitaSelezionata = visite.get(scelta - 1);

        String statoOriginale = visitaSelezionata.getStato();
        String[] stati = {"Proposta", "Completa", "Confermata", "Cancellata", "Effettuata"};
        String nuovoStato = consoleIO.chiediNuovoStato(stati);

        if (consoleIO.chiediConfermaModifica(statoOriginale, nuovoStato)) {
            boolean successo = modificaUtilita.aggiornaStatoVisita(visitaSelezionata.getId(), nuovoStato);
            if (successo) {
                consoleIO.mostraMessaggio("Stato della visita aggiornato con successo.");
            } else {
                consoleIO.mostraMessaggio("Errore nell'aggiornamento dello stato.");
            }
        } else {
            consoleIO.mostraMessaggio("Modifica annullata. Nessun cambiamento effettuato.");
        }
    }

    public void visualizzaArchivioStorico() {
        viewUtilita.stampaArchivioStorico(visiteController);
    }

    public void aggiungiDatePrecluse() {
        consoleIO.mostraElencoConOggetti(visiteManagerDB.getDatePrecluseMap().entrySet().stream().toList());
        if (consoleIO.chiediAnnullaOperazione()) return;
        LocalDate data = null;
        boolean continua = true;
        do {
            if (InputDati.yesOrNo("Vuoi aggiungere una data personale? (s/n)")) {
                data = consoleIO.chiediDataPreclusa();
                continua = false;
            } else {
                data = consoleIO.chiediDataPreclusaStandard();
                continua = false;
            }
        } while (continua);            
        if (data != null) {
                String motivo = consoleIO.chiediMotivoPreclusione(data);
                addUtilita.aggiungiDataPreclusa(data, motivo);
                consoleIO.mostraMessaggio("Data preclusa aggiunta con successo.");
            }
    }

    public void mostraDatePrecluse() {
        viewUtilita.stampaDatePrecluse(visiteController);
    }

    public void eliminaDatePrecluse() {
        ConcurrentHashMap<LocalDate, String> datePrecluse = visiteManagerDB.getDatePrecluseMap();
        if (datePrecluse.isEmpty()) {
            consoleIO.mostraMessaggio("Non ci sono date precluse da eliminare.");
            return;
        }
        int scelta = consoleIO.chiediDataPreclusaDaEliminare(datePrecluse);
        LocalDate dataDaEliminare = new ArrayList<>(datePrecluse.keySet()).get(scelta);
        if (consoleIO.chiediConfermaEliminazioneData(dataDaEliminare)) {
            boolean successo = modificaUtilita.eliminaDataPreclusa(dataDaEliminare);
            consoleIO.mostraRisultatoEliminazioneData(successo);
        } else {
            consoleIO.mostraRisultatoEliminazioneData(false);
        }
    }

    public void mostraAmbitoTerritoriale() {
        viewUtilita.stampaAmbitoTerritoriale(ambitoTerritoriale);
    }

    public void stampaTipiVisitaClassPerLuogo(){
       viewUtilita.stampaTipiVisitaClassPerLuogo(luoghiController);
    }

    public void aggiungiVolontariATipoVisita() {
        if (consoleIO.chiediAnnullaOperazione()) return;

        Map<String, Volontario> volontariMap = volontariManager.getVolontariMap();
        List<TipiVisitaClass> tipiVisitaList = visiteManagerDB.getTipiVisitaClassList();

        if (tipiVisitaList.isEmpty()) {
            consoleIO.mostraMessaggio("Nessun tipo di visita disponibile.");
            return;
        }
        TipiVisitaClass tipoVisitaScelto = consoleIO.chiediTipoVisita(tipiVisitaList);

        if (volontariMap.isEmpty()) {
            consoleIO.mostraMessaggio("Nessun volontario disponibile.");
            return;
        }
        List<Volontario> volontariDisponibili = new ArrayList<>(volontariMap.values());
        List<Volontario> volontariSelezionati = consoleIO.chiediVolontariMultipli(volontariDisponibili);

        addUtilita.assegnaTipoVisitaAVolontari(volontariSelezionati, tipoVisitaScelto);
        consoleIO.mostraMessaggio("Tipo di visita " + tipoVisitaScelto + " assegnato a " + volontariSelezionati.size() + " volontari.");
    }

    public void rimuoviVolontariDaTipoVisita() {
        if (consoleIO.chiediAnnullaOperazione()) return;

        Map<String, Volontario> volontariMap = volontariManager.getVolontariMap();
        List<TipiVisitaClass> tipiVisitaList = visiteManagerDB.getTipiVisitaClassList();

        if (tipiVisitaList.isEmpty()) {
            consoleIO.mostraMessaggio("Nessun tipo di visita disponibile.");
            return;
        }
        TipiVisitaClass tipoVisitaScelto = consoleIO.chiediTipoVisita(tipiVisitaList);

        List<Volontario> volontariConTipoVisita = volontariMap.values().stream()
            .filter(v -> v.getTipiDiVisite().contains(tipoVisitaScelto))
            .toList();

        if (volontariConTipoVisita.isEmpty()) {
            consoleIO.mostraMessaggio("Nessun volontario ha questo tipo di visita assegnato.");
            return;
        }
        List<Volontario> volontariSelezionati = consoleIO.chiediVolontariMultipli(volontariConTipoVisita);

        addUtilita.rimuoviTipoVisitaDaVolontari(volontariSelezionati, tipoVisitaScelto);
        consoleIO.mostraMessaggio("Tipo di visita " + tipoVisitaScelto + " rimosso da " + volontariSelezionati.size() + " volontari.");
    }

    public void visualizzaVolontariPerTipoVisita(){
        viewUtilita.visualizzaVolontariPerTipoVisita(volontariManager);
    }

    public void eliminaVisita() {
        if (consoleIO.chiediAnnullaOperazione()) return;
        List<Visita> visite = visiteController.getVisite();
        if (visite.isEmpty()) {
            consoleIO.mostraMessaggio("Nessuna visita disponibile per la modifica.");
            return;
        }
        int scelta = consoleIO.chiediSelezioneVisita(visite);
        Visita visitaDaEliminare = visite.get(scelta);
        if (consoleIO.chiediConfermaEliminazioneVisita(visitaDaEliminare)) {
            modificaUtilita.eliminaVisita(visitaDaEliminare, visiteController);
        } else {
            consoleIO.mostraMessaggio("Operazione annullata.");
        }
    }

    public void modificaNumeroPersoneIscrivibili() {
        viewUtilita.stampaMaxPersoneIscrivibiliNow();
        consoleIO.chiediAnnullaOperazione();
        int numeroMax = consoleIO.chiediNumeroMaxPersone(visiteManagerDB);
        if (consoleIO.chiediConfermaNumeroMax(numeroMax)) {
            modificaUtilita.aggiornaNumeroPersoneIscrivibili(numeroMax);
            consoleIO.mostraRisultatoAggiornamentoNumeroMax(true, numeroMax);
        } else {
            consoleIO.mostraMessaggio("Operazione annullata.");
        }
    }

    public void assegnaVisitaAVolontario() {
        consoleIO.mostraElencoConOggetti(visiteManagerDB.getVisiteMap().values().stream().toList());
        if (consoleIO.chiediAnnullaOperazione()) return;
        List<Visita> visite = new ArrayList<>(visiteManagerDB.getVisiteMap().values());
        if (visite.isEmpty()) {
            consoleIO.mostraMessaggio("Nessuna visita disponibile per l'assegnazione.");
        }
        int sceltaVisita = consoleIO.chiediSelezioneVisita(visite);
        Visita visitaSelezionata = visite.get(sceltaVisita);

        List<Volontario> volontari = new ArrayList<>(volontariManager.getVolontariMap().values());
        if (volontari.isEmpty()) {
            consoleIO.mostraMessaggio("Nessun volontario disponibile per l'assegnazione.");
        }
        int sceltaVolontario = consoleIO.chiediSelezioneVolontario(volontari);
        Volontario volontarioSelezionato = volontari.get(sceltaVolontario);
        if (!consoleIO.chiediAnnullaOperazione()) {
                addUtilita.assegnaVisitaAVolontario(visitaSelezionata, volontarioSelezionato);
                consoleIO.mostraRisultatoAggiornamentoVisitaVolontario(true);
        } else {
            consoleIO.mostraMessaggio("Operazione annullata.");
        }
    }

    public void rimuoviVisitaDaVolontario() {
        consoleIO.mostraElencoConOggetti(visiteManagerDB.getVisiteMap().values().stream().toList());
        if (consoleIO.chiediAnnullaOperazione()) return;
        List<Visita> visite = new ArrayList<>(visiteManagerDB.getVisiteMap().values());
        if (visite.isEmpty()) {
            consoleIO.mostraMessaggio("Nessuna visita disponibile per la rimozione.");
        }
        int sceltaVisita = consoleIO.chiediSelezioneVisita(visite);
        Visita visitaSelezionata = visite.get(sceltaVisita);

        List<Volontario> volontari = new ArrayList<>(volontariManager.getVolontariMap().values());
        if (volontari.isEmpty()) {
            consoleIO.mostraMessaggio("Nessun volontario disponibile per la rimozione.");
        }
        int sceltaVolontario = consoleIO.chiediSelezioneVolontario(volontari);
        Volontario volontarioSelezionato = volontari.get(sceltaVolontario);
        if (!consoleIO.chiediAnnullaOperazione()) {
             
            consoleIO.mostraRisultatoAggiornamentoVisitaVolontario(true);
        } else {
            consoleIO.mostraMessaggio("Operazione annullata.");
        }
    }



    public void aggiungiNuovoTipoVisita() {
        TipiVisitaClass nuovoTipo = consoleIO.chiediNuovoTipoVisita();
        if (nuovoTipo != null && InputDati.yesOrNo("Vuoi confermare e aggiungere il nuovo tipo di visita?")) {
            addUtilita.aggiungiNuovoTipoVisita(nuovoTipo);
            consoleIO.mostraMessaggio("Nuovo tipo di visita aggiunto con successo.");
        } else {
            consoleIO.mostraMessaggio("Operazione annullata.");
        }
    }
}
