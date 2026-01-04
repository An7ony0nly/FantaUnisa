package it.unisa.fantaunisa.model;

public class Giocatore {
    private int id;
    private String nome;
    private String ruolo; // "P", "D", "C", "A"
    private String squadraSerieA;
    private int quotazione;
    private Statistiche statistiche; //statistiche correnti per l'algoritmo

    public Giocatore() {
    }

    public Giocatore(int id, String nome, String ruolo, String squadraSerieA, int quotazione) {
        this.id = id;
        this.nome = nome;
        this.ruolo = ruolo;
        this.squadraSerieA = squadraSerieA;
        this.quotazione = quotazione;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getRuolo() { return ruolo; }
    public void setRuolo(String ruolo) { this.ruolo = ruolo; }

    public String getSquadraSerieA() { return squadraSerieA; }
    public void setSquadraSerieA(String squadraSerieA) { this.squadraSerieA = squadraSerieA; }

    public int getQuotazione() { return quotazione; }
    public void setQuotazione(int quotazione) { this.quotazione = quotazione; }

    public Statistiche getStatistiche() { return statistiche; }
    public void setStatistiche(Statistiche statistiche) { this.statistiche = statistiche; }
}
