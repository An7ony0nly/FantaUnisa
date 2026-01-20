package subsystems.access_profile.control;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import subsystems.access_profile.model.User;
import subsystems.access_profile.model.UserDAO;
import utils.PasswordHasher;
/*+*/
@WebServlet("/ResetPasswordServlet")
public class ResetPasswordServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token = request.getParameter("token");
        UserDAO userDAO = new UserDAO();
        User user = userDAO.findByResetToken(token);

        if (user != null) {
            // Token valido: Mostra la pagina di reset
            request.setAttribute("token", token); // Passiamo il token al form
            request.getRequestDispatcher("reset_password.jsp").forward(request, response);
        } else {
            // Token scaduto o errato
            response.sendRedirect("login.jsp?error=TokenExpired");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token = request.getParameter("token");
        String newPassword = request.getParameter("password");

        String hashedNewPassword = PasswordHasher.hash(newPassword);

        UserDAO userDAO = new UserDAO();
        User user = userDAO.findByResetToken(token);

        if (user != null) {
            userDAO.updatePassword(user.getEmail(), hashedNewPassword);
            response.sendRedirect("login.jsp?msg=PasswordUpdated");
        } else {
            response.sendRedirect("login.jsp?error=ErrorUpdating");
        }
    }
}