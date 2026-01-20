package utils;

import subsystems.team_management.model.Formation;
import subsystems.team_management.model.Player;
import subsystems.module_selection.model.Module;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
/*+*/
public class RandomFormation {

    public static void generateRandomLineup(Formation formation, List<Player> rosa, Module modulo) {

        // 1. Separiamo la rosa per ruoli
        List<Player> portieri = rosa.stream().filter(p -> p.getRuolo().equalsIgnoreCase("P")).collect(Collectors.toList());
        List<Player> difensori = rosa.stream().filter(p -> p.getRuolo().equalsIgnoreCase("D")).collect(Collectors.toList());
        List<Player> centrocampisti = rosa.stream().filter(p -> p.getRuolo().equalsIgnoreCase("C")).collect(Collectors.toList());
        List<Player> attaccanti = rosa.stream().filter(p -> p.getRuolo().equalsIgnoreCase("A")).collect(Collectors.toList());

        // 2. Controllo Validità
        if (portieri.isEmpty())
            throw new IllegalArgumentException("Rosa incompleta: Manca il portiere.");
        if (difensori.size() < modulo.getDifensori())
            throw new IllegalArgumentException("Non hai abbastanza difensori per il modulo " + modulo.getId());
        if (centrocampisti.size() < modulo.getCentrocampisti())
            throw new IllegalArgumentException("Non hai abbastanza centrocampisti per il modulo " + modulo.getId());
        if (attaccanti.size() < modulo.getAttaccanti())
            throw new IllegalArgumentException("Non hai abbastanza attaccanti per il modulo " + modulo.getId());

        // 3. Mescoliamo le liste
        Collections.shuffle(portieri);
        Collections.shuffle(difensori);
        Collections.shuffle(centrocampisti);
        Collections.shuffle(attaccanti);

        // 4. Selezione TITOLARI ("T")
        // Rimuoviamo dalla lista i giocatori scelti, così restano solo le riserve

        // Portiere
        formation.addPlayer(portieri.remove(0).getId(), "T");

        // Difensori
        for (int i = 0; i < modulo.getDifensori(); i++) {
            formation.addPlayer(difensori.remove(0).getId(), "T");
        }

        // Centrocampisti
        for (int i = 0; i < modulo.getCentrocampisti(); i++) {
            formation.addPlayer(centrocampisti.remove(0).getId(), "T");
        }

        // Attaccanti
        for (int i = 0; i < modulo.getAttaccanti(); i++) {
            formation.addPlayer(attaccanti.remove(0).getId(), "T");
        }

        // 5. Selezione PANCHINA ("P")
        List<Player> panchinari = new ArrayList<>();
        panchinari.addAll(portieri);
        panchinari.addAll(difensori);
        panchinari.addAll(centrocampisti);
        panchinari.addAll(attaccanti);

        int maxPanchina = Math.min(panchinari.size(), 12);

        for (int i = 0; i < maxPanchina; i++) {
            formation.addPlayer(panchinari.get(i).getId(), "P");
        }
    }
}