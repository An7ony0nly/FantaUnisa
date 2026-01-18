package subsystems.statistics_viewer;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import subsystems.calcolo_formazione.Statistiche;
import subsystems.statistics_import.StatisticheDAO;

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

        int playerId;
        try {
            playerId = Integer.parseInt(playerIdStr);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "playerId non valido");
            return;
        }

        Integer fromGiornata = null;
        Integer toGiornata = null;
        try {
            if (fromStr != null && !fromStr.isEmpty()) {
                fromGiornata = Integer.parseInt(fromStr);
            }
            if (toStr != null && !toStr.isEmpty()) {
                toGiornata = Integer.parseInt(toStr);
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Intervallo giornate non valido");
            return;
        }

        StatisticheDAO dao = new StatisticheDAO();
        List<Statistiche> stats = dao.findByPlayerAndRange(playerId, fromGiornata, toGiornata);

        response.setContentType("text/html;charset=UTF-8");
        var out = response.getWriter();

        out.println("<html><head><title>Statistiche giocatore</title></head><body>");
        out.println("<h1>Statistiche giocatore ID " + playerId + "</h1>");

        if (stats.isEmpty()) {
            out.println("<p>Nessuna statistica disponibile per i filtri selezionati.</p>");
        } else {
            out.println("<table border='1'>");
            out.println("<tr><th>Giornata</th><th>Partite voto</th><th>MV</th><th>FM</th>" +
                    "<th>Gol fatti</th><th>Gol subiti</th><th>Assist</th>" +
                    "<th>Ammonizioni</th><th>Espulsioni</th><th>Autogol</th></tr>");
            for (Statistiche s : stats) {
                out.println("<tr>");
                out.println("<td>" + s.getGiornata() + "</td>");
                out.println("<td>" + s.getPartiteVoto() + "</td>");
                out.println("<td>" + s.getMediaVoto() + "</td>");
                out.println("<td>" + s.getFantaMedia() + "</td>");
                out.println("<td>" + s.getGolFatti() + "</td>");
                out.println("<td>" + s.getGolSubiti() + "</td>");
                out.println("<td>" + s.getAssist() + "</td>");
                out.println("<td>" + s.getAmmonizioni() + "</td>");
                out.println("<td>" + s.getEspulsioni() + "</td>");
                out.println("<td>" + s.getAutogol() + "</td>");
                out.println("</tr>");
            }
            out.println("</table>");
        }

        out.println("</body></html>");
    }
}
