package subsystems.access_profile.control;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import subsystems.access_profile.model.UserDAO;

@WebServlet("/ActivationServlet")
public class ActivationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token = request.getParameter("token");

        if (token == null || token.isEmpty()) {
            response.sendRedirect("view/login.jsp?error=InvalidToken");
            return;
        }

        UserDAO userDAO = new UserDAO();
        boolean activated = userDAO.doActivate(token);

        if (activated) {
            // Successo: Account attivo, ora può fare login
            response.sendRedirect("view/login.jsp?msg=AccountActivated");
        } else {
            // Fallimento: Token scaduto o errato
            response.sendRedirect("view/login.jsp?error=ActivationFailed");
        }
    }
}