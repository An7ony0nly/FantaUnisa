package subsystems.access_profile.control;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import subsystems.access_profile.model.User;
import subsystems.access_profile.model.Role;
import subsystems.access_profile.model.UserDAO;
import utils.NavigationUtils;
import utils.PasswordHasher;

/**
 * Descrizione: Gestisce esclusivamente l'autenticazione dell'utente nel sistema.
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);


        // Utente già loggato
        if (session != null && session.getAttribute("user") != null) {
            Role role = (Role) session.getAttribute("role");
           NavigationUtils.redirectBasedOnRole(role, response);
            return;
        }

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        UserDAO userDAO = new UserDAO();

        String hashedPassword = PasswordHasher.hash(password);

        User user = userDAO.doRetrieveByEmailAndPassword(email, hashedPassword);

        if (user != null) {
            if (!user.is_Active()) {
                request.setAttribute("error", "Account non attivo. Controlla la mail.");
                request.getRequestDispatcher("view/login.jsp").forward(request, response);
                return;
            }
            session = request.getSession();
            session.setAttribute("user", user);
            session.setAttribute("role", user.getRole());

            NavigationUtils.redirectBasedOnRole(user.getRole(), response);
        } else {
            request.setAttribute("error", "Email o Password errati.");
            request.getRequestDispatcher("view/login.jsp").forward(request, response);
        }
    }
}
