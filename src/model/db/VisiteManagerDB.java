package src.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import src.controller.ThreadPoolController;
import src.model.TipiVisitaClass;
import src.model.Visita;
import src.model.Volontario;

public class VisiteManagerDB extends DatabaseManager {
    private ConcurrentHashMap<Integer, Visita> visiteMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<LocalDate, String> datePrecluseMap = new ConcurrentHashMap<>();

    public VisiteManagerDB(ThreadPoolController threadPoolManager) {
        super(threadPoolManager);
        caricaVisite();
        caricaDatePrecluse();
    }

    public static VisiteManagerDB getInstance() {
        return new VisiteManagerDB(ThreadPoolController.getInstance());
    }

    //Logiche delle visite--------------------------------------------------
     
    protected void caricaVisite() {
        String sql = "SELECT id, titolo, luogo, tipo_visita, volontario, data, stato, max_persone, ora_inizio, durata_minuti, posti_prenotati, min_partecipanti, biglietto, barriere_architettoniche FROM visite";
        try (Connection conn = DatabaseConnection.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()) {

            synchronized (visiteMap) {
                visiteMap.clear();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String titolo = rs.getString("titolo");
                    String luogo = rs.getString("luogo");
                    List<TipiVisitaClass> tipoVisita = TipiVisitaClass.fromString(rs.getString("tipo_visita"));
                    String volontario = rs.getString("volontario");
                    LocalDate data = rs.getDate("data") != null ? rs.getDate("data").toLocalDate() : null;  
                    int maxPersone = rs.getInt("max_persone");
                    String stato = rs.getString("stato");
                    LocalTime oraInizio = rs.getTime("ora_inizio") != null ? rs.getTime("ora_inizio").toLocalTime() : null;
                    int durataMinuti = rs.getInt("durata_minuti");
                    int postiPrenotati = rs.getInt("posti_prenotati");
                    int minPartecipanti = rs.getInt("min_partecipanti");
                    boolean biglietto = rs.getBoolean("biglietto");
                    boolean barriereArchitettoniche = rs.getBoolean("barriere_architettoniche");

                     
                    Visita visita = new Visita(id, titolo, luogo, tipoVisita, volontario,
                                                data, maxPersone, stato, oraInizio,
                                                durataMinuti, postiPrenotati, minPartecipanti, biglietto, barriereArchitettoniche);
                    visiteMap.putIfAbsent(id, visita);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il caricamento delle visite: " + e.getMessage());
        }
    }

     
    protected void aggiungiVisita(Visita visita) {
        String inserisciSql = "INSERT INTO visite (luogo, titolo, tipo_visita, volontario, data, stato, max_persone, ora_inizio, durata_minuti, min_partecipanti, biglietto, barriere_architettoniche) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement(inserisciSql)) {
    
                pstmt.setString(1, visita.getLuogo());
                pstmt.setString(2, visita.getTitolo());
                pstmt.setString(3, visita.getTipiVisitaClassString());
                pstmt.setString(4, visita.getVolontario());
                pstmt.setDate(5, visita.getData() != null ? java.sql.Date.valueOf(visita.getData()) : null);
                pstmt.setString(6, visita.getStato());
                pstmt.setInt(7, visita.getMaxPersone());
                pstmt.setTime(8, visita.getOraInizio() != null ? java.sql.Time.valueOf(visita.getOraInizio()) : null);
                pstmt.setInt(9, visita.getDurataMinuti());
                pstmt.setInt(10, visita.getMinPartecipanti());
                pstmt.setBoolean(11, visita.isBiglietto());
                pstmt.setBoolean(12, visita.getBarriereArchitettoniche());
                pstmt.executeUpdate();
    
                consoleIO.mostraMessaggio("Visita aggiunta con successo.");
            } catch (SQLException e) {
                System.err.println("Errore durante l'aggiunta della visita: " + e.getMessage());
            }
    }

