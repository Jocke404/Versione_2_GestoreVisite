package src.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import src.model.db.VolontariManager;

public class Disponibilita {
    private final Map<String, List<LocalDate>> disponibilitaVolontari = new ConcurrentHashMap<>();


    // Sincronizza la mappa delle disponibilità con il file stato_raccolta.txt
    public void sincronizzaDisponibilitaVolontari() {
        Map<String, List<LocalDate>> dalFile = leggiDisponibilitaDaFile();
        disponibilitaVolontari.clear();
        disponibilitaVolontari.putAll(dalFile);
    }

    public void gestisciVolontariSenzaDisponibilita(VolontariManager volontariManager) {
        LocalDate oggi = LocalDate.now();
        
        // Se siamo dopo il giorno 15, segna tutti i volontari che non hanno inserito disponibilità
        if (oggi.getDayOfMonth() > 15) {
            for (Volontario volontario : volontariManager.getVolontariMap().values()) {
                if (!disponibilitaVolontari.containsKey(volontario.getEmail())) {
                    // Volontario senza disponibilità -> lista vuota
                    disponibilitaVolontari.put(volontario.getEmail(), new ArrayList<>());
                }
            }
            salvaStatoERaccolta(disponibilitaVolontari, "RACCOLTA_CHIUSA");
        }
    }

    public static Map<String, List<LocalDate>> leggiDisponibilitaDaFile() {
        Map<String, List<LocalDate>> disp = new ConcurrentHashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader("src/utility/stato_raccolta.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("disponibilita_volontari=")) {
                    String data = line.substring("disponibilita_volontari=".length());
                    String[] volontariParts = data.split(";");
                    
                    for (String volontarioPart : volontariParts) {
                        if (volontarioPart.trim().isEmpty()) continue;
                        
                        String[] emailDateParts = volontarioPart.split(":");
                        if (emailDateParts.length == 2) {
                            String email = emailDateParts[0].trim();
                            List<LocalDate> date = new ArrayList<>();
                            
                            if (!emailDateParts[1].trim().isEmpty()) {
                                String[] dateParts = emailDateParts[1].split(",");
                                for (String dateStr : dateParts) {
                                    if (!dateStr.trim().isEmpty()) {
                                        date.add(LocalDate.parse(dateStr.trim()));
                                    }
                                }
                            }
                            
                            disp.put(email, date);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Errore nella lettura di stato_raccolta.txt: " + e.getMessage());
        }
        return disp;
    }


    public void salvaDisponibilita(String emailVolontario, List<LocalDate> dateDisponibili) {
        disponibilitaVolontari.put(emailVolontario, dateDisponibili);
        salvaStatoERaccolta(disponibilitaVolontari, "RACCOLTA_APERTA");
    }

    // Salva lo stato raccolta e le disponibilità dei volontari su stato_raccolta.txt
    public static void salvaStatoERaccolta(Map<String, List<LocalDate>> disponibilita, String statoCiclo) {
        try {
            File file = new File("src/utility/stato_raccolta.txt");
            
            try (java.io.PrintWriter writer = new java.io.PrintWriter(file, "UTF-8")) {
                // Scrivi lo stato del ciclo
                writer.println("stato_ciclo=" + statoCiclo);
                
                // Scrivi le disponibilità dei volontari
                writer.print("disponibilita_volontari=");
                boolean first = true;
                
                for (Map.Entry<String, List<LocalDate>> entry : disponibilita.entrySet()) {
                    if (!first) {
                        writer.print(";");
                    }
                    first = false;
                    
                    writer.print(entry.getKey() + ":");
                    
                    // Scrivi le date in formato yyyy-MM-dd
                    boolean firstDate = true;
                    for (LocalDate data : entry.getValue()) {
                        if (!firstDate) {
                            writer.print(",");
                        }
                        firstDate = false;
                        writer.print(data.toString());
                    }
                }
                writer.println();
            }
        } catch (Exception e) {
            System.err.println("Errore nel salvataggio di stato_raccolta.txt: " + e.getMessage());
        }
    }

    public List<LocalDate> calcolaDateDisponibiliVolontario(Volontario volontario, ValidatoreVisite validatoreVisite) {
        // Controllo se siamo oltre il giorno 15 del mese corrente
        LocalDate oggi = LocalDate.now();
        if (oggi.getDayOfMonth() > 15) {
            return null;
        }
        
        LocalDate meseProssimo = oggi.plusMonths(1);
        YearMonth ym = YearMonth.of(meseProssimo.getYear(), meseProssimo.getMonthValue());
        
        List<Integer> giorniDisponibili = validatoreVisite.trovaGiorniDisponibili(volontario, ym);
        
        if (giorniDisponibili.isEmpty()) {
            return null;
        }

        List<LocalDate> dateDisponibili = validatoreVisite.filtraDateDisponibili(giorniDisponibili, ym);
        salvaDisponibilita(volontario.getEmail(), dateDisponibili);
        return dateDisponibili;
    }

}
