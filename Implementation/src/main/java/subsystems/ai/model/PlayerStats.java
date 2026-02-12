package subsystems.ai.model;

public class PlayerStats {
    private int id;
    private String ruolo; // P, D, C, A
    private String nome;
    private String squadra;
    private int partiteVoto; // Pv
    private double mediaVoto; // Mv
    private double fantaMedia; // Fm
    private int goalFatti; // Gf
    private int assist; // Ass

    // Algoritmo Score: Questo è il valore che la AI userà per decidere
    private double aiScore;

    public PlayerStats(int id, String ruolo, String nome, String squadra, int pv, double mv, double fm, int gf, int ass) {
        this.id = id;
        this.ruolo = ruolo;
        this.nome = nome;
        this.squadra = squadra;
        this.partiteVoto = pv;
        this.mediaVoto = mv;
        this.fantaMedia = fm;
        this.goalFatti = gf;
        this.assist = ass;
        this.aiScore = calculateAIScore();
    }

    // --- IL CUORE DELL'ALGORITMO ---
    private double calculateAIScore() {
        if (partiteVoto == 0) return 0.0;

        // Calcolo affidabilità (es. su 21 giornate ipotetiche o dinamiche)
        // Per renderlo più robusto, usiamo un divisore fisso o stimato se non abbiamo il dato 'giornata corrente'
        double affidabilita = (double) partiteVoto / 21.0;
        if (affidabilita > 1.0) affidabilita = 1.0;

        double malusPanchinaro = (affidabilita < 0.5) ? 0.85 : 1.0;
        double bonusGoal = (goalFatti * 0.2);

        return (fantaMedia * malusPanchinaro) + bonusGoal;
    }

    // --- GETTERS (FONDAMENTALI PER LA JSP) ---
    // Senza questi, la pagina web non può leggere i dati e resta vuota!

    public int getId() { return id; }

    public String getRuolo() { return ruolo; }

    public String getNome() { return nome; }

    public String getSquadra() { return squadra; }

    public int getPartiteVoto() { return partiteVoto; }

    public double getMediaVoto() { return mediaVoto; }

    public double getFantaMedia() { return fantaMedia; }

    public int getGoalFatti() { return goalFatti; }

    public int getAssist() { return assist; }
    public void setAiScore(double score) {
        this.aiScore = score;
    }
    public double getAiScore() { return aiScore; }
}