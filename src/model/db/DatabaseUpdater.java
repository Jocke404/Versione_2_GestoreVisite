package src.model.db;

import src.controller.ThreadPoolController;
import src.model.TemporaryCredential;
import src.view.ConsoleIO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;


public class DatabaseUpdater {
    private ConcurrentHashMap<String, TemporaryCredential> temporaryCredentials = new ConcurrentHashMap<>();
    private ConsoleIO consoleIO = new ConsoleIO();
    
    private final VolontariManager volontariManager;
    private final ConfiguratoriManager configuratoriManager;
    private final LuoghiManager luoghiManager;
    private final VisiteManagerDB visiteManagerDB;
    private final ExecutorService executorService = ThreadPoolController.getInstance().createThreadPool(4);  
    private Thread aggiornamentoThread;
    private volatile boolean eseguiAggiornamento = true;  

    public DatabaseUpdater(
        VolontariManager volontariManager, 
        ConfiguratoriManager configuratoriManager, 
        LuoghiManager luoghiManager, 
        VisiteManagerDB visiteManagerDB
    ) {
        this.volontariManager = volontariManager;
        this.configuratoriManager = configuratoriManager;
        this.luoghiManager = luoghiManager;
        this.visiteManagerDB = visiteManagerDB;
    }

    //Logiche Thread------------------------------------------------------------------
     
    public void sincronizzaDalDatabase() {
        executorService.submit(() -> {
            if (eseguiAggiornamento) {
                try {
                     
                    volontariManager.caricaVolontari();
                    configuratoriManager.caricaConfiguratori();
                    luoghiManager.caricaLuoghi();
                    visiteManagerDB.caricaVisite();
                    visiteManagerDB.caricaDatePrecluse();
                } catch (Exception e) {
                    System.err.println("Errore durante la sincronizzazione dal database: " + e.getMessage());
                }
            } else {
                return;
            }            
        });
    }

     
    public void avviaSincronizzazioneConSleep() {
        eseguiAggiornamento = true;  
        aggiornamentoThread = new Thread(() -> {
            while (eseguiAggiornamento) {
                try {
                    sincronizzaDalDatabase();
                    Thread.sleep(5000);  
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();  
                    break;  
                }
            }
        });
        aggiornamentoThread.start();
    }

     
    public void arrestaSincronizzazioneConSleep() {
        eseguiAggiornamento = false;  
        if (aggiornamentoThread != null) {
            aggiornamentoThread.interrupt();  
            try {
                aggiornamentoThread.join();  
            } catch (InterruptedException e) {
                System.err.println("Errore durante l'arresto del thread di aggiornamento.");
                Thread.currentThread().interrupt();  
            }
        }
    }

     
    private boolean recordEsiste(String sql, Object... parametri) {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
    
            for (int i = 0; i < parametri.length; i++) {
                pstmt.setObject(i + 1, parametri[i]);
            }
    
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();  
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la verifica dell'esistenza del record: " + e.getMessage());
        }
        return false;
    }



    //Logiche per Credenziali Temporanee--------------------------------------------------
    public void caricaCredenzialiTemporanee() {
        String sql = "SELECT username, password FROM credenziali_temporanee";
    
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
    
            while (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password");
                temporaryCredentials.put(username, new TemporaryCredential(username, password));
            }
    
            consoleIO.mostraMessaggio("Credenziali temporanee caricate con successo.");
        } catch (SQLException e) {
            consoleIO.mostraMessaggio("Errore durante il caricamento delle credenziali temporanee: " + e.getMessage());
        }
    }

    


//Getters e Setters--------------------------------------------------
    public String getTipoUtente(String email, String password){
        String tipo_utente = null;
        String sql = "SELECT tipo_utente, password FROM utenti_unificati WHERE email = ?";

        try (Connection conn = DatabaseConnection.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String dbPassword = rs.getString("password");
                     
                    if (dbPassword != null && dbPassword.equals(password)) {
                        tipo_utente = rs.getString("tipo_utente");
                    }
                }
            }
        } catch (SQLException e) {
            consoleIO.mostraMessaggio("Errore durante la verifica delle credenziali: " + e.getMessage());
        }
        return tipo_utente;
    }

    public boolean isPasswordModificata(String email) {
        String sql = "SELECT password_modificata FROM utenti_unificati WHERE email = ?";
        boolean passwordModificata = false; 
    
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
    
             
            pstmt.setString(1, email);
    
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    passwordModificata = rs.getBoolean("password_modificata");  
                } else {
                    consoleIO.mostraMessaggio("Nessun record trovato per l'email: " + email);
                }
            }
        } catch (SQLException e) {
            consoleIO.mostraMessaggio("Errore durante la verifica del campo password_modificata: " + e.getMessage());
        }
    
        return passwordModificata;
    }

    public ConcurrentHashMap<String, TemporaryCredential> getTemporaryCredentials() {
        return temporaryCredentials;
    }

    public void startDatabaseSync() {
         
        avviaSincronizzazioneConSleep();
    }

    public boolean isEmailPresente(String email) {
        String sql = "SELECT 1 FROM utenti_unificati WHERE email = ?";
        return recordEsiste(sql, email);
    }
}