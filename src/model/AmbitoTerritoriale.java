package src.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import src.model.db.ApplicationSettingsDAO;
import src.view.ConsoleIO;
import lib.InputDati;

public class AmbitoTerritoriale {

    private static final String AMBITO_FILE = "src/utility/ambito_territoriale.config";
    private Set<String> ambitoTerritoriale = new HashSet<>();
    private final ConsoleIO consoleIO = new ConsoleIO();

    public void verificaAggiornaAmbitoTerritoriale() {
        if (!isAmbitoConfigurato()) {
            scegliAmbitoTerritoriale();
            salvaAmbitoTerritoriale();
        } else {
            caricaAmbitoTerritoriale();
        }
    }

    public boolean isAmbitoConfigurato() {
        try {
            if (ApplicationSettingsDAO.hasTerritorialScope()) return true;
        } catch (Throwable t) {}
        // fallback legacy: controllo file
        File file = new File(AMBITO_FILE);
        return file.exists();
    }

    public void scegliAmbitoTerritoriale() {
        consoleIO.mostraMessaggio("Configurazione ambito territoriale (inserisci uno o più comuni).");
        ambitoTerritoriale.clear();
        do {
            String comune = InputDati.leggiStringaNonVuota("Inserisci il nome del comune: ");
            ambitoTerritoriale.add(comune);
        } while (InputDati.yesOrNo("Vuoi aggiungere un altro comune? (s/n): "));
        salvaAmbitoTerritoriale();
        consoleIO.mostraMessaggio("Ambito territoriale configurato: " + ambitoTerritoriale);
    }

    private void salvaAmbitoTerritoriale() {
        List<String> lista = new ArrayList<>(ambitoTerritoriale);
        boolean ok = false;
        try {
            ok = ApplicationSettingsDAO.setTerritorialScope(lista);
        } catch (Throwable t) {
            // ignore, ci sarà fallback su file
        }
        if (!ok) {
            // fallback legacy: scrivi ancora il file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(AMBITO_FILE))) {
                for (String comune : ambitoTerritoriale) {
                    writer.write(comune);
                    writer.newLine();
                }
                ok = true;
            } catch (IOException e) {
                consoleIO.mostraMessaggio("Errore nel salvataggio dell'ambito territoriale.");
            }
        }
        if (ok) consoleIO.mostraMessaggio("Ambito salvato.");
    }

    public void caricaAmbitoTerritoriale() {
        ambitoTerritoriale.clear();
        List<String> fromDb = null;
        try {
            fromDb = ApplicationSettingsDAO.getTerritorialScope();
        } catch (Throwable t) {
            fromDb = null;
        }
        if (fromDb != null && !fromDb.isEmpty()) {
            ambitoTerritoriale.addAll(fromDb);
            return;
        }

        // fallback legacy: leggi dal file e migra su DB
        try (BufferedReader reader = new BufferedReader(new FileReader(AMBITO_FILE))) {
            String line;
            List<String> tmp = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    ambitoTerritoriale.add(trimmed);
                    tmp.add(trimmed);
                }
            }
            if (!tmp.isEmpty()) {
                try {
                    ApplicationSettingsDAO.setTerritorialScope(tmp);
                } catch (Throwable t) { /* ignore */ }
            }
        } catch (IOException e) {
            consoleIO.mostraMessaggio("Errore nel caricamento dell'ambito territoriale.");
        }
    }

    public List<String> getAmbitoTerritoriale() {
        if (ambitoTerritoriale.isEmpty()) {
            caricaAmbitoTerritoriale();
        }
        return new ArrayList<>(ambitoTerritoriale);
    }

}
