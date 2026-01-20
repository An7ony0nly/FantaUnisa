package subsystems.access_profile.control;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import subsystems.access_profile.model.User;
import subsystems.access_profile.model.UserDAO;

/**
 * Gestisce SOLO la modifica dei dati anagrafici (Nome, Cognome).
 */
@WebServlet("/UpdateProfileServlet")
public class UpdateProfileServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("view/login.jsp");
            return;
        }

        User currentUser = (User) session.getAttribute("user");

        String nuovoNome = request.getParameter("nome");
        String nuovoCognome = request.getParameter("cognome");

        boolean isChanged = false;

        if (nuovoNome != null && !nuovoNome.trim().isEmpty() && !nuovoNome.equals(currentUser.getNome())) {
            currentUser.setNome(nuovoNome);
            isChanged = true;
        }

        if (nuovoCognome != null && !nuovoCognome.trim().isEmpty() && !nuovoCognome.equals(currentUser.getCognome())) {
            currentUser.setCognome(nuovoCognome);
            isChanged = true;
        }

        if (isChanged) {
            try {
                UserDAO userDAO = new UserDAO();
                userDAO.doUpdateInfo(currentUser);

                session.setAttribute("user", currentUser);

                response.sendRedirect("view/profilo.jsp?msg=ProfileUpdated");
            } catch (Exception e) {
                e.printStackTrace();
                response.sendRedirect("view/profilo.jsp?error=UpdateFailed");
            }
        } else {
            response.sendRedirect("view/profilo.jsp");
        }
    }
}