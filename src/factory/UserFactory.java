package src.factory;
import java.util.List;

import src.model.Configuratore;
import src.model.TipiVisitaClass;
import src.model.Utente;
import src.model.Volontario;


public class UserFactory {
    
    public static final String VOLONTARIO = "Volontario";
    public static final String CONFIGURATORE = "Configuratore";
    public static final String FRUITORE = "Fruitore";

    private UserFactory() {
    }

    public static Utente createUser(String userType, String email, String password,  String nome, String cognome, List<TipiVisitaClass> tipodiVisita) {
        switch (userType) {
            case VOLONTARIO:
                return new Volontario(nome, cognome, email, password, tipodiVisita);
            case CONFIGURATORE:
                return new Configuratore(nome, cognome, email, password);
            default:
                throw new IllegalArgumentException("Tipo di utente sconosciuto: " + userType);
        }
    }
}
