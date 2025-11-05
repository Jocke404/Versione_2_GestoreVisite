package src.controller;

import src.view.*;
import src.model.*;


public class AuthenticationController {
    private final CredentialManager credentialManager;
    private final ConsoleIO consoleIO;    
    private Utente utenteLoggato;

    public AuthenticationController(
        CredentialManager credentialManager,
        ConsoleIO consoleIO
    ) {
        this.credentialManager = credentialManager;
        this.consoleIO = consoleIO;
    }

    public boolean autentica() {
        String email = consoleIO.chiediEmail();
        boolean emailPresente = credentialManager.isEmailPresente(email);
        String password = consoleIO.chiediPassword();

        if (emailPresente) {
            Utente utente = credentialManager.autentica(email, password);

            if (utente == null) {
                consoleIO.mostraMessaggio("Credenziali non valide.");
                return false;
            }

             
            if (!credentialManager.isPasswordModificata(email)) {
                consoleIO.mostraMessaggio("Hai credenziali temporanee. Ti preghiamo di modificarle.");
                boolean emailCorretta = consoleIO.chiediConfermaEmail(email);
                if (!emailCorretta) {
                    String nuovaEmail = consoleIO.chiediNuovaEmail(credentialManager);
                    credentialManager.aggiornaEmailUtente(utente, nuovaEmail);
                    consoleIO.mostraMessaggio("Email aggiornata con successo.");
                }
                modificaPasswordUtente(utente);
            }

            if (!credentialManager.isPasswordModificata(email)) {
                if(utente instanceof Configuratore) {
                    String nuovoNome = consoleIO.chiediNome();
                    String nuovoCognome = consoleIO.chiediCognome();
                    credentialManager.aggiornaNomeCognomeConf(utente, nuovoNome, nuovoCognome);
                }
            }


            this.utenteLoggato = utente;
            return true;
        } else {
            consoleIO.mostraMessaggio("Email non registrata. Procedi con la creazione di un nuovo account.");
            String tipoUtente = credentialManager.estraiTipoUtente(email, password);
            Utente nuovoUtente = creaNuovoUtente(tipoUtente);
            this.utenteLoggato = nuovoUtente;
            return true;
        }
    }

    public void modificaPasswordUtente(Utente utente) {
        String nuovaPassword = consoleIO.chiediPassword();
        credentialManager.aggiornaPasswordUtente(utente, nuovaPassword);
        consoleIO.mostraMessaggio("Password aggiornata con successo.");
    }

    public Utente creaNuovoUtente(String tipoUtente) {
        String name = consoleIO.chiediNome();
        String surname = consoleIO.chiediCognome();
        String newEmail = consoleIO.chiediNuovaEmail(credentialManager);
        String newPassword = consoleIO.chiediPassword();
        Utente nuovoUtente = credentialManager.creaNuoveCredenziali(tipoUtente, name, surname, newEmail, newPassword);
        return nuovoUtente;
    }

    public Utente getUtenteCorrente() {
        return utenteLoggato;
    }
}
