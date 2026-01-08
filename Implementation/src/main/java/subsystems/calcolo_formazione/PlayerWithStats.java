package subsystems.calcolo_formazione;

import subsystems.team_management.model.Player;

public class PlayerWithStats {
    private final Player player;
    private final Statistiche statistiche;

    public PlayerWithStats(Player player, Statistiche statistiche) {
        if (player == null) {
            throw new IllegalArgumentException("player is required");
        }
        this.player = player;
        this.statistiche = statistiche;
    }

    public Player getPlayer() {
        return player;
    }

    public Statistiche getStatistiche() {
        return statistiche;
    }

    public String getRuolo() {
        return player.getRuolo();
    }
}
