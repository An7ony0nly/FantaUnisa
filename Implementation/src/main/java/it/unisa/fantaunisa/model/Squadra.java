package it.unisa.fantaunisa.model;

import java.util.List;

public class Squadra {
    private int id;
    private String idUtente; //email dell'utente
    private String nomeSquadra;
    private List<Giocatore> rosa;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIdUtente() { return idUtente; }
    public void setIdUtente(String idUtente) { this.idUtente = idUtente; }

    public String getNomeSquadra() { return nomeSquadra; }
    public void setNomeSquadra(String nomeSquadra) { this.nomeSquadra = nomeSquadra; }

    public List<Giocatore> getRosa() { return rosa; }
    public void setRosa(List<Giocatore> rosa) { this.rosa = rosa; }
}
