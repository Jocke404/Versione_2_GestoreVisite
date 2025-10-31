package src.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.sql.Date;

import src.controller.ThreadPoolController;
import src.model.Volontario;

public class DisponibilitaManager{

    // Salva la mappa Volontario -> List<LocalDate> nel DB
    public void salvaDisponibilitaVolontari(Map<Volontario, List<LocalDate>> merged, VolontariManager volontariManager) {
        if (merged == null || merged.isEmpty()) {
            return;
        }
        
        String deleteSql = "DELETE FROM disponibilita WHERE volontario_id = ?";
        String insertSql = "INSERT INTO disponibilita (volontario_id, data_disponibile) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.connect();
            PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
            PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            
            conn.setAutoCommit(false);
            
            try {
                // Prima elimina tutte le disponibilità esistenti per i volontari interessati
                for (Volontario volontario : merged.keySet()) {
                    int id = volontariManager.getIdByEmail(volontario.getEmail());
                    deleteStmt.setInt(1, id);
                    deleteStmt.addBatch();
                }
                deleteStmt.executeBatch();
                
                // Poi inserisce tutte le nuove disponibilità
                for (Map.Entry<Volontario, List<LocalDate>> entry : merged.entrySet()) {
                    Volontario volontario = entry.getKey();
                    List<LocalDate> dateDisponibili = entry.getValue();
                    int id = volontariManager.getIdByEmail(volontario.getEmail());
                    
                    if (dateDisponibili != null) {
                        for (LocalDate data : dateDisponibili) {
                            insertStmt.setInt(1, id);
                            insertStmt.setDate(2, Date.valueOf(data));
                            insertStmt.addBatch();
                        }
                    }
                }
                insertStmt.executeBatch();
                
                conn.commit();
                
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Errore durante il salvataggio delle disponibilità", e);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Errore di connessione al database", e);
        }
    }

    public List<LocalDate> getDisponibilitaByVolontarioId(int volontarioId) {
        List<LocalDate> disponibilita = new ArrayList<>();
        String sql = "SELECT data_disponibile FROM disponibilita WHERE volontario_id = ? ORDER BY data_disponibile";

        try (Connection conn = DatabaseConnection.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, volontarioId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                java.sql.Date sqlDate = rs.getDate("data_disponibile");
                disponibilita.add(sqlDate.toLocalDate());
            }

        } catch (SQLException e) {
            System.err.println("Errore durante il caricamento delle disponibilità per volontario ID: " + volontarioId + ": " + e.getMessage());
        }

        return disponibilita;
    }

    public ConcurrentHashMap<String, List<LocalDate>> getDisponibilitaMap(VolontariManager volontariManager) {
        ConcurrentHashMap<String, List<LocalDate>> disponibilitaMap = new ConcurrentHashMap<>();
        String sql = "SELECT volontario_id, data_disponibile FROM disponibilita";

        try (Connection conn = DatabaseConnection.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int volontarioId = rs.getInt("volontario_id");
                Date sqlDate = rs.getDate("data_disponibile");
                LocalDate data = sqlDate.toLocalDate();

                disponibilitaMap.computeIfAbsent(volontariManager.getEmailById(volontarioId), k -> new ArrayList<>()).add(data);
            }

        } catch (SQLException e) {
            System.err.println("Errore durante il caricamento delle disponibilità: " + e.getMessage());
        }

        return disponibilitaMap;
    }

}