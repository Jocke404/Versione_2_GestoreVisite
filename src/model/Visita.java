package src.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Visita {
    private int id;
    private String titolo;
    private String luogo;
    private List<TipiVisitaClass> tipiVisita;
    private String volontario;
    private LocalDate data;
    private int maxPersone; 
    private String stato; 
    private LocalTime oraInizio; 
    private int durataMinuti;
    private int postiPrenotati;
    private int minPartecipanti;
    private boolean biglietto;
    private boolean barriereArchitettoniche;

     
    public Visita(int id, String titolo, String luogo, List<TipiVisitaClass> tipiVisita, String volontario, 
                LocalDate data, int maxPersone, String stato, LocalTime oraInizio, 
                int durataMinuti, int postiPrenotati, int minPartecipanti, boolean biglietto, 
                boolean barriereArchitettoniche) {
        this.id = id;
        this.titolo = titolo;
        this.luogo = luogo;
        this.tipiVisita = tipiVisita;
        this.volontario = volontario;
        this.data = data;
        this.maxPersone = maxPersone;
        this.stato = stato;
        this.oraInizio = oraInizio;
        this.durataMinuti = durataMinuti;
        this.postiPrenotati = postiPrenotati;
        this.minPartecipanti = minPartecipanti;
        this.biglietto = biglietto;
        this.barriereArchitettoniche = barriereArchitettoniche;
    }

    
    public int getPostiPrenotati() {
        return postiPrenotati;
    }


    public void setPostiPrenotati(int postiPrenotati) {
        this.postiPrenotati = postiPrenotati;
    }

    public int getPostiDisponibili() {
        return maxPersone - postiPrenotati;
    }

    public boolean isDisponibile() {
        return getPostiDisponibili() > 0;
    }


    public LocalTime getOraInizio() {
        return oraInizio;
    }


    public void setOraInizio(LocalTime oraInizio) {
        this.oraInizio = oraInizio;
    }


    public int getDurataMinuti() {
        return durataMinuti;
    }


    public void setDurataMinuti(int durataMinuti) {
        this.durataMinuti = durataMinuti;
    }

    public int getId() {
        return id;
    }

    public String getTitolo() {
        return titolo;
    }

    public String getLuogo() {
        return luogo;
    }

    public List<TipiVisitaClass> getTipiVisitaClass() {
        return tipiVisita;
    }

    public String getTipiVisitaClassString() {
        if (tipiVisita == null || tipiVisita.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (TipiVisitaClass tipo : tipiVisita) {
            sb.append(tipo.getNome()).append(", ");
        }
        return sb.substring(0, sb.length() - 2);
    }

    public String getVolontario() {
        return volontario;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public int getMaxPersone() {
        return maxPersone;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public String getDescrizione() {
        return tipiVisita != null && !tipiVisita.isEmpty() ? tipiVisita.get(0).getDescrizione() : "";
    }

    public int getMinPartecipanti() {
        return minPartecipanti;
    }

    public boolean isBiglietto() {
        return biglietto;
    }

    public boolean getBarriereArchitettoniche() {
        return barriereArchitettoniche;
    }

    @Override
    public String toString() {
        return  "Titolo: " + getTitolo()+
            "\nTipo di visita: " + getTipiVisitaClassString() +
            "\nDescrizione: " + getDescrizione() +
            "\nLuogo: " + getLuogo() +
            "\nData: " + getData() +
            "\nOra: " + getOraInizio() +
            "\nStato: " + getStato() +
            "\nDurata: " + getDurataMinuti() + " minuti" +
            "\nNumero massimo di persone: " + getMaxPersone() +
            "\nPosti disponibili: " + getPostiDisponibili() +
            "\nMinimo partecipanti: " + getMinPartecipanti() +
            "\nBiglietto richiesto: " + (isBiglietto() ? "Sì" : "No") +
            "\nBarriere architettoniche: " + (getBarriereArchitettoniche() ? "Sì" : "No");
    }
}
   