package src.view;
import java.util.List;

public interface View {
    void mostraMessaggio(String messaggio);

    void mostraErrore(String errore);

    void mostraElenco(List<String> elementi);

    void mostraElencoConOggetti(List<?> oggetti);
    
}
