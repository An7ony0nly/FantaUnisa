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

@WebServlet("/Statistiche")
public class StatsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // 1. Inizializza AI Engine e Trova CSV
        FormationAI aiEngine = new FormationAI();
        String csvDirPath = getServletContext().getRealPath("/resources/csv/");
        File csvDir = new File(csvDirPath);

        // 2. Recupera Mappa Giocatori
        Map<Integer, PlayerStats> statsMap = aiEngine.getAllPlayerStats(csvDir);
        List<PlayerStats> allPlayers = new ArrayList<>(statsMap.values());


        if (allPlayers.isEmpty()) {
            request.setAttribute("error", "Nessun dato trovato. Verifica i file CSV.");
            request.getRequestDispatcher("view/statistiche.jsp").forward(request, response);
            return;
        }


        List<PlayerStats> topPlayers = allPlayers.stream()
                .sorted((p1, p2) -> Double.compare(p2.getFantaMedia(), p1.getFantaMedia())) // Decrescente
                .limit(20) // Mostra i primi 20
                .collect(Collectors.toList());


        // LOGICA 2: CLASSIFICA SQUADRE

        // Raggruppa i giocatori per Squadra
        Map<String, List<PlayerStats>> playersByTeam = allPlayers.stream()
                .collect(Collectors.groupingBy(PlayerStats::getSquadra));

        List<TeamStats> teamRanking = new ArrayList<>();

        for (Map.Entry<String, List<PlayerStats>> entry : playersByTeam.entrySet()) {
            String teamName = entry.getKey();
            List<PlayerStats> roster = entry.getValue();

            // A. Media Base

            double avgFantaMedia = roster.stream()
                    .mapToDouble(PlayerStats::getFantaMedia)
                    .sorted()
                    .skip(Math.max(0, roster.size() - 14)) // Salta i peggiori, tieni i migliori 14
                    .average()
                    .orElse(0.0);

            // B. Bonus Gol Fatti
            double totalGoals = roster.stream().mapToInt(PlayerStats::getGoalFatti).sum();

            // C. Malus Gol Subiti
            double totalConceded = roster.stream().mapToInt(PlayerStats::getGolSubiti).sum();

            // --- FORMULA DELL'INDICE DI DIFFICOLTÀ ---

            double difficultyIndex = (avgFantaMedia * 10) + (totalGoals * 0.2) - (totalConceded * 0.5);

            // Normalizzazione (minimo 0, massimo 100)
            if (difficultyIndex > 100) difficultyIndex = 100;
            if (difficultyIndex < 0) difficultyIndex = 0; // Evita numeri negativi per squadre disastrose

            // Aggiungi solo se il nome della squadra è valido
            if (teamName != null && !teamName.isEmpty() && !teamName.equalsIgnoreCase("null")) {
                teamRanking.add(new TeamStats(teamName, difficultyIndex));
            }
        }

        // Ordina decrescente (Chi ha l'indice più alto è più forte)
        teamRanking.sort((t1, t2) -> Double.compare(t2.getIndiceDifficolta(), t1.getIndiceDifficolta()));

        request.setAttribute("topPlayers", topPlayers);
        request.setAttribute("teamStats", teamRanking);

        request.getRequestDispatcher("view/statistiche.jsp").forward(request, response);
    }
}