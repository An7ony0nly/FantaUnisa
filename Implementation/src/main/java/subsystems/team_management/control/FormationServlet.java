package subsystems.team_management.control;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import subsystems.access_profile.model.User;
import subsystems.team_management.model.*;
import subsystems.module_selection.model.Module;
import subsystems.ai.engine.FormationAI;
import subsystems.ai.model.PlayerStats;

@WebServlet("/FormationServlet")
public class FormationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) { response.sendRedirect("view/login.jsp"); return; }

        SquadDAO squadDAO = new SquadDAO();
        Squad mySquad = squadDAO.doRetrieveSquadObject(user.getEmail());
        List<Module> validModules = Module.getValidModules();

        // 1. Calcola Giornata
        int currentGiornata = 1;
        String calendarPath = getServletContext().getRealPath("/resources/csv/Calendario.csv");
        File calendarFile = new File(calendarPath);

        FormationAI aiEngine = new FormationAI();
        currentGiornata = aiEngine.findUpcomingGiornata(calendarFile);

        // 2. Carica le statistiche per TUTTI i giocatori (per la sidebar)
        String csvDirPath = getServletContext().getRealPath("/resources/csv/");
        File csvDir = new File(csvDirPath);
        Map<Integer, PlayerStats> statsMap = new HashMap<>();
        if (csvDir.exists()) {
            statsMap = aiEngine.getAllPlayerStats(csvDir);
        }
        request.setAttribute("statsMap", statsMap);

        // 3. Esegui AI se richiesto (per il suggerimento formazione)
        String aiParam = request.getParameter("ai");
        if ("true".equals(aiParam) && mySquad != null) {
            try {
                List<Integer> playerIds = new ArrayList<>();
                for (Player p : mySquad.getPlayers()) playerIds.add(p.getId());

                if (csvDir.exists()) {
                    Map<String, List<PlayerStats>> aiSuggestion = aiEngine.generateFormationWithMatchup(csvDir, calendarFile, playerIds);
                    if (aiSuggestion.isEmpty()) {
                        request.setAttribute("aiError", "Statistiche non trovate. Controlla i file CSV.");
                    } else {
                        request.setAttribute("aiSuggestion", aiSuggestion);
                        request.setAttribute("aiMessage", "Formazione calcolata per la Giornata " + currentGiornata + "!");
                    }
                }
            } catch (Exception e) { e.printStackTrace(); request.setAttribute("aiError", "Errore AI: " + e.getMessage()); }
        }

        request.setAttribute("mySquad", mySquad);
        request.setAttribute("modules", validModules);
        request.setAttribute("currentGiornata", currentGiornata);

        request.getRequestDispatcher("view/formazione.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) { response.sendRedirect("view/login.jsp"); return; }

        try {
            String giornataParam = request.getParameter("giornata");
            int giornata = Integer.parseInt(giornataParam);
            String moduloId = request.getParameter("modulo");
            String[] giocatoriRaw = request.getParameterValues("giocatori");

            if (giocatoriRaw == null || giocatoriRaw.length == 0) throw new IllegalArgumentException("Nessun giocatore selezionato.");

            Formation formation = new Formation(user.getEmail(), giornata, moduloId);
            List<Integer> titolariIds = new ArrayList<>();

            for (String rawData : giocatoriRaw) {
                String[] parts = rawData.split(":");
                if (parts.length == 3) {
                    int pId = Integer.parseInt(parts[0]);
                    String pRuolo = parts[1];
                    String pStatus = parts[2];
                    formation.addPlayer(pId, pRuolo + ":" + pStatus);
                    if ("titolare".equals(pStatus)) titolariIds.add(pId);
                }
            }

            if (titolariIds.size() != 11) throw new IllegalArgumentException("Devi schierare 11 titolari.");

            FormationDAO formationDAO = new FormationDAO();
            int savedId = formationDAO.doSave(formation);

            String postText = request.getParameter("testo");
            if (postText != null && !postText.trim().isEmpty()) {
                request.setAttribute("formationId", savedId);
                request.setAttribute("testo", postText);
                RequestDispatcher rd = request.getRequestDispatcher("/PostServlet");
                rd.forward(request, response);
                return;
            }

            request.setAttribute("formationId", savedId);
            request.setAttribute("currentGiornata", giornata);
            request.getRequestDispatcher("/FormationServlet").forward(request, response);

        } catch (IllegalArgumentException e) {
            response.sendRedirect("FormationServlet?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("FormationServlet?error=Errore+di+Sistema");
        }
    }
}