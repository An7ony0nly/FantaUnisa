package subsystems.team_management.control;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import subsystems.access_profile.model.User;
import subsystems.statistics_import.model.StatisticheDAO;
import subsystems.team_management.model.*;
import subsystems.module_selection.model.Module;
import utils.RandomFormation;


@WebServlet("/FormationServlet")
public class FormationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) { response.sendRedirect("view/login.jsp"); return; }

        SquadDAO squadDAO = new SquadDAO();
        List<Player> mySquadList = squadDAO.doRetrieveByEmail(user.getEmail());

        List<Module> validModules = Module.getValidModules();

        request.setAttribute("mySquadList", mySquadList);
        request.setAttribute("modules", validModules);

        StatisticheDAO statsDAO = new StatisticheDAO();
        int lastLoaded = statsDAO.getLastGiornataCalcolata();
        int nextGiornata = lastLoaded + 1;

        request.setAttribute("currentGiornata", nextGiornata);

        request.getRequestDispatcher("view/schiera_formazione.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) { response.sendRedirect("view/login.jsp"); return; }

        try {
            String moduloId = request.getParameter("modulo");

            if (moduloId == null) throw new IllegalArgumentException("Devi selezionare un modulo.");

            StatisticheDAO statsDAO = new StatisticheDAO();
            int giornata = statsDAO.getLastGiornataCalcolata() + 1;

            Module module = Module.findById(moduloId);
            if (module == null) throw new IllegalArgumentException("Modulo non valido.");

            SquadDAO squadDAO = new SquadDAO();
            List<Player> rosaCompleta = squadDAO.doRetrieveByEmail(user.getEmail());

            Formation formation = new Formation(user.getEmail(), giornata, moduloId);

            RandomFormation.generateRandomLineup(formation, rosaCompleta, module);

            FormationDAO formationDAO = new FormationDAO();
            int savedId = formationDAO.doSave(formation);

            Map<String, List<Player>> dettagliFormazione = formationDAO.doRetrieveDetailById(savedId);

            request.setAttribute("message", "Formazione generata con successo per la giornata " + giornata + "!");
            request.setAttribute("titolari", dettagliFormazione.get("TITOLARI"));
            request.setAttribute("panchina", dettagliFormazione.get("PANCHINA"));
            request.setAttribute("formationId", savedId);
            request.setAttribute("moduloScelto", moduloId);
            request.getRequestDispatcher("view/formazione_saved.jsp").forward(request, response);

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            doGet(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il salvataggio della formazione.");
        }
    }
}