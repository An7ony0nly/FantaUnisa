package subsystems.team_management.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
/*+*/
/**
 * Rappresenta l'insieme dei calciatori posseduti da un utente.
 */
public class Squad implements Serializable {

    private String userEmail;
    private List<Player> players;

    public Squad() {
        this.players = new ArrayList<>();
    }

    public Squad(String userEmail, List<Player> players) {
        this.userEmail = userEmail;
        this.players = players;
    }


    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    /**
     * Metodo di utilità per aggiungere un giocatore alla lista.
     */
    public void addPlayer(Player player) {
        if (this.players == null) {
            this.players = new ArrayList<>();
        }
        this.players.add(player);
    }

    /**
     * Restituisce il numero di giocatori attualmente in rosa.
     */
    public int size() {
        return (players != null) ? players.size() : 0;
    }

    /**
     * Verifica se la rosa è completa (25 giocatori).
     */
    public boolean isComplete() {
        return size() == 25;
    }

    public boolean containsPlayer(int playerId) {
        if (players == null) return false;
        for (Player p : players) {
            if (p.getId() == playerId) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Squad{" +
                "userEmail='" + userEmail + '\'' +
                ", numeroGiocatori=" + size() +
                '}';
    }
}