package subsystems.ai.model;

public class TeamStats {
    private String nomeSquadra;
    private double indiceDifficolta; // Valore da 0 a 100

    public TeamStats(String nomeSquadra, double indiceDifficolta) {
        this.nomeSquadra = nomeSquadra;
        this.indiceDifficolta = indiceDifficolta;
    }

    public String getNomeSquadra() { return nomeSquadra; }
    public double getIndiceDifficolta() { return indiceDifficolta; }

    // Formatta il numero per non avere troppi decimali (es. 75.4)
    public String getIndiceFormatted() {
        return String.format("%.1f", indiceDifficolta);
    }
}