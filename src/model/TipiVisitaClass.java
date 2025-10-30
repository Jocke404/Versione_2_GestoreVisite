package src.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rappresenta un tipo di visita. Sostituisce l'ex enum per permettere tipi dinamici.
 */
public class TipiVisitaClass {
    private final String nome;        // identificatore (usare unico, es. "GUID" o nome semplice)
    private final String descrizione; // descrizione testuale

    // costruttore pubblico per creare nuovi tipi
    public TipiVisitaClass(String nome, String descrizione) {
        this.nome = nome != null ? nome.trim() : "";
        this.descrizione = descrizione != null ? descrizione : "";
    }

    // costanti built‑in (sostituisci i nomi coi valori che avevi nell'enum)
    public static final TipiVisitaClass STORICA = new TipiVisitaClass("STORICA", "Un percorso guidato alla scoperta della storia e dei monumenti principali della città.");
    public static final TipiVisitaClass SCIENTIFICA = new TipiVisitaClass("SCIENTIFICA", "Un'esperienza educativa dedicata alle scienze e alle innovazioni tecnologiche.");
    public static final TipiVisitaClass ENOGASTRONOMICA = new TipiVisitaClass("ENOGASTRONOMICA", "Un viaggio tra i sapori tipici locali con degustazioni di prodotti tradizionali.");
    public static final TipiVisitaClass LABBAMBINI = new TipiVisitaClass("LABBAMBINI", "Attività ludico-didattiche pensate per i più piccoli, con laboratori creativi e giochi.");

    // registry dinamico thread-safe: key = nome normalizzato, value = TipiVisitaClass
    private static final ConcurrentHashMap<String, TipiVisitaClass> registry = new ConcurrentHashMap<>();

    static {
        // popola registry con built-in (inseriti per primi)
        registry.put(normalizeKey(STORICA.getNome()), STORICA);
        registry.put(normalizeKey(SCIENTIFICA.getNome()), SCIENTIFICA);
        registry.put(normalizeKey(ENOGASTRONOMICA.getNome()), ENOGASTRONOMICA);
        registry.put(normalizeKey(LABBAMBINI.getNome()), LABBAMBINI);
    }

    private static String normalizeKey(String nome) {
        return nome == null ? "" : nome.trim().toLowerCase(Locale.ROOT);
    }

    public String getNome() {
        return nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    @Override
    public String toString() {
        return nome + (descrizione.isEmpty() ? "" : " - " + descrizione);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TipiVisitaClass)) return false;
        TipiVisitaClass that = (TipiVisitaClass) o;
        return nome.equalsIgnoreCase(that.nome);
    }

    /**
     * Factory: restituisce un'istanza built-in se nome corrisponde (case-insensitive),
     * altrimenti crea una nuova istanza dinamica con descrizione vuota.
     */
    public static TipiVisitaClass fromName(String nome) {
        if (nome == null) return null;
        String n = nome.trim();
        if (n.equalsIgnoreCase(STORICA.getNome())) return STORICA;
        if (n.equalsIgnoreCase(SCIENTIFICA.getNome())) return SCIENTIFICA;
        if (n.equalsIgnoreCase(ENOGASTRONOMICA.getNome())) return ENOGASTRONOMICA;
        if (n.equalsIgnoreCase(LABBAMBINI.getNome())) return LABBAMBINI;
        return new TipiVisitaClass(n, "");
    }

    public static List<TipiVisitaClass> fromString(String string) {
        String[] parts = string.split(",");
        return java.util.Arrays.stream(parts)
                .map(String::trim)
                .map(TipiVisitaClass::fromName)
                .toList();
    }

    public static TipiVisitaClass valueOf(String trim) {
        return fromName(trim);
    }

    /**
     * Restituisce i nomi di tutti i tipi disponibili (built-in + custom registrati).
     */
    public static List<String> getAllTypeNames() {
        List<String> out = new ArrayList<>();
        for (TipiVisitaClass v : registry.values()) out.add(v.getNome());
        return out;
    }

    /**
     * Espone la mappa concorrente per accesso avanzato (modifiche attraverso i metodi dedicati).
     */
    public static ConcurrentHashMap<String, TipiVisitaClass> getTipiVisitaClassMap() {
        return registry;
    }

    /**
     * Registra un nuovo tipo custom. Non sovrascrive i tipi esistenti.
     * @return true se il tipo è stato inserito, false se già presente o nome non valido.
     */
    public static boolean registerCustomType(String nome, String descrizione) {
        if (nome == null || nome.trim().isEmpty()) return false;
        String key = normalizeKey(nome);
        TipiVisitaClass toInsert = new TipiVisitaClass(nome.trim(), descrizione == null ? "" : descrizione);
        return registry.putIfAbsent(key, toInsert) == null;
    }

    /**
     * Rimuove un tipo custom; non permette la rimozione dei built-in.
     * @return true se rimosso, false se non presente o built-in (non rimovibile).
     */
    public static boolean removeCustomType(String nome) {
        if (nome == null || nome.trim().isEmpty()) return false;
        String key = normalizeKey(nome);
        // non permettere cancellazione dei built-in
        if (isBuiltInKey(key)) return false;
        return registry.remove(key) != null;
    }

    private static boolean isBuiltInKey(String key) {
        return key.equals(normalizeKey(STORICA.getNome()))
                || key.equals(normalizeKey(SCIENTIFICA.getNome()))
                || key.equals(normalizeKey(ENOGASTRONOMICA.getNome()))
                || key.equals(normalizeKey(LABBAMBINI.getNome()));
    }

    public static List<TipiVisitaClass> values() {
        return new ArrayList<>(registry.values());
    }
        
}
