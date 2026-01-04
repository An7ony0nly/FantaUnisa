package it.unisa.fantaunisa.model;

import java.util.ArrayList;
import java.util.List;

public class Formazione {
    private int id;
    private int idSquadra;
    private int giornata;
    private String modulo;
    private double totalePunti;
    private List<Giocatore> calciatori;

    public Formazione() {
        this.calciatori = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdSquadra() { return idSquadra; }
    public void setIdSquadra(int idSquadra) { this.idSquadra = idSquadra; }

    public int getGiornata() { return giornata; }
    public void setGiornata(int giornata) { this.giornata = giornata; }

    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }

    public double getTotalePunti() { return totalePunti; }
    public void setTotalePunti(double totalePunti) { this.totalePunti = totalePunti; }

    public List<Giocatore> getCalciatori() { return calciatori; }
    public void setCalciatori(List<Giocatore> calciatori) { this.calciatori = calciatori; }

    public List<Giocatore> getTitolari() {
        if (calciatori == null || calciatori.size() < 11) return new ArrayList<>();
        return calciatori.subList(0, 11);
    }

    public List<Giocatore> getPanchina() {
        if (calciatori == null || calciatori.size() <= 11) return new ArrayList<>();
        return calciatori.subList(11, calciatori.size());
    }
}
