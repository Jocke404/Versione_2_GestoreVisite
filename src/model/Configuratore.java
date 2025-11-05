package src.model;


public class Configuratore extends Utente {
     
     
     
     

     
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
