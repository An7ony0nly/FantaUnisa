package it.unisa.fantaunisa.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import it.unisa.fantaunisa.model.Giocatore;
import it.unisa.fantaunisa.model.Statistiche;
import it.unisa.fantaunisa.model.AlgoritmoConfig;

public class CalcolaFormazione {

    private static final List<String> MODULI_AMMESSI = Arrays.asList(
        "3-4-3", "3-5-2", "4-3-3", "4-4-2", "4-5-1", "5-3-2", "5-4-1"
    );

    public List<Giocatore> calcolaFormazione(List<Giocatore> rosa, String modulo, int giornataCorrente, AlgoritmoConfig config) {
        
        //validazione modulo
        if (!MODULI_AMMESSI.contains(modulo)) {
            throw new IllegalArgumentException("Modulo non valido: \" + modulo");
        }

        //1. dividiamo la rosa in 4 liste per ruolo
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
        if (!portieri.isEmpty()) {
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
        lista.sort((g1, g2) -> {
            double p1 = calcolaPunteggio(g1, giornata, config);
            double p2 = calcolaPunteggio(g2, giornata, config);
            return Double.compare(p2, p1);
        });
    }

    private double calcolaPunteggio(Giocatore g, int giornataCorrente, AlgoritmoConfig config) {
        Statistiche s = g.getStatistiche();
        if (s == null) return 0.0; 

        double score;
        
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
