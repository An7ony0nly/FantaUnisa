package subsystems.access_profile.control;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import subsystems.access_profile.model.Role;
import subsystems.access_profile.model.User;
import subsystems.access_profile.model.UserDAO;
/*+*/
@WebServlet("/DeleteUserServlet")
public class DeleteUserServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (currentUser == null) {
            response.sendRedirect("view/login.jsp");
            return;
        }

        String emailToDelete = request.getParameter("email");

        // Se il parametro è null, significa che l'utente vuole cancellare se stesso
        if (emailToDelete == null || emailToDelete.isEmpty()) {
            emailToDelete = currentUser.getEmail();
        }

        UserDAO userDAO = new UserDAO();

        // CONTROLLO PERMESSI
        boolean isSelfDelete = currentUser.getEmail().equals(emailToDelete);
        boolean isAdmin = (currentUser.getRole() == Role.GESTORE_UTENTI);

        if (isSelfDelete || isAdmin) {

            userDAO.doDelete(emailToDelete);

            if (isSelfDelete) {
                // Caso 1: Mi sono cancellato -> Logout forzato
                session.invalidate();
                response.sendRedirect("view/login.jsp?msg=AccountDeleted");
            } else {
                // Caso 2: Admin ha cancellato qualcun altro -> Torna alla dashboard admin
                response.sendRedirect("admin/gestione_utenti.jsp?msg=UserDeleted");
            }
        } else {
            response.sendRedirect("view/home_utente.jsp?error=Unauthorized");
        }
    }
}