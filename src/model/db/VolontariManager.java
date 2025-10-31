package src.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import src.controller.ThreadPoolController;
import src.model.TipiVisitaClass;
import src.model.Volontario;

public class VolontariManager extends DatabaseManager {

    private ConcurrentHashMap<String, Volontario> volontariMap = new ConcurrentHashMap<>();

    public VolontariManager(ThreadPoolController threadPoolManager) {
        super(threadPoolManager);
        caricaVolontari();
    }

    // Metodo per sincronizzare i volontari
    public void sincronizzaVolontari() {
        for (Volontario volontario : volontariMap.values()) {
            aggiungiVolontario(volontario);
            aggiornaPswVolontario(volontario.getEmail(), volontario.getPassword());
        }
        consoleIO.mostraMessaggio("Sincronizzazione dei volontari completata.");
    }

    //Logiche dei volontari--------------------------------------------------
    // Metodo per caricare i volontari dal database e memorizzarli nella HashMap
    protected void caricaVolontari() {
        String sql = "SELECT nome, cognome, email, password, tipi_di_visite FROM volontari";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            synchronized (volontariMap) {
                volontariMap.clear();
                while (rs.next()) {
                    String email = rs.getString("email");
                    String tipiDiVisite = rs.getString("tipi_di_visite");
                    List<TipiVisitaClass> listaTipiVisite = new ArrayList<>();
                    if (tipiDiVisite != null && !tipiDiVisite.isEmpty()) {
                        for (String tipo : tipiDiVisite.split(",")) {
                            listaTipiVisite.add(TipiVisitaClass.valueOf(tipo.trim()));
                        }
                    }
                    Volontario volontario = new Volontario(
                            rs.getString("nome"),
                            rs.getString("cognome"),
                            email,
                            rs.getString("password"),
                            listaTipiVisite
                    );
                    volontariMap.putIfAbsent(email, volontario);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il caricamento dei volontari: " + e.getMessage());
        }
    }

    // Metodo per aggiungere un volontario al database
    protected void aggiungiVolontario(Volontario volontario) {
        String inserisciSqlVolontari = "INSERT INTO volontari (nome, cognome, email, password, tipi_di_visite, password_modificata) VALUES (?, ?, ?, ?, ?, ?)";
    
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(inserisciSqlVolontari)) {
            pstmt.setString(1, volontario.getNome());
            pstmt.setString(2, volontario.getCognome());
            pstmt.setString(3, volontario.getEmail());
            pstmt.setString(4, volontario.getPassword());
            pstmt.setString(5, String.join(",", volontario.getTipiDiVisite().stream().map(TipiVisitaClass::getNome).toArray(String[]::new)));
            pstmt.setBoolean(6, false);
            pstmt.executeUpdate();
            consoleIO.mostraMessaggio("Volontario aggiunto con successo nella tabella 'volontari'.");
    
            // Aggiungi anche nella tabella 'utenti_unificati'
            aggiungiUtenteUnificato(volontario, false);
        } catch (SQLException e) {
            System.err.println("Errore durante l'aggiunta del volontario: " + e.getMessage());
        }
    }

    // Metodo per aggiornare un volontario nel database
    public void aggiornaPswVolontario(String email, String nuovaPassword) {
        String sqlVolontari = "UPDATE volontari SET password = ?, password_modificata = ? WHERE email = ?";
        String sqlUtentiUnificati = "UPDATE utenti_unificati SET password = ?, password_modificata = ? WHERE email = ?";
    
        executorService.submit(() -> {
            try (Connection conn = DatabaseConnection.connect()) {
                try (PreparedStatement pstmtVolontari = conn.prepareStatement(sqlVolontari)) {
                    pstmtVolontari.setString(1, nuovaPassword);
                    pstmtVolontari.setBoolean(2, true); // Imposta password_modificata a true
                    pstmtVolontari.setString(3, email);
                    int rowsUpdatedVolontari = pstmtVolontari.executeUpdate();
    
                    if (rowsUpdatedVolontari > 0) {
                        System.err.println("Password aggiornata con successo nella tabella 'volontari'.");
                    } else {
                        System.err.println("Errore: Nessun volontario trovato con l'email specificata.");
                    }
                }
    
                // Aggiorna la tabella "utenti_unificati"
                try (PreparedStatement pstmtUtenti = conn.prepareStatement(sqlUtentiUnificati)) {
                    pstmtUtenti.setString(1, nuovaPassword);
                    pstmtUtenti.setBoolean(2, true); // Imposta password_modificata a true
                    pstmtUtenti.setString(3, email);
                    int rowsUpdatedUtenti = pstmtUtenti.executeUpdate();
    
                    if (rowsUpdatedUtenti > 0) {
                        System.err.println("Password aggiornata con successo nella tabella 'utenti_unificati'.");
                    } else {
                        System.err.println("Errore: Nessun utente trovato con l'email specificata nella tabella 'utenti_unificati'.");
                    }
                }
            } catch (SQLException e) {
                System.err.println("Errore durante l'aggiornamento della password: " + e.getMessage());
            }
        });
    }

    public void aggiungiNuovoVolontario(Volontario nuovoVolontario) {
        String verificaSql = "SELECT 1 FROM volontari WHERE email = ?";
        if(!recordEsiste(verificaSql, nuovoVolontario.getEmail())){
            aggiungiVolontario(nuovoVolontario);
            consoleIO.mostraMessaggio("Volontario aggiunto con successo.");
        } else {
            consoleIO.mostraMessaggio("Il volontario con email " + nuovoVolontario.getEmail() + " esiste già.");
        }
    }

