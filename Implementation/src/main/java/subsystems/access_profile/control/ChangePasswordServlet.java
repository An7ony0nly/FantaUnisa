package subsystems.access_profile.control;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import subsystems.access_profile.service.ChangePasswordService;
import subsystems.access_profile.model.User;
import utils.PasswordHasher;
/*+*/

@WebServlet("/ChangePasswordServlet")
public class ChangePasswordServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect("view/login.jsp");
            return;
        }

        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");

        if (oldPassword == null || newPassword == null ||
                oldPassword.trim().isEmpty() || newPassword.trim().isEmpty()) {
            redirectError(request, response, "Devi inserire entrambe le password.");
            return;
        }

        if (newPassword.length() < 6) {
            redirectError(request, response, "La nuova password deve essere di almeno 6 caratteri.");
            return;
        }

        String hashedOldInput = PasswordHasher.hash(oldPassword);

        if (!hashedOldInput.equals(user.getPassword())) {
            redirectError(request, response, "La vecchia password non è corretta.");
            return;
        }

        try {
            String hashedNewPassword = PasswordHasher.hash(newPassword);

            ChangePasswordService.getInstance().updatePassword(user, hashedNewPassword);

            response.sendRedirect("view/profilo.jsp?msg=PasswordChanged");

        } catch (Exception e) {
            e.printStackTrace();
            redirectError(request, response, "Errore di sistema.");
        }
    }

    private void redirectError(HttpServletRequest request, HttpServletResponse response, String errorMsg)
            throws ServletException, IOException {
        request.setAttribute("errorPassword", errorMsg);
        request.getRequestDispatcher("view/profilo.jsp").forward(request, response);
    }
}