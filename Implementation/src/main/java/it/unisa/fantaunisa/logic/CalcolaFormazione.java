package it.unisa.fantaunisa.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import it.unisa.fantaunisa.model.Giocatore;
import it.unisa.fantaunisa.model.Statistiche;
import it.unisa.fantaunisa.model.AlgoritmoConfig;

public class CalcolaFormazione {

    public List<Giocatore> calcolaFormazione(List<Giocatore> rosa, String modulo, int giornataCorrente, AlgoritmoConfig config) {
        
        //creiamo 4 liste separate per ruolo
        List<Giocatore> portieri = new ArrayList<>();
        List<Giocatore> difensori = new ArrayList<>();
        List<Giocatore> centrocampisti = new ArrayList<>();
        List<Giocatore> attaccanti = new ArrayList<>();

        for (Giocatore g : rosa) {
            if (g.getRuolo().equalsIgnoreCase("P")) {
                portieri.add(g);
            } else if (g.getRuolo().equalsIgnoreCase("D")) {
                difensori.add(g);
            } else if (g.getRuolo().equalsIgnoreCase("C")) {
                centrocampisti.add(g);
            } else if (g.getRuolo().equalsIgnoreCase("A")) {
                attaccanti.add(g);
            }
        }

        //ordiniamo ogni lista in base al punteggio (decrescente)
        ordinaLista(portieri, giornataCorrente, config);
        ordinaLista(difensori, giornataCorrente, config);
        ordinaLista(centrocampisti, giornataCorrente, config);
        ordinaLista(attaccanti, giornataCorrente, config);

        //capiamo quanti titolari servono per ogni ruolo dal modulo (es. "3-4-3")
        String[] partiModulo = modulo.split("-");
        int numDifensori = Integer.parseInt(partiModulo[0]);
        int numCentrocampisti = Integer.parseInt(partiModulo[1]);
        int numAttaccanti = Integer.parseInt(partiModulo[2]);

        List<Giocatore> formazioneFinale = new ArrayList<>();

        //inseriamo i titolari
        //portiere titolare (sempre 1)
        if (portieri.size() > 0) {
            formazioneFinale.add(portieri.get(0));
        }

        //difensori titolari
        for (int i = 0; i < numDifensori && i < difensori.size(); i++) {
            formazioneFinale.add(difensori.get(i));
        }

        //centrocampisti titolari
        for (int i = 0; i < numCentrocampisti && i < centrocampisti.size(); i++) {
            formazioneFinale.add(centrocampisti.get(i));
        }

        //attaccanti titolari
        for (int i = 0; i < numAttaccanti && i < attaccanti.size(); i++) {
            formazioneFinale.add(attaccanti.get(i));
        }

        //inseriamo la panchina (tutti quelli rimasti fuori)
        //altri portieri (dal secondo in poi)
        for (int i = 1; i < portieri.size(); i++) {
            formazioneFinale.add(portieri.get(i));
        }

        //altri difensori
        for (int i = numDifensori; i < difensori.size(); i++) {
            formazioneFinale.add(difensori.get(i));
        }

        //altri centrocampisti
        for (int i = numCentrocampisti; i < centrocampisti.size(); i++) {
            formazioneFinale.add(centrocampisti.get(i));
        }

        //altri attaccanti
        for (int i = numAttaccanti; i < attaccanti.size(); i++) {
            formazioneFinale.add(attaccanti.get(i));
        }

        return formazioneFinale;
    }

    private void ordinaLista(List<Giocatore> lista, int giornata, AlgoritmoConfig config) {
        Collections.sort(lista, new Comparator<Giocatore>() {
            @Override
            public int compare(Giocatore g1, Giocatore g2) {
                double p1 = calcolaPunteggio(g1, giornata, config);
                double p2 = calcolaPunteggio(g2, giornata, config);
                
                if (p1 < p2) return 1;
                if (p1 > p2) return -1;
                return 0;
            }
        });
    }

    private double calcolaPunteggio(Giocatore g, int giornataCorrente, AlgoritmoConfig config) {
        Statistiche s = g.getStatistiche();
        if (s == null) return 0.0; 

        double score = 0.0;
        
        //calcolo costanza
        double costanza = ((double) s.getPartiteVoto() / giornataCorrente) * 100.0;

        if (g.getRuolo().equalsIgnoreCase("P")) {
            //portieri
            double mvNormalizzata = (s.getMediaVoto() / 8.0) * 100.0;
            
            double malusGol = ((double) s.getGolSubiti() / s.getPartiteVoto()) * 40.0; 
            double gsScore = 100.0 - malusGol;

            score = (mvNormalizzata * config.getPesoMvPortiere()) + 
                    (gsScore * config.getPesoGsPortiere()) + 
                    (costanza * config.getPesoCostanzaPortiere());

        } else {
            //giocatori movimento
            double fmNormalizzata = (s.getFantaMedia() / 11.0) * 100.0;

            double fattoreGol = ((double) s.getGolFatti() / s.getPartiteVoto()) * 100.0;

            double fattoreAssist = ((double) s.getAssist() / s.getPartiteVoto()) * 100.0;

            score = (fmNormalizzata * config.getPesoFmGiocatore()) + 
                    (costanza * config.getPesoCostanzaGiocatore()) +
                    (fattoreGol * config.getPesoGol()) +
                    (fattoreAssist * config.getPesoAssist());
        }

        return score;
    }
}
