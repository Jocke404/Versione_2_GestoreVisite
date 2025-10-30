package src.controller;

import java.util.concurrent.ExecutorService;

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



    private ViewUtilita viewUtilita;




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
            if (utenteCorrente instanceof Configuratore) {
                if (!ambitoTerritoriale.isAmbitoConfigurato()) {
                    ambitoTerritoriale.scegliAmbitoTerritoriale();
                } else {
                    ambitoTerritoriale.caricaAmbitoTerritoriale();
                }
                menu = menuFactory.creaMenuConfiguratore(configuratoriController);
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
