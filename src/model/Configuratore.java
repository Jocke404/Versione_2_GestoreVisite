package src.model;


public class Configuratore extends Utente {
    // private String nome;
    // private String cognome;
    // private String email;
    // private String password;

    // Costruttore, getter e setter
    public Configuratore(String nome, String cognome, String email, String password) {
        super(email, password, nome, cognome);
    }

    @Override
    public String toString() {
        return "Configuratore{" +
                "nome='" + getNome() + '\'' +
                ", cognome='" + getCognome() + '\'' +
                ", email='" + getEmail() + '\'' +
                '}';
    }

}
