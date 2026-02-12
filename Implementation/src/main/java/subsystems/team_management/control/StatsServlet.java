package subsystems.team_management.control;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import subsystems.ai.engine.FormationAI;
import subsystems.ai.model.PlayerStats;
import subsystems.ai.model.TeamStats;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

// Questo URL mappa la servlet quando clicchi "Statistiche"
@WebServlet("/Statistiche")
public class StatsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // 1. Recupera tutti i giocatori usando il motore AI esistente
        FormationAI aiEngine = new FormationAI();
        String csvDirPath = getServletContext().getRealPath("/resources/csv/");
        File csvDir = new File(csvDirPath);

        // Otteniamo la mappa ID -> PlayerStats e la convertiamo in una Lista
        Map<Integer, PlayerStats> statsMap = aiEngine.getAllPlayerStats(csvDir);
        List<PlayerStats> allPlayers = new ArrayList<>(statsMap.values());

        if (allPlayers.isEmpty()) {
            request.setAttribute("error", "Nessun dato statistico trovato.");
            request.getRequestDispatcher("view/statistiche.jsp").forward(request, response);
            return;
        }

        // ---------------------------------------------------
        // LOGICA 1: TOP PLAYERS (Ordina per FantaMedia decrescente)
        // ---------------------------------------------------
        List<PlayerStats> topPlayers = allPlayers.stream()
                .sorted((p1, p2) -> Double.compare(p2.getFantaMedia(), p1.getFantaMedia())) // Ordine decrescente
                .limit(15) // Prendi solo i primi 15
                .collect(Collectors.toList());

        // ---------------------------------------------------
        // LOGICA 2: CLASSIFICA SQUADRE (Indice di Difficoltà)
        // ---------------------------------------------------
        Map<String, List<PlayerStats>> playersByTeam = allPlayers.stream()
                .collect(Collectors.groupingBy(PlayerStats::getSquadra));

        List<TeamStats> teamRanking = new ArrayList<>();

        for (Map.Entry<String, List<PlayerStats>> entry : playersByTeam.entrySet()) {
            String teamName = entry.getKey();
            List<PlayerStats> roster = entry.getValue();

            // Calcolo Indice: Media delle Fantamedie dei migliori 11 giocatori della squadra
            // Moltiplichiamo per 10 per avere un indice su base 100 (es. media 6.5 -> Indice 65)
            double avgFantaMedia = roster.stream()
                    .mapToDouble(PlayerStats::getFantaMedia)
                    .sorted()
                    .skip(Math.max(0, roster.size() - 14)) // Prendi i migliori 14 (titolari + riserve top)
                    .average()
                    .orElse(0.0);

            double difficultyIndex = avgFantaMedia * 10;

            // Normalizziamo visivamente (se supera 100 lo tronchiamo, ma nel calcio è raro)
            if (difficultyIndex > 100) difficultyIndex = 100;

            teamRanking.add(new TeamStats(teamName, difficultyIndex));
        }

        // Ordina le squadre per indice decrescente (più forte in alto)
        teamRanking.sort((t1, t2) -> Double.compare(t2.getIndiceDifficolta(), t1.getIndiceDifficolta()));

        // ---------------------------------------------------
        // INVIO ALLA JSP
        // ---------------------------------------------------
        request.setAttribute("topPlayers", topPlayers);
        request.setAttribute("teamStats", teamRanking);

        request.getRequestDispatcher("view/statistiche.jsp").forward(request, response);
    }
}