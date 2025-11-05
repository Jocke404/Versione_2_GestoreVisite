package src.model;

import java.util.List;

import src.factory.UserFactory;
import src.model.db.*;

public class CredentialManager {

    private final DatabaseUpdater databaseUpdater;
    private final VolontariManager volontariManager;
    private final ConfiguratoriManager configuratoriManager;
    private DatabaseManager databaseManager;



    private Volontario volontarioCorrente = null;
    private Configuratore configuratoreCorrente = null;

    public CredentialManager(DatabaseUpdater databaseUpdater, VolontariManager volontariManager, ConfiguratoriManager configuratoriManager) {
        this.databaseUpdater = databaseUpdater;
        this.volontariManager = volontariManager;
        this.configuratoriManager = configuratoriManager;
    }

    public Utente autentica(String email, String password) {
        String tipoUtente = estraiTipoUtente(email, password);

        if (tipoUtente == null) {
            return null;
        }

        String nome = null;
        String cognome = null;
        List<TipiVisitaClass> tipidiVisite = null;

        switch (tipoUtente) {
            case UserFactory.VOLONTARIO:
                volontarioCorrente = volontariManager.getVolontariMap().get(email);
                if (volontarioCorrente == null) {
                    return null;
                }
                nome = volontarioCorrente.getNome();
                cognome = volontarioCorrente.getCognome();
                tipidiVisite = volontarioCorrente.getTipiDiVisite();
                break;

            case UserFactory.CONFIGURATORE:
                configuratoreCorrente = configuratoriManager.getConfiguratoriMap().get(email);
                if (configuratoreCorrente == null) {
                    return null;
                }
                nome = configuratoreCorrente.getNome();
                cognome = configuratoreCorrente.getCognome();
                break;

            default:
                return null;
        }

        return UserFactory.createUser(tipoUtente, email, password, nome, cognome, tipidiVisite);
    }

    public void caricaCredenzialiTemporanee() {
        databaseUpdater.getTemporaryCredentials();
    }

    public void aggiornaPasswordUtente(Utente utente, String nuovaPassword) {
        utente.setPassword(nuovaPassword);
        if (utente instanceof Volontario) {
            volontariManager.getVolontariMap().put(utente.getEmail(), (Volontario) utente);
            volontariManager.aggiornaPswVolontario(utente.getEmail(), nuovaPassword);
        }
        else if (utente instanceof Configuratore) {
            configuratoriManager.getConfiguratoriMap().put(utente.getEmail(), (Configuratore) utente);
            configuratoriManager.aggiornaPswConfiguratore(utente.getEmail(), nuovaPassword);
        }
    }

    public Utente creaNuoveCredenziali(String tipoUtente, String name, String surname, String newEmail, String newPassword) {
        Utente nuovoUtente = UserFactory.createUser(tipoUtente, newEmail, newPassword, name, surname, null);

        switch (tipoUtente) {
            case UserFactory.CONFIGURATORE:
                configuratoriManager.getConfiguratoriMap().put(newEmail, (Configuratore) nuovoUtente);
                configuratoriManager.aggiungiNuovoConf((Configuratore) nuovoUtente);
                break;
        }
        return nuovoUtente;
    }

     
    public String estraiTipoUtente(String email, String password) {
        String tipo_utente = databaseUpdater.getTipoUtente(email, password);
        return tipo_utente;
    }

     
    public boolean isPasswordModificata(String email) {
        Boolean passwordModificata = databaseUpdater.isPasswordModificata(email);
        return passwordModificata;
    }

    public boolean isEmailPresente(String email) {
        return databaseUpdater.isEmailPresente(email);
    }

    public void aggiornaEmailUtente(Utente utente, String nuovaEmail) {
        utente.setEmail(nuovaEmail);
        if (utente instanceof Volontario) {
            volontariManager.getVolontariMap().remove(utente.getEmail());
            volontariManager.getVolontariMap().put(nuovaEmail, (Volontario) utente);
            databaseManager.aggiornaEmail(utente, nuovaEmail);
        }
        else if (utente instanceof Configuratore) {
            configuratoriManager.getConfiguratoriMap().remove(utente.getEmail());
            configuratoriManager.getConfiguratoriMap().put(nuovaEmail, (Configuratore) utente);
            databaseManager.aggiornaEmail(utente, nuovaEmail);
        }
    }

    public void aggiornaNomeCognomeConf(Utente utente, String nuovoNome, String nuovoCognome) {
        configuratoreCorrente = (Configuratore) utente;
        configuratoreCorrente.setNome(nuovoNome);
        configuratoreCorrente.setCognome(nuovoCognome);
        configuratoriManager.aggiornaNomeCognome(utente.getEmail(), configuratoreCorrente);
    }

}
