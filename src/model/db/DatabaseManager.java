package src.model.db;

import src.model.Utente;
import src.view.ConsoleIO;
import src.controller.ThreadPoolController;
import java.sql.*;
import java.util.concurrent.ExecutorService;

public abstract class DatabaseManager {
    protected ConsoleIO consoleIO;
    protected ExecutorService executorService;

    public DatabaseManager(ThreadPoolController threadPoolManager) {
        this.executorService = threadPoolManager.createThreadPool(4);
        this.consoleIO = new ConsoleIO();
    }

    public boolean recordEsiste(String sql, Object... parametri) {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < parametri.length; i++) {
                pstmt.setObject(i + 1, parametri[i]);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            consoleIO.mostraErrore("Errore verifica record: " + e.getMessage());
        }
        return false;
    }

    public boolean aggiornaEmail(Utente utente, String nuovaEmail) {
        String vecchiaEmail = utente.getEmail();
        String tipoUtente = utente.getClass().getSimpleName();
        String sql = "UPDATE " + tipoUtente.toLowerCase() + " SET email = ? WHERE email = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nuovaEmail);
            pstmt.setString(2, vecchiaEmail);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                utente.setEmail(nuovaEmail);
                consoleIO.mostraMessaggio("Email aggiornata con successo.");
                return true;
            } else {
                consoleIO.mostraErrore("Nessun record aggiornato. Email non trovata.");
            }
        } catch (SQLException e) {
            consoleIO.mostraErrore("Errore aggiornamento email: " + e.getMessage());
        }
        return false;
    }

     
    protected void aggiungiUtenteUnificato(Utente utente, boolean passwordModificata) {
        String nome = utente.getNome();
        String cognome = utente.getCognome();
        String email = utente.getEmail();
        String password = utente.getPassword();
        String tipoUtente = utente.getClass().getSimpleName();

        String inserisciSqlUtentiUnificati = "INSERT INTO utenti_unificati (nome, cognome, email, password, tipo_utente, password_modificata) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(inserisciSqlUtentiUnificati)) {
            pstmt.setString(1, nome);
            pstmt.setString(2, cognome);
            pstmt.setString(3, email);
            pstmt.setString(4, password);
            pstmt.setString(5, tipoUtente);
            pstmt.setBoolean(6, passwordModificata);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Errore durante l'aggiunta dell'utente nella tabella 'utenti_unificati': " + e.getMessage());
        }
    }
}
