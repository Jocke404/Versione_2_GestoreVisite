package src.model.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import src.view.ConsoleIO;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/gestione_visite?useSSL=false";
    private static final String USER = "root"; 
    private static final String PASSWORD = ""; 

    private static ConsoleIO consoleIO = new ConsoleIO();

    public static Connection connect() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            consoleIO.mostraMessaggio("Errore di connessione al database: " + e.getMessage());
            return null;
        }
    }

}