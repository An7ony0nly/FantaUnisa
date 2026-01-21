package subsystems.access_profile.control;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import subsystems.access_profile.model.User;
import subsystems.access_profile.model.UserDAO;
import utils.PasswordHasher;

import java.lang.reflect.Method;

public class ResetPasswordServletTest {

    private ResetPasswordServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        servlet = new ResetPasswordServlet();
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    // Metodi helper per invocare doGet e doPost
    private void executeDoGet() throws Exception {
        Method doGet = ResetPasswordServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);
    }

    private void executeDoPost() throws Exception {
        Method doPost = ResetPasswordServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    // --- TEST doGet (Visualizzazione Pagina Reset) ---

    @Test
    void testDoGet_TokenValido() throws Exception {
        try (MockedConstruction<UserDAO> mockedDAO = mockConstruction(UserDAO.class,
                (mock, context) -> {
                    // Simula token valido (utente trovato)
                    when(mock.findByResetToken("valid-token")).thenReturn(new User());
                })) {

            when(request.getParameter("token")).thenReturn("valid-token");

            executeDoGet();

            // Verifica che il token venga passato alla JSP
            verify(request).setAttribute("token", "valid-token");
            verify(request).getRequestDispatcher("view/reset_password.jsp");
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoGet_TokenScadutoOErrato() throws Exception {
        try (MockedConstruction<UserDAO> mockedDAO = mockConstruction(UserDAO.class,
                (mock, context) -> {
                    // Simula token non trovato
                    when(mock.findByResetToken("invalid-token")).thenReturn(null);
                })) {

            when(request.getParameter("token")).thenReturn("invalid-token");

            executeDoGet();

            // Verifica redirect errore
            verify(response).sendRedirect("view/login.jsp?error=TokenExpired");
        }
    }

    // --- TEST doPost (Aggiornamento Password) ---

    @Test
    void testDoPost_Success() throws Exception {
        try (MockedConstruction<UserDAO> mockedDAO = mockConstruction(UserDAO.class,
                (mock, context) -> {
                    User user = new User();
                    user.setEmail("user@test.it");
                    // Simula che il token corrisponda a questo utente
                    when(mock.findByResetToken("valid-token")).thenReturn(user);
                });
             MockedStatic<PasswordHasher> hasherMock = mockStatic(PasswordHasher.class)) {

            // Mock dell'hashing statico
            hasherMock.when(() -> PasswordHasher.hash("NewPass1!")).thenReturn("HASHED_PASS");

            // Input
            when(request.getParameter("token")).thenReturn("valid-token");
            when(request.getParameter("password")).thenReturn("NewPass1!");

            executeDoPost();

            // Verifiche
            UserDAO dao = mockedDAO.constructed().get(0);

            // 1. Verifica che venga cercato l'utente col token
            verify(dao).findByResetToken("valid-token");

            // 2. Verifica che la password venga aggiornata (con l'hash)
            verify(dao).updatePassword("user@test.it", "HASHED_PASS");

            // 3. Verifica redirect successo
            verify(response).sendRedirect("login.jsp?msg=PasswordUpdated");
        }
    }

    @Test
    void testDoPost_ErrorUpdating_InvalidToken() throws Exception {
        try (MockedConstruction<UserDAO> mockedDAO = mockConstruction(UserDAO.class,
                (mock, context) -> {
                    // Simula token non trovato al momento del submit
                    when(mock.findByResetToken("invalid-token")).thenReturn(null);
                });
             MockedStatic<PasswordHasher> hasherMock = mockStatic(PasswordHasher.class)) {

            when(request.getParameter("token")).thenReturn("invalid-token");
            when(request.getParameter("password")).thenReturn("Pass");

            executeDoPost();

            // Verifica che NON venga chiamato l'update
            if (!mockedDAO.constructed().isEmpty()) {
                verify(mockedDAO.constructed().get(0), never()).updatePassword(anyString(), anyString());
            }

            // Verifica redirect errore
            verify(response).sendRedirect("login.jsp?error=ErrorUpdating");
        }
    }
}