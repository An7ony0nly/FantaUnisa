package subsystems.ai.model;

public class PlayerStats {
    private int id;
    private String ruolo;
    private String nome;
    private String squadra;
    private int partiteVoto;
    private double mediaVoto;
    private double fantaMedia;
    private int goalFatti;
    private int golSubiti;
    private int assist;
    private double aiScore;

    // NUOVO CAMPO
    private String prossimaAvversaria = "--";

    // Costruttore Completo
    public PlayerStats(int id, String ruolo, String nome, String squadra, int partiteVoto, double mediaVoto, double fantaMedia, int goalFatti, int golSubiti, int assist) {
        this.id = id;
        this.ruolo = ruolo;
        this.nome = nome;
        this.squadra = squadra;
        this.partiteVoto = partiteVoto;
        this.mediaVoto = mediaVoto;
        this.fantaMedia = fantaMedia;
        this.goalFatti = goalFatti;
        this.golSubiti = golSubiti;
        this.assist = assist;
        this.aiScore = fantaMedia; // Default
    }

    // Costruttore Semplificato
    public PlayerStats(int id, String ruolo, String nome, String squadra, int partiteVoto, double mediaVoto, double fantaMedia, int goalFatti, int golSubiti) {
        this(id, ruolo, nome, squadra, partiteVoto, mediaVoto, fantaMedia, goalFatti, golSubiti, 0);
    }

    // --- GETTER E SETTER ---
    public int getId() { return id; }
    public String getRuolo() { return ruolo; }
    public String getNome() { return nome; }
    public String getSquadra() { return squadra; }
    public int getPartiteVoto() { return partiteVoto; }
    public double getMediaVoto() { return mediaVoto; }
    public double getFantaMedia() { return fantaMedia; }
    public int getGoalFatti() { return goalFatti; }
    public int getGolSubiti() { return golSubiti; }
    public int getAssist() { return assist; }

    public double getAiScore() { return aiScore; }
    public void setAiScore(double aiScore) { this.aiScore = aiScore; }

    // NUOVI METODI PER L'AVVERSARIA
    public String getProssimaAvversaria() { return prossimaAvversaria; }
    public void setProssimaAvversaria(String prossimaAvversaria) { this.prossimaAvversaria = prossimaAvversaria; }

    @Override
    public String toString() {
        return String.format("%s (%s) vs %s - FM: %.2f", nome, squadra, prossimaAvversaria, fantaMedia);
    }
}