    protected void aggiungiNuovoTipoVisita(TipiVisitaClass nuovoTipo) {
        String sql = "INSERT INTO tipi_visita (nome, descrizione) VALUES (?, ?)";
        executorService.submit(() -> {
            try (Connection conn = DatabaseConnection.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, nuovoTipo.getNome());
                pstmt.setString(2, nuovoTipo.getDescrizione());
                pstmt.executeUpdate();

            } catch (SQLException e) {
                System.err.println("Errore durante l'aggiunta del nuovo tipo di visita: " + e.getMessage());
            }
        });
    }

    protected void aggiungiDataPreclusa(LocalDate data, String motivo) {
        String sql = "INSERT INTO date_precluse (data, motivo) VALUES (?, ?)";
        executorService.submit(() -> {
            try (Connection conn = DatabaseConnection.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDate(1, java.sql.Date.valueOf(data));
                pstmt.setString(2, motivo);
                pstmt.executeUpdate();

                synchronized (datePrecluseMap) {
                    datePrecluseMap.putIfAbsent(data, motivo);
                }

            } catch (SQLException e) {
                System.err.println("Errore durante l'aggiunta della data preclusa: " + e.getMessage());
            }
        });
    }

    protected void caricaDatePrecluse() {
        String sql = "SELECT data, motivo FROM date_precluse";
        try (Connection conn = DatabaseConnection.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            synchronized (datePrecluseMap) {
                datePrecluseMap.clear();
                while (rs.next()) {
                    LocalDate data = rs.getDate("data").toLocalDate();
                    String motivo = rs.getString("motivo");
                    datePrecluseMap.putIfAbsent(data, motivo);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il caricamento delle date precluse: " + e.getMessage());
        }
    }

    protected void eliminaDataPreclusa(LocalDate dataDaEliminare) {
        String sql = "DELETE FROM date_precluse WHERE data = ?";
        executorService.submit(() -> {
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDate(1, java.sql.Date.valueOf(dataDaEliminare));
                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    caricaDatePrecluse();
                } else {
                    System.err.println("Nessuna data preclusa trovata da eliminare.");
                }
            } catch (SQLException e) {
                System.err.println("Errore durante l'eliminazione della data preclusa: " + e.getMessage());
            }
        });
    }

     
    protected void aggiornaVisitaDB(int visitaId, Visita visitaAggiornata) {
        String sql = "UPDATE visite SET luogo = ?, tipo_visita = ?, volontario = ?, data = ?, stato = ?, max_persone = ?, ora_inizio = ?, durata_minuti = ? WHERE id = ?";
        executorService.submit(() -> {
            try (Connection conn = DatabaseConnection.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, visitaAggiornata.getLuogo());
                pstmt.setString(2, visitaAggiornata.getTipiVisitaClassString());
                pstmt.setString(3, visitaAggiornata.getVolontario());
                pstmt.setDate(4, visitaAggiornata.getData() != null ? java.sql.Date.valueOf(visitaAggiornata.getData()) : null);
                pstmt.setString(5, visitaAggiornata.getStato());
                pstmt.setInt(6, visitaAggiornata.getMaxPersone());
                pstmt.setTime(7, visitaAggiornata.getOraInizio() != null ? java.sql.Time.valueOf(visitaAggiornata.getOraInizio()) : null);
                pstmt.setInt(8, visitaAggiornata.getDurataMinuti());
                pstmt.setInt(9, visitaId);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Errore durante l'aggiornamento della visita: " + e.getMessage());
            }
        });
    }

    protected void assegnaVisitaAVolontarioDB(Volontario volontarioSelezionato, Visita visitaSelezionata) {
        String sql = "UPDATE visite SET volontario = ? WHERE id = ?";
        executorService.submit(() -> {
            try (Connection conn = DatabaseConnection.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, volontarioSelezionato.getNome()+" "+volontarioSelezionato.getCognome());
                pstmt.setInt(2, visitaSelezionata.getId());
                pstmt.executeUpdate();

                consoleIO.mostraMessaggio("Visita assegnata con successo al volontario.");
            } catch (SQLException e) {
                System.err.println("Errore durante l'assegnazione della visita al volontario: " + e.getMessage());
            }
        });
    }

     
    protected void aggiornaMaxPersonePerVisita(int maxPersonePerVisita) {
        String sql = "UPDATE visite SET max_persone = ?";
        executorService.submit(() -> {
            try (Connection conn = DatabaseConnection.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, maxPersonePerVisita);
                pstmt.executeUpdate();

            } catch (SQLException e) {
                System.err.println("Errore durante l'aggiornamento del numero massimo di persone per visita: " + e.getMessage());
            }
        });
    }

    public void addNuovoTipoVisita(TipiVisitaClass nuovoTipo) {
        String verificaSql = "SELECT 1 FROM tipi_visita WHERE nome = ?";
        if(!recordEsiste(verificaSql, nuovoTipo.getNome())){
            aggiungiNuovoTipoVisita(nuovoTipo);
        } else {
            consoleIO.mostraMessaggio("Il tipo di visita esiste già. Non posso aggiungerlo.");
            return;
        }
    }

    public void aggiungiNuovaVisita(Visita nuovaVisita) {
        String verificaSql = "SELECT 1 FROM visite WHERE luogo = ? AND data = ? AND volontario = ? AND ora_inizio = ?";
        if(!recordEsiste(verificaSql, nuovaVisita.getLuogo(), nuovaVisita.getData(), nuovaVisita.getVolontario(), nuovaVisita.getOraInizio())){
            consoleIO.mostraMessaggio("La visita non esiste. Procedo con l'aggiunta.");
            aggiungiVisita(nuovaVisita);
        } else {
            consoleIO.mostraMessaggio("La visita esiste già. Non posso aggiungerla.");
            return;
        }
    }

    public void aggiungiNuovaDataPreclusa(LocalDate data, String motivo) {
        String verificaSql = "SELECT 1 FROM date_precluse WHERE data = ?";
        if(!recordEsiste(verificaSql, data)){
            aggiungiDataPreclusa(data, motivo);
        } else {
            consoleIO.mostraMessaggio("La data preclusa esiste già. Non posso aggiungerla.");
            return;
        }
    }

     
    protected int getMaxPersoneDefault() {
        String sql = "SELECT max_persone FROM visite";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
    
            if (rs.next()) {
                return rs.getInt("max_persone");
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il recupero del numero massimo di persone: " + e.getMessage());
        }
        return 10;
    }    
    
    protected boolean eliminaVisitaDB(int visitaId){
        String sql = "DELETE FROM visite WHERE id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, visitaId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                visiteMap.remove(visitaId);
                consoleIO.mostraMessaggio("Visita eliminata con successo.");
                return true;
            } else {
                consoleIO.mostraMessaggio("Nessuna visita trovata da eliminare.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Errore durante l'eliminazione della visita: " + e.getMessage());
            return false;
        }
    }

    public int getMaxPersone() {
        return getMaxPersoneDefault();
    }

    public ConcurrentHashMap<Integer, Visita> getVisiteMap() {
        return visiteMap;
    }

    public List<TipiVisitaClass> getTipiVisitaClassList() {
        return List.of(visiteMap.values().stream()
                .flatMap(v -> v.getTipiVisitaClass().stream())
                .distinct()
                .toArray(TipiVisitaClass[]::new));
    }

    public ConcurrentHashMap<LocalDate, String> getDatePrecluseMap() {
        return datePrecluseMap;
    }

    public void eliminaData(LocalDate data){
        eliminaDataPreclusa(data);
        datePrecluseMap.remove(data);
    }

    public void aggiornaVisita(int visitaId, Visita visitaAggiornata){
        aggiornaVisitaDB(visitaId, visitaAggiornata);
    }

    public void aggiornaMaxPersone(int numeroMax) {
        aggiornaMaxPersonePerVisita(numeroMax);
    }
    
    public void eliminaVisita(Visita visita){
        eliminaVisitaDB(visita.getId());
    }

    public void assegnaVisitaAVolontario(Volontario volontarioSelezionato, Visita visitaSelezionata) {
        assegnaVisitaAVolontarioDB(volontarioSelezionato, visitaSelezionata);
    }


}

