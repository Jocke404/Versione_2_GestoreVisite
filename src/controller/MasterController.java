package src.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import src.factory.MenuFactory;
import src.model.*;
import src.model.db.*;
import src.view.*;


public class MasterController {

    public Volontario volontarioCorrente;
    public Configuratore configuratoreCorrente;
    public Utente utenteCorrente;
    private ThreadPoolController threadPoolController;
    private VolontariManager volontariManager;
    private ConfiguratoriManager configuratoriManager;
    private LuoghiManager luoghiManager;
    private VisiteManagerDB visiteManager;
    private DatabaseUpdater databaseUpdater;
    private AggiuntaUtilita aggiuntaUtilita;
    private ModificaUtilita modificaUtilita;
    private AuthenticationController authenticationController;
    private VolontariController volontariController;
    private ConfiguratoriController configuratoriController;
    private LuoghiController luoghiController;
    private VisiteController visiteController;
    private ValidatoreVisite validatore;
    private AmbitoTerritoriale ambitoTerritoriale = new AmbitoTerritoriale();
    private MenuFactory menuFactory = new MenuFactory();
    private ConsoleIO consoleIO = new ConsoleIO();
    private ScheduledExecutorService scheduledExecutor;
    private ViewUtilita viewUtilita;
    private Disponibilita disponibilita = new Disponibilita();




    private Boolean isAuth = false;


    public MasterController(){}

    public MasterController createApp() {

        threadPoolController = ThreadPoolController.getInstance();
        volontariManager = new VolontariManager(threadPoolController);
        configuratoriManager = new ConfiguratoriManager(threadPoolController);
        luoghiManager = new LuoghiManager(threadPoolController);
        visiteManager = new VisiteManagerDB(threadPoolController);
        databaseUpdater = new DatabaseUpdater(volontariManager, configuratoriManager, luoghiManager, visiteManager);
        aggiuntaUtilita = new AggiuntaUtilita(volontariManager, luoghiManager, visiteManager);
        modificaUtilita = new ModificaUtilita(visiteManager);
        viewUtilita = ViewUtilita.getInstance();
        visiteController = new VisiteController(visiteManager);
        luoghiController = new LuoghiController(luoghiManager, viewUtilita);
        validatore = new ValidatoreVisite(visiteManager);

        ConsoleIO consoleIO = new ConsoleIO();
        CredentialManager credentialManager = new CredentialManager(
            databaseUpdater, volontariManager, configuratoriManager);
        
        AuthenticationController authenticationController = new AuthenticationController(
            credentialManager, consoleIO);

        MasterController masterController = this;
        masterController.authenticationController = authenticationController;
        masterController.volontariController = volontariController;
        masterController.configuratoriController = configuratoriController;
        masterController.validatore = validatore;
        return masterController;
    }

    public void startApp() {
        if (autentica()) {
            threadPoolController.createThreadPool(1).submit(() -> {
                try {
                    validatore.gestioneVisiteAuto();
                    validatore.gestioneDatePrecluseAuto();
                    disponibilita.sincronizzaDisponibilitaVolontari(volontariManager);
                } catch (Throwable t) {
                    System.err.println("Errore gestioneVisiteAuto (immediato): " + t.getMessage());
                }
            });

            // programma l'esecuzione ripetuta (una volta ogni 24 ore)
            if (scheduledExecutor == null || scheduledExecutor.isShutdown()) {
                scheduledExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread t = new Thread(r);
                    t.setDaemon(true);
                    t.setName("validatore-visite-scheduler");
                    return t;
                });
                scheduledExecutor.scheduleAtFixedRate(() -> {
                    try {
                        validatore.gestioneVisiteAuto();
                    } catch (Throwable t) {
                        System.err.println("Errore gestioneVisiteAuto (scheduler): " + t.getMessage());
                    }
                }, 5, 5, TimeUnit.SECONDS); 
            }
            showMenu();
            aggiornaDatabaseAsync();
        }
    }

    private void aggiornaDatabaseAsync() {
        ExecutorService executor = threadPoolController.createThreadPool(4);
        executor.submit(()->{
            databaseUpdater.sincronizzaDalDatabase();
        });
    
    }

    public void stopExecutorService() {
        threadPoolController.shutdownAll();
    }

    private boolean autentica() {
        final int maxAttempts = 3;
        int attempt = 0;
        isAuth = false;
        while (attempt < maxAttempts && !isAuth) {
            attempt++;
            isAuth = authenticationController.autentica();
            if (!isAuth) {
                consoleIO.mostraMessaggio("Autenticazione fallita (" + attempt + "/" + maxAttempts + ").");
                if (attempt < maxAttempts) {
                    consoleIO.mostraMessaggio("Riprova.");
                }
            }
        }

        if (isAuth) {
            utenteCorrente = authenticationController.getUtenteCorrente();
            volontariController = new VolontariController(volontariManager, aggiuntaUtilita, consoleIO, volontarioCorrente, validatore, viewUtilita);
            configuratoriController = new ConfiguratoriController(aggiuntaUtilita, modificaUtilita, viewUtilita, volontariController, luoghiController, visiteController, visiteManager, volontariManager, luoghiManager);
        } else {
            utenteCorrente = null;
            consoleIO.mostraMessaggio("Numero massimo di tentativi superato. Accesso negato.");
        }
        return isAuth;
    }

    private void showMenu() {
        Menu menu = null;
        if (isAuth) {
            System.out.println("Buongiorno " + utenteCorrente.getNome() + "!");
            if (utenteCorrente instanceof Configuratore){
                ambitoTerritoriale.verificaAggiornaAmbitoTerritoriale();
                menu = menuFactory.creaMenuConfiguratore(configuratoriController);
            } else if (utenteCorrente instanceof Volontario){
                volontariController.volontarioCorrente = (Volontario) utenteCorrente;  
                menu = menuFactory.creaMenuVolontario(volontariController);
            } else {
                consoleIO.mostraMessaggio("Errore: tipo di utente non riconosciuto.");
            }
        } else {
            consoleIO.mostraMessaggio("Accesso negato. Effettua prima l'autenticazione.");
        }
        if (menu != null) {
            menu.mostraMenu();
        } else {
            consoleIO.mostraMessaggio("Errore nella creazione del menu.");
        }
    }


}
