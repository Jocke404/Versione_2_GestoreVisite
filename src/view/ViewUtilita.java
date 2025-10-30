package src.view;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import lib.InputDati;
import lib.ServizioFile;
import src.controller.VolontariController;
import src.model.AmbitoTerritoriale;
import src.model.Luogo;
import src.model.TipiVisitaClass;
import src.model.Visita;
import src.model.Volontario;
import src.model.db.ApplicationSettingsDAO;
import src.model.db.VisiteManagerDB;
import src.model.db.VolontariManager;
import src.controller.VisiteController;
import src.controller.LuoghiController;
import src.controller.ThreadPoolController;

public class ViewUtilita {

    private static final File NUMERO_PERSONE_FILE = new File("src/utility/max_persone_iscrivibili.config");
    private ConcurrentHashMap<Integer, Visita> visiteMap = new VisiteManagerDB(ThreadPoolController.getInstance()).getVisiteMap();
    private final ConsoleIO consoleIO = new ConsoleIO();
    private List<TipiVisitaClass> tipiVisitaList = new VisiteManagerDB(ThreadPoolController.getInstance()).getTipiVisitaClassList();
    
    private static ViewUtilita instance;

    private ViewUtilita() {}

    public static ViewUtilita getInstance() {
        if (instance == null) {
            instance = new ViewUtilita();
        }
        return instance;
    }

    // Metodo per stampare i luoghi
    public void stampaLuoghi(LuoghiController luoghiController) {
        List<Luogo> luoghi = luoghiController.getLuoghi();
        if (luoghi.isEmpty()) {
            System.out.println("Nessun luogo disponibile.");
            return;
        }

        System.out.println("Luoghi:");
        consoleIO.mostraElencoConOggetti(luoghi);
    }

    // Metodo per stampare i volontari
    public void stampaVolontari(VolontariController volontariController) {
        List<Volontario> volontari = volontariController.getVolontari();
        consoleIO.mostraElencoConOggetti(volontari);
        // if (volontari.isEmpty()) {
        //     System.out.println("Nessun volontario disponibile.");
        //     return;
        // }

        // System.out.println("Volontari:");
        // for (Volontario volontario : volontari) {
        //     System.out.println("Nome: " + volontario.getNome() + " " + volontario.getCognome());
        //     System.out.println("Email: " + volontario.getEmail());
        //     System.out.println("Tipi di Visite: " + volontario.getTipiDiVisite());
        //     System.out.println("-------------------------");
        // }
    }

    // Metodo per stampare le visite
    public void stampaVisite(VisiteController visiteController) {
        List<Visita> visite = visiteController.getVisite();
        if (visite.isEmpty()) {
            System.out.println("Nessuna visita disponibile.");
            return;
        }

        System.out.println("Visite:");
        consoleIO.mostraElencoConOggetti(visite);
    }

    // Metodo per visualizzare le visite per stato
    public void stampaVisitePerStato() { 
        if (visiteMap.isEmpty()) {
            System.out.println("Non ci sono visite disponibili.");
            return;
        }

        String[] stati = {"Proposta", "Completa", "Confermata", "Cancellata"};
        System.out.println("Stati disponibili:");
        for (int i = 0; i < stati.length; i++) {
            System.out.printf("%d. %s%n", i + 1, stati[i]);
        }

        int sceltaStato = InputDati.leggiIntero("Seleziona lo stato da visualizzare: ", 1, stati.length) - 1;
        String statoScelto = stati[sceltaStato];
        List<Visita> visiteInStato = new ArrayList<>();

        System.out.printf("Visite in stato '%s':%n", statoScelto);
        for (Visita visita : visiteMap.values()) {
            if (visita.getStato().equalsIgnoreCase(statoScelto)) {
                visiteInStato.add(visita);
            }
        }
        consoleIO.mostraElencoConOggetti(visiteInStato);
    }

    // Metodo per visualizzare l'archivio storico delle visite
    public void stampaArchivioStorico(VisiteController visiteController) {
        ConcurrentHashMap<Integer, Visita> visiteMap = visiteController.getVisiteMap();

        if (visiteMap.isEmpty()) {
            System.out.println("Non ci sono visite disponibili nell'archivio storico.");
            return;
        }

        System.out.println("Archivio storico delle visite effettuate:");
        for (Visita visita : visiteMap.values()) {
            if ("Effettuata".equalsIgnoreCase(visita.getStato())) {
                System.out.printf("Luogo: %s, Tipo Visita: %s, Volontario: %s, Data: %s%n",
                        visita.getLuogo(), visita.getTipiVisitaClass(), visita.getVolontario(),
                        visita.getData() != null ? visita.getData() : "Nessuna data");
            }
        }
    }


