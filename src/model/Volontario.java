package src.model;

import java.util.List;

public class Volontario extends Utente {
    private List<TipiVisitaClass> tipiDiVisite; // Tipi di visite a cui il volontario è assegnato

    // Costruttore, getter e setter
    public Volontario(String nome, String cognome, String email, String password, List<TipiVisitaClass> tipiDiVisite) {
        super(email, password, nome, cognome);
        this.tipiDiVisite = tipiDiVisite;
    }
    
    public List<TipiVisitaClass> getTipiDiVisite() {
        return tipiDiVisite;
    }

    public void setTipiDiVisite(List<TipiVisitaClass> tipiDiVisite) {
        this.tipiDiVisite = tipiDiVisite;
    }
    
    public void aggiungiTipoVisita(TipiVisitaClass tipoVisita) {
        if (!this.tipiDiVisite.contains(tipoVisita)) {
            this.tipiDiVisite.add(tipoVisita);
        }
    }
    
    public void rimuoviTipoVisita(TipiVisitaClass tipoVisita) {
        this.tipiDiVisite.remove(tipoVisita);
    }

    public boolean contieneTipoVisita(TipiVisitaClass tipoVisita) {
        return this.tipiDiVisite.contains(tipoVisita);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString())
          .append("\nTipi di Visite:");

        if (tipiDiVisite == null || tipiDiVisite.isEmpty()) {
            sb.append(" []");
        } else {
            for (TipiVisitaClass t : tipiDiVisite) {
                sb.append("\n - ");
                sb.append(t == null ? "null" : t.toString());
            }
        }
        return sb.toString();
    }

}
