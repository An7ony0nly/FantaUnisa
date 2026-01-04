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
import subsystems.team_management.model.Player;
import subsystems.team_management.model.PlayerDAO;
import subsystems.team_management.model.Squad;
import subsystems.team_management.model.SquadDAO;

@WebServlet("/SquadServlet")
public class SquadServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        PlayerDAO playerDAO = new PlayerDAO();
        SquadDAO squadDAO = new SquadDAO();

        // 1. Recupera TUTTI i giocatori disponibili
        List<Player> allPlayers = playerDAO.doRetrieveByFilter(null, null);

        List<Player> currentList = squadDAO.doRetrieveByEmail(user.getEmail());
        Squad mySquad = new Squad(user.getEmail(), currentList);

        request.setAttribute("allPlayers", allPlayers);
        request.setAttribute("mySquad", mySquad);
        request.getRequestDispatcher("gestione_rosa.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // Recupera gli ID selezionati dalle checkbox
        String[] selectedIdsStr = request.getParameterValues("selectedPlayers");

        if (selectedIdsStr == null || selectedIdsStr.length != 25) {
            int count = (selectedIdsStr == null) ? 0 : selectedIdsStr.length;
            response.sendRedirect("SquadServlet?error=CountError&count=" + count);
            return;
        }

        // Conversione String[] -> List<Integer>
        List<Integer> playerIds = new ArrayList<>();
        try {
            for (String s : selectedIdsStr) {
                playerIds.add(Integer.parseInt(s));
            }
        } catch (NumberFormatException e) {
            response.sendRedirect("SquadServlet?error=InvalidData");
            return;
        }

        try {
            SquadDAO squadDAO = new SquadDAO();
            squadDAO.doUpdateSquad(user.getEmail(), playerIds);

            response.sendRedirect("SquadServlet?msg=SquadSaved");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("SquadServlet?error=DbError");
        }
    }
}