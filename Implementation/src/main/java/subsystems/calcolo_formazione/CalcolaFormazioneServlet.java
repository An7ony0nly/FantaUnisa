package subsystems.calcolo_formazione;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import subsystems.access_profile.model.User;
import subsystems.module_selection.model.Module;
import subsystems.statistics_import.StatisticheDAO;
import subsystems.team_management.model.Player;
import subsystems.team_management.model.SquadDAO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@WebServlet("/calcola-formazione")
public class CalcolaFormazioneServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String giornataStr = request.getParameter("giornata");
        String moduloId = request.getParameter("modulo");

        if (giornataStr == null || moduloId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametri giornata o modulo mancanti");
            return;
        }

        int giornata;
        try {
            giornata = Integer.parseInt(giornataStr);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Giornata non valida");
            return;
        }

        Module module = Module.findById(moduloId);
        if (module == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Modulo non valido");
            return;
        }

        // Recupera la rosa dell'utente
        SquadDAO squadDAO = new SquadDAO();
        List<Player> rosa = squadDAO.doRetrieveByEmail(user.getEmail());

        // Costruisce PlayerWithStats per ogni giocatore usando le statistiche reali (finestra ultime 5 giornate)
        StatisticheDAO statsDAO = new StatisticheDAO();
        List<PlayerWithStats> rosaConStats = new ArrayList<>();

        int fromGiornata = Math.max(1, giornata - 4); // Ultime 5 giornate (inclusa quella corrente)

        for (Player p : rosa) {
            List<Statistiche> storico = statsDAO.findByPlayerAndRange(p.getId(), fromGiornata, giornata);
            Statistiche s = storico.isEmpty() ? null : storico.get(0); // record più recente
            rosaConStats.add(new PlayerWithStats(p, s));
        }

        CalcolaFormazione algoritmo = new CalcolaFormazione();
        AlgoritmoConfig config = AlgoritmoConfig.defaultConfig();

        List<Player> formazioneOrdinata = algoritmo.calcolaFormazione(rosaConStats, moduloId, giornata, config);

        request.setAttribute("suggestedFormation", formazioneOrdinata);
        request.setAttribute("modulo", moduloId);
        request.setAttribute("giornata", giornata);

        request.getRequestDispatcher("formazione_automatica.jsp").forward(request, response);
    }
}

