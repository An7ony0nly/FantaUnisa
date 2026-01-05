package subsystems.access_profile.control;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import subsystems.access_profile.model.User;
import subsystems.access_profile.model.Role;
import subsystems.access_profile.model.UserDAO;
import utils.PasswordHasher;
import java.util.UUID;
import utils.EmailSender;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String nome = request.getParameter("nome");
        String cognome = request.getParameter("cognome");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        UserDAO userDAO = new UserDAO();

        if (userDAO.doRetrieveByEmail(email) != null) {
            request.setAttribute("error", "Email già registrata!");
            request.getRequestDispatcher("registrazione.jsp").forward(request, response);
            return;
        }

        try {
            User user = new User();
            user.setNome(nome);
            user.setCognome(cognome);
            user.setEmail(email);
            String hashedPassword = PasswordHasher.hash(password);
            user.setPassword(hashedPassword);
            user.setRole(Role.FANTALLENATORE); // Default
            String token = UUID.randomUUID().toString();
            user.setVerificationToken(token);
            user.setIs_active(false);

            userDAO.doSave(user);

            new Thread(() -> {
                EmailSender.sendVerificationEmail(email, token);
            }).start();

            response.sendRedirect("registrazione_successo.jsp");

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Errore durante la registrazione. Riprova.");
            request.getRequestDispatcher("registrazione.jsp").forward(request, response);
        }
    }
}