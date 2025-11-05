package src.view;

import lib.MyMenu;
import src.controller.VolontariController;

import java.time.LocalDate;

public class MenuVolontario implements Menu {
    private final VolontariController volontariController; // Updated controller reference
    private static final String[] OPZIONI_VOLONTARIO = {
        "Visualizza visite assegnate",
        "Inserisci disponibilità",
        "Modifica disponibilità",
        "Modifica password",
    };

    public MenuVolontario(VolontariController volontariController) {
        this.volontariController = volontariController;
    }

    @Override
    public void mostraMenu() {
        boolean goOn = true;
        System.out.printf("Oggi è il: %d/%d/%d\n", LocalDate.now().getDayOfMonth(), LocalDate.now().getMonthValue(), LocalDate.now().getYear());
        do {
            MyMenu menu = new MyMenu("Digitare l'opzione desiderata\n", OPZIONI_VOLONTARIO);
            int chosed = menu.scegli();

            switch (chosed) {
                case 1 -> volontariController.visualizzaVisiteVolontario();
                case 2 -> volontariController.raccogliDisponibilitaVolontario();
                case 3 -> volontariController.modificaDisponibilitaVolontario();
                case 4 -> volontariController.modificaPassword();
                case 0 -> goOn = false;
                default -> System.out.println("Opzione non valida.");
            }
        } while (goOn);
    }
}
