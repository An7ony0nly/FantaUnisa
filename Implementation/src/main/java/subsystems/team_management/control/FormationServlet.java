package subsystems.team_management.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import subsystems.access_profile.model.User;
import subsystems.team_management.model.*;
import subsystems.module_selection.model.Module;
@WebServlet("/FormationServlet")
public class FormationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) { response.sendRedirect("login.jsp"); return; }

        // 1. Recupera la Rosa dell'utente (per fargli scegliere i giocatori)
        SquadDAO squadDAO = new SquadDAO();
        Squad mySquad = squadDAO.doRetrieveSquadObject(user.getEmail()); // Usiamo il metodo "Wrapper" creato prima

        // 2. Recupera i moduli tattici disponibili
        List<Module> validModules = Module.getValidModules();

        // 3. Imposta attributi
        request.setAttribute("mySquad", mySquad);
        request.setAttribute("modules", validModules);

        // Passiamo la giornata corrente (hardcoded o da DB Settings)
        request.setAttribute("currentGiornata", 18); // Esempio: prossima giornata

        request.getRequestDispatcher("schiera_formazione.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) { response.sendRedirect("login.jsp"); return; }

        try {
            // 1. Parametri base
            int giornata = Integer.parseInt(request.getParameter("giornata"));
            String moduloId = request.getParameter("modulo");

            // 2. Recupera gli array di ID dal Form
            String[] titolariIds = request.getParameterValues("titolari");
            String[] panchinaIds = request.getParameterValues("panchina");

            // VALIDAZIONE BASE
            if (titolariIds == null || titolariIds.length != 11) {
                throw new IllegalArgumentException("Devi schierare esattamente 11 titolari.");
            }
            if (panchinaIds == null || panchinaIds.length > 7) { // Max 7 panchinari
                // opzionale warning
            }

            // 3. Verifica congruenza Modulo (Difensori/Centrocampisti/Attaccanti)
            Module module = Module.findById(moduloId);
            if (module == null) throw new IllegalArgumentException("Modulo non valido.");

            // Per validare i ruoli, dobbiamo recuperare i Player dal DB
            PlayerDAO playerDAO = new PlayerDAO();
            List<Player> selectedStarters = new ArrayList<>();
            for (String id : titolariIds) {
                selectedStarters.add(playerDAO.doRetrieveById(Integer.parseInt(id))); // Immagina esista doRetrieveById
            }

            validateTactics(selectedStarters, module);

            Formation formation = new Formation(user.getEmail(), giornata, moduloId);

            for (String id : titolariIds) {
                formation.addPlayer(Integer.parseInt(id), "T");
            }
            if (panchinaIds != null) {
                for (String id : panchinaIds) {
                    formation.addPlayer(Integer.parseInt(id), "P");
                }
            }

            FormationDAO formationDAO = new FormationDAO();
            int savedId = formationDAO.doSave(formation);

            request.setAttribute("formationId", savedId);
            request.getRequestDispatcher("formazione_saved.jsp").forward(request, response);

        } catch (IllegalArgumentException e) {
            response.sendRedirect("FormationServlet?error=" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("FormationServlet?error=SystemError");
        }
    }

    /**
     * Controlla se i giocatori scelti rispettano il conteggio del modulo.
     * Es. 3-4-3 -> Devono esserci 1 P, 3 D, 4 C, 3 A.
     */
    private void validateTactics(List<Player> players, Module module) {
        int p = 0, d = 0, c = 0, a = 0;

        for (Player pl : players) {
            switch (pl.getRuolo().toUpperCase()) {
                case "P": p++; break;
                case "D": d++; break;
                case "C": c++; break;
                case "A": a++; break;
            }
        }

        if (p != 1) throw new IllegalArgumentException("Devi schierare esattamente 1 portiere.");
        if (d != module.getDifensori()) throw new IllegalArgumentException("Il modulo richiede " + module.getDifensori() + " difensori.");
        if (c != module.getCentrocampisti()) throw new IllegalArgumentException("Il modulo richiede " + module.getCentrocampisti() + " centrocampisti.");
        if (a != module.getAttaccanti()) throw new IllegalArgumentException("Il modulo richiede " + module.getAttaccanti() + " attaccanti.");
    }
}