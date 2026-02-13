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

        // 2. Recupera Mappa Giocatori (ID -> Stats)
        Map<Integer, PlayerStats> statsMap = aiEngine.getAllPlayerStats(csvDir);
        List<PlayerStats> allPlayers = new ArrayList<>(statsMap.values());

        // Controllo Sicurezza: Se non ci sono dati, non crashare
        if (allPlayers.isEmpty()) {
            request.setAttribute("error", "Nessun dato trovato. Verifica i file CSV.");
            request.getRequestDispatcher("view/statistiche.jsp").forward(request, response);
            return;
        }

        // =================================================================
        // LOGICA 1: TOP 20 GIOCATORI (Ordinati per FantaMedia)
        // =================================================================
        List<PlayerStats> topPlayers = allPlayers.stream()
                .sorted((p1, p2) -> Double.compare(p2.getFantaMedia(), p1.getFantaMedia())) // Decrescente
                .limit(20) // Mostra i primi 20
                .collect(Collectors.toList());

        // =================================================================
        // LOGICA 2: CLASSIFICA SQUADRE (Con Bonus Attacco e Malus Difesa)
        // =================================================================

        // Raggruppa i giocatori per Squadra
        Map<String, List<PlayerStats>> playersByTeam = allPlayers.stream()
                .collect(Collectors.groupingBy(PlayerStats::getSquadra));

        List<TeamStats> teamRanking = new ArrayList<>();

        for (Map.Entry<String, List<PlayerStats>> entry : playersByTeam.entrySet()) {
            String teamName = entry.getKey();
            List<PlayerStats> roster = entry.getValue();

            // A. Media Base (Top 14 giocatori)
            // Prendiamo solo i giocatori che "giocano" davvero per valutare la forza della squadra
            double avgFantaMedia = roster.stream()
                    .mapToDouble(PlayerStats::getFantaMedia)
                    .sorted()
                    .skip(Math.max(0, roster.size() - 14)) // Salta i peggiori, tieni i migliori 14
                    .average()
                    .orElse(0.0);

            // B. Bonus Gol Fatti (Attacco) - Somma GF di tutta la rosa
            double totalGoals = roster.stream().mapToInt(PlayerStats::getGoalFatti).sum();

            // C. Malus Gol Subiti (Difesa - Gs) - Somma GS di tutta la rosa
            double totalConceded = roster.stream().mapToInt(PlayerStats::getGolSubiti).sum();

            // --- FORMULA DELL'INDICE DI DIFFICOLTÀ ---
            // Base: Media * 10 (es. 6.5 -> 65)
            // Bonus: +0.2 per ogni gol fatto
            // Malus: -0.5 per ogni gol subito (pesa di più per penalizzare le difese deboli)
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

        // =================================================================
        // INVIO DATI ALLA JSP
        // =================================================================
        request.setAttribute("topPlayers", topPlayers);
        request.setAttribute("teamStats", teamRanking);

        request.getRequestDispatcher("view/statistiche.jsp").forward(request, response);
    }
}