    //metodo per aggiornare i tipi di visita di un volontario
    protected void aggiornaTipiVisitaClassVolontario(String email, List<TipiVisitaClass> nuoviTipiVisitaClass) {
        String sql= "UPDATE volontari SET tipi_di_visite = ? WHERE email = ?";
        executorService.submit(() -> {
            try (Connection conn = DatabaseConnection.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, String.join(",", nuoviTipiVisitaClass.stream().map(TipiVisitaClass::getNome).toArray(String[]::new)));
                pstmt.setString(2, email);
                int rowsUpdated = pstmt.executeUpdate();
                if (rowsUpdated > 0) {
                    //aggiorna anche nella mappa locale
                    synchronized (volontariMap) {
                        Volontario volontario = volontariMap.get(email);
                        if (volontario != null) {
                            volontario.setTipiDiVisite(nuoviTipiVisitaClass);
                        }
                    }
                    consoleIO.mostraMessaggio("Tipi di visita aggiornati con successo per il volontario " + email);
                }else {
                    consoleIO.mostraMessaggio("Nessun volontario trovato con l'email " + email);
                }
            } catch (SQLException e) {
                System.err.println("Errore durante l'aggiornamento dei tipi di visita: " + e.getMessage());
            }
        });
    }

    // metodo per aggiungere un tipo di visita a un volontaro
    public void aggiungiTipoVisitaAVolontari (String email, TipiVisitaClass tipoVisita){
        synchronized (volontariMap){
            Volontario volontario = volontariMap.get(email);
            if (volontario !=null){
                List<TipiVisitaClass> tipiEsistenti = new ArrayList<>(volontario.getTipiDiVisite());
                if (!tipiEsistenti.contains(tipoVisita)){
                    tipiEsistenti.add(tipoVisita);
                    aggiornaTipiVisitaClassVolontario(email, tipiEsistenti);
                } 
            }
        }
    }

    //metodo per rimuovere tipi di visita da un volontario
    public void rimuoviTipiVisitaClassVolontario (String email, List<TipiVisitaClass> tipiVisitaDaRimuovere){
        String sql = "UPDATE volontari SET tipi_visita = ? WHERE email = ?";
        executorService.submit(() -> {
            try (Connection conn= DatabaseConnection.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)){
                
                synchronized (volontariMap) {
                    Volontario volontario = volontariMap.get(email);
                    if (volontario != null) {
                        //rimuovi i tipi di visita dalla lista
                        List<TipiVisitaClass> nuoviTipiVisitaClass = new ArrayList<>(volontario.getTipiDiVisite());
                        nuoviTipiVisitaClass.removeAll(tipiVisitaDaRimuovere);

                        pstmt.setString (1, String.join(",", nuoviTipiVisitaClass.stream().map(TipiVisitaClass::getNome).toArray(String[]::new)));
                        pstmt.setString(2, email);
                        int rowsUpdated = pstmt.executeUpdate();

                        if (rowsUpdated > 0) {
                            //aggiorna anche nella mappa locale
                            volontario.setTipiDiVisite(nuoviTipiVisitaClass);
                            consoleIO.mostraMessaggio("Tipi di visita rimossi con successo per il volontario " + email);
                        } else {
                            consoleIO.mostraMessaggio("Nessun volontario trovato con l'email " + email);
                        }
                    }
                }
                
            } catch (SQLException e) {
                System.err.println("Errore durante la rimozione dei tipi di visita: " + e.getMessage());
            }
        });
    }

    public int getIdByEmail(String volontario) {
        String sql = "SELECT id FROM volontari WHERE email = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, volontario);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il recupero dell'ID del volontario: " + e.getMessage());
        }
        return -1;
    }

    //metodo per rimuovere un singolo tipo di visita da un volontario
    public void rimuoviTipoVisitaDaVolontario (String email, TipiVisitaClass tipoVisita){
        rimuoviTipiVisitaClassVolontario(email, Arrays.asList(tipoVisita));
    }

    //metodo per ottenere tutti i volontaari per un tipo di visita specifico
    public List<Volontario> getVolontariPerTipoVisita (TipiVisitaClass tipoVisita){
        List<Volontario> volontariPerTipo = new ArrayList<>();
        synchronized (volontariMap) {
            for (Volontario volontario : volontariMap.values()) {
                if (volontario.getTipiDiVisite().contains(tipoVisita)) {
                    volontariPerTipo.add(volontario);
                }
            }
        } return volontariPerTipo;
    }

    //metodo per ottenere tutti i tipi di visita con i relativi volontari
    public Map<TipiVisitaClass, List<Volontario>> getVolontariPerTipoVisita(){
        Map<TipiVisitaClass, List<Volontario>> volontariPerTipo = new HashMap<>();
        synchronized (volontariMap) {
            for (Volontario volontario : volontariMap.values()) {
                for (TipiVisitaClass tipoVisita : volontario.getTipiDiVisite()) {
                    volontariPerTipo.computeIfAbsent(tipoVisita, k -> new ArrayList<>()).add(volontario);
                }
            }
        } return volontariPerTipo;
    }

    public ConcurrentHashMap<String, Volontario> getVolontariMap() {
        return volontariMap;
    }
    
    public void setVolontariMap(ConcurrentHashMap<String, Volontario> volontariMap) {
        this.volontariMap = volontariMap;
    }

    public void modificaPsw(String email, String nuovaPassword) {
        aggiornaPswVolontario(email, nuovaPassword);
    }

}
