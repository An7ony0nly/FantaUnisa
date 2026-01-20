package subsystems.access_profile.control;

import java.io.IOException;
import java.util.UUID;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import subsystems.access_profile.model.UserDAO;
import utils.EmailSender;

@WebServlet("/ForgotPasswordServlet")
public class ForgotPasswordServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        UserDAO userDAO = new UserDAO();

        if (userDAO.doRetrieveByEmail(email) != null) {

            String token = UUID.randomUUID().toString();

            userDAO.setResetToken(email, token);

            new Thread(() -> EmailSender.sendResetEmail(email, token)).start();
        }

        request.setAttribute("msg", "Se l'indirizzo esiste, riceverai una mail con le istruzioni.");
        request.getRequestDispatcher("view/login.jsp").forward(request, response);
    }
}