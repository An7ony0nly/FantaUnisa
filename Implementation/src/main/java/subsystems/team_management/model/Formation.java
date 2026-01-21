package subsystems.team_management.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Formation {
    private int id;
    private String userEmail;
    private int giornata;
    private String modulo; // Stringa "3-4-3"

    // Mappa PlayerID -> RuoloSchierato ("T"=Titolare, "P"=Panchina)
    private Map<Integer, String> playersMap;
    private List<Player> players = new ArrayList<>();

    public Formation() {
        this.playersMap = new HashMap<>();
    }

    // Costruttore rapido
    public Formation(String userEmail, int giornata, String modulo) {
        this.userEmail = userEmail;
        this.giornata = giornata;
        this.modulo = modulo;
        this.playersMap = new HashMap<>();
    }

    public void addPlayer(int playerId, String schieramento) {
        this.playersMap.put(playerId, schieramento);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public int getGiornata() {
        return giornata;
    }

    public void setGiornata(int giornata) {
        this.giornata = giornata;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public Map<Integer, String> getPlayersMap() {
        return playersMap;
    }

    public void setPlayersMap(Map<Integer, String> playersMap) {
        this.playersMap = playersMap;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public void addFullPlayer(Player p) {
        this.players.add(p);
    }
}