    // Metodo per visualizzare le visite assegnate a un volontario
    public void stampaVisiteVolontario(Volontario volontario) {
        System.out.println("Visite assegnate a " + volontario.getNome() + " " + volontario.getCognome() + ":");
        if (visiteMap.isEmpty()) {
            System.out.println("Nessuna visita disponibile.");
            return;
        }

        boolean visiteTrovate = false;
    
        for (Map.Entry<Integer, Visita> entry : visiteMap.entrySet()) {
            Visita visita = entry.getValue();
            if (visita.getVolontario().equals(volontario.getNome() + " " + volontario.getCognome())) {
                System.out.println("ID: " + entry.getKey());
                System.out.println("Luogo: " + visita.getLuogo());
                System.out.println("Tipi Visita: " + visita.getTipiVisitaClassString());
                System.out.println("Data: " + (visita.getData() != null ? visita.getData() : "Nessuna data"));
                System.out.println("Stato: " + visita.getStato());
                System.out.println("-------------------------");
                visiteTrovate = true;
            }
        }
    
        if (!visiteTrovate) {
            System.out.println("Nessuna visita assegnata al volontario.");
        }
    }

    public void stampaDatePrecluse(VisiteController visiteController) {
        List<Map.Entry<LocalDate, String>> datePrecluseMap = visiteController.getDatePrecluse();
        if (datePrecluseMap.isEmpty()) {
            System.out.println("Nessuna data preclusa disponibile.");
            return;
        }

        System.out.println("Date Precluse:");
        for (Map.Entry<LocalDate, String> entry : datePrecluseMap) {
            System.out.printf("Data: %s, Motivo: %s%n", entry.getKey(), entry.getValue());
        }
    }

  // Metodo per visualizzare l'ambito territoriale
    public void stampaAmbitoTerritoriale(AmbitoTerritoriale ambitoTerritoriale) {
        List<String> ambito = ambitoTerritoriale.getAmbitoTerritoriale();
        if (ambito.isEmpty()) {
            System.out.println("Ambito territoriale non configurato.");
        } else {
            System.out.println("Ambito territoriale:");
            for (String comune : ambito) {
                System.out.println("- " + comune);
            }
        }
    }

    //Metodo per visualizzare i tipi di visita per luogo
    public void stampaTipiVisitaClassPerLuogo(LuoghiController luoghiController) {

        System.out.println ("Tipi di visita per luogo:");

        List<Luogo> luoghi = luoghiController.getLuoghi();

        if (luoghi.isEmpty()) {
            System.out.println("Nessun luogo disponibile.");
            return;
        }

        for (Luogo luogo : luoghi) {
            System.out.println("Luogo: " + luogo.getNome());
            List<TipiVisitaClass> tipiVisita = luogo.getTipiVisitaClass();
            System.out.println("Tipi di visita:");
            if (tipiVisita == null || tipiVisita.isEmpty()) {
                System.out.println("  Nessun tipo di visita disponibile.");
            } else {
                for (TipiVisitaClass tipo : tipiVisita) {
                    System.out.println("  - " + tipo.getNome());
                }
            }
        }
    }

    public void visualizzaVisiteDisponibili() {
        System.out.println("Visite disponibili (stato: Proposta/Confermata, posti ancora disponibili):");
        boolean visiteTrovate = false;

        for (Visita visita : visiteMap.values()) {
            String stato = visita.getStato();
            int postiDisponibili = visita.getPostiDisponibili();

            if ((stato.equalsIgnoreCase("Proposta") || stato.equalsIgnoreCase("Confermata")|| stato.equalsIgnoreCase("Cancellata"))
                && postiDisponibili > 0) {
                consoleIO.mostraMessaggio("ID: " + visita.getId());
                consoleIO.mostraMessaggio("Titolo: " + visita.getTitolo());
                consoleIO.mostraMessaggio("Descrizione: " + visita.getDescrizione());
                consoleIO.mostraMessaggio("Luogo: " + visita.getLuogo());
                consoleIO.mostraMessaggio("Tipi Visita: " + visita.getTipiVisitaClassString());
                consoleIO.mostraMessaggio("Data: " + (visita.getData() != null ? visita.getData() : "Nessuna data"));
                consoleIO.mostraMessaggio("Orario: " + (visita.getOraInizio() != null ? visita.getOraInizio() : "Nessun orario"));
                consoleIO.mostraMessaggio("Posti disponibili: " + postiDisponibili);
                consoleIO.mostraMessaggio("Stato: " + stato);
                consoleIO.mostraMessaggio("-------------------------");
                visiteTrovate = true;
            }
        }

        if (!visiteTrovate) {
            System.out.println("Nessuna visita disponibile al momento.");
        }
    }

    //metodo per visualizzare i volontari per ogni tipo di visita
    public void visualizzaVolontariPerTipoVisita(VolontariManager volontariManager){

        if (tipiVisitaList.isEmpty()) {
            consoleIO.mostraMessaggio("Nessun tipo di visita disponibile.");
            return;
        }

        consoleIO.mostraMessaggio ("VOLONTARI PER TIPO DI VISITA");

        for (TipiVisitaClass tipovisita : tipiVisitaList){
            List<Volontario> volontari = volontariManager.getVolontariPerTipoVisita(tipovisita);
            consoleIO.mostraMessaggio("\nTipo di visita: " + tipovisita);
            consoleIO.mostraMessaggio ("Numero volontari assegnati: " + volontari.size());
            
            if (volontari.isEmpty()){
                consoleIO.mostraMessaggio("Nessun volontario assegnato a questo tipo di visita.");
            }else{
                consoleIO.mostraMessaggio ("Volontari assegnati:");
                for (int i=0; i<volontari.size();i++){
                    Volontario v = volontari.get(i);
                    consoleIO.mostraMessaggio((i+1) + ". " + v.getNome() + " " + v.getCognome()); 
                }
            }
        }

    }

    //metodo alternativo per visualizzazione dettagliata di un tipo specifico
    public void visualizzaVolontariPerTipoVisitaSpecifico(VisiteManagerDB visiteManagerDB, VolontariManager volontariManager){
        //mostra i tipi di visita disponibili
        List <TipiVisitaClass> tipiVisitaDisponibili = visiteManagerDB.getTipiVisitaClassList();

        if (tipiVisitaDisponibili.isEmpty()) {
            consoleIO.mostraMessaggio("Nessun tipo di visita disponibile.");
            return ;
        }

        consoleIO.mostraMessaggio ("Seleziona il tipo di visita da visualizzare:");
        consoleIO.mostraElencoConOggetti(tipiVisitaDisponibili);
        int tipoIndex = InputDati.leggiIntero ("Seleziona il numero del tipo di visita: ", 1, tipiVisitaDisponibili.size()) -1;
        TipiVisitaClass tipoVisitaScelto = tipiVisitaDisponibili.get(tipoIndex);
        

        List<Volontario> volontari = volontariManager.getVolontariPerTipoVisita(tipoVisitaScelto);
        consoleIO.mostraMessaggio("\nTipo di visita: " + tipoVisitaScelto);
        consoleIO.mostraMessaggio ("Numero volontari assegnati: " + volontari.size());
        
        if (volontari.isEmpty()){
            consoleIO.mostraMessaggio("Nessun volontario assegnato a questo tipo di visita.");
        }else{
            consoleIO.mostraMessaggio ("Volontari assegnati:");
            consoleIO.mostraElencoConOggetti(volontari);

            //mostra anche gli altri tipi di visita del volontario
            for (Volontario v : volontari) {
                if (!v.getTipiDiVisite().isEmpty()) {
                    consoleIO.mostraMessaggio("   Altri tipi di visita assegnati:");
                    for (TipiVisitaClass altroTipo : v.getTipiDiVisite()) {
                        if (!altroTipo.equals(tipoVisitaScelto)) {
                            consoleIO.mostraMessaggio("   - " + altroTipo);
                        } else {
                            consoleIO.mostraMessaggio("   Nessun altro tipo di visita assegnato.");
                        }
                    }
                }
            }
        }
    }

    public void stampaMaxPersoneIscrivibiliNow() {
    Integer maxDb = null;
        try {
            maxDb = ApplicationSettingsDAO.getMaxPeoplePerVisit();
        } catch (Throwable t) {
            // ignore DB error -> fallback file
        }

        int value;
        if (maxDb != null) {
            value = maxDb.intValue();
        } else {
            // fallback legacy: leggi dal file
            Object props = ServizioFile.caricaProperties(NUMERO_PERSONE_FILE);
            // prova a estrarre un valore numerico da props (Properties o stringa)
            int fallback = 10;
            try {
                if (props instanceof java.util.Properties) {
                    java.util.Properties p = (java.util.Properties) props;
                    if (!p.isEmpty()) {
                        String first = p.values().iterator().next().toString();
                        fallback = Integer.parseInt(first.trim());
                    }
                } else if (props != null) {
                    String s = props.toString().trim();
                    fallback = Integer.parseInt(s);
                }
            } catch (Exception e) {
                // keep default
            }
            value = fallback;
        }

        consoleIO.mostraMessaggio("Il numero massimo di persone iscrivibili da un fruitore è attualmente: " + value);
    }
    
}