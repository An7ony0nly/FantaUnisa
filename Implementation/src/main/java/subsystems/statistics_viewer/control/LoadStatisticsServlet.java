package subsystems.statistics_viewer.control;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import subsystems.statistics_viewer.model.Statistiche;
import subsystems.statistics_import.model.StatisticheDAO;
import java.io.IOException;
import java.util.List;


@WebServlet("/LoadStatisticsServlet")
public class LoadStatisticsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String playerIdStr = request.getParameter("playerId");
        String fromStr = request.getParameter("fromGiornata");
        String toStr = request.getParameter("toGiornata");

        if (playerIdStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametro playerId mancante");
            return;
        }

        try {
            int playerId = Integer.parseInt(playerIdStr);

            Integer fromGiornata = (fromStr != null && !fromStr.isEmpty()) ? Integer.parseInt(fromStr) : null;
            Integer toGiornata = (toStr != null && !toStr.isEmpty()) ? Integer.parseInt(toStr) : null;

            StatisticheDAO dao = new StatisticheDAO();

            List<Statistiche> stats = dao.findByPlayerAndRange(playerId, fromGiornata, toGiornata);

            Statistiche lastStat = dao.findLastStatByPlayer(playerId);

            request.setAttribute("statisticheList", stats);

            request.setAttribute("lastStat", lastStat);

            request.setAttribute("selectedPlayerId", playerId);

            request.getRequestDispatcher("/WEB-INF/views/statistiche_view.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Formato parametri non valido");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore recupero statistiche");
        }
    }
}