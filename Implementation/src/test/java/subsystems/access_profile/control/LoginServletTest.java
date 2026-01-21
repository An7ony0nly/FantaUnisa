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
import subsystems.access_profile.model.Role;
import utils.NavigationUtils;
import utils.PasswordHasher;

import java.lang.reflect.Method;

public class LoginServletTest {

    private LoginServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new LoginServlet();

        // Setup base request/session
        when(request.getSession()).thenReturn(session);
        when(request.getSession(false)).thenReturn(session); // Simula sessione esistente
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    private void executeDoPost() throws Exception {
        // Usa Reflection per chiamare il metodo protected doPost
        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    // --- TC1: Login OK ---
    @Test
    void testTC1_LoginOK() throws Exception {
        // 1. Mockiamo la creazione del DAO (perché la servlet fa 'new UserDAO()' locale)
        try (MockedConstruction<UserDAO> mockedDAO = mockConstruction(UserDAO.class,
                (mock, context) -> {
                    // Prepariamo un utente valido
                    User u = new User();
                    u.setEmail("mario@test.it");
                    u.setPassword("HASHED_PWD");
                    u.setRole(Role.FANTALLENATORE);
                    u.setIs_active(true); // Utente ATTIVO

                    // Quando il DAO cerca, trova l'utente
                    when(mock.doRetrieveByEmailAndPassword(anyString(), anyString())).thenReturn(u);
                });
             // 2. Mockiamo le classi statiche (Hasher e Navigation)
             MockedStatic<PasswordHasher> hasherMock = mockStatic(PasswordHasher.class);
             MockedStatic<NavigationUtils> navMock = mockStatic(NavigationUtils.class)) {

            // Configurazione Input
            when(request.getParameter("email")).thenReturn("mario@test.it");
            when(request.getParameter("password")).thenReturn("Password1!");

            // Mock Hash
            hasherMock.when(() -> PasswordHasher.hash("Password1!")).thenReturn("HASHED_PWD");

            // Esecuzione
            executeDoPost();

            // VERIFICHE
            // 1. L'utente è in sessione?
            verify(session).setAttribute(eq("user"), any(User.class));
            verify(session).setAttribute(eq("role"), eq(Role.FANTALLENATORE));

            // 2. Redirect chiamato correttamente (con 3 argomenti come nella tua servlet!)
            navMock.verify(() -> NavigationUtils.redirectBasedOnRole(
                    eq(Role.FANTALLENATORE),
                    eq(request),
                    eq(response)
            ));
        }
    }

    // --- TC2: Utente Non Trovato / Password Errata ---
    @Test
    void testTC23_CredenzialiErrate() throws Exception {
        try (MockedConstruction<UserDAO> mockedDAO = mockConstruction(UserDAO.class,
                (mock, context) -> {
                    // Il DAO restituisce NULL (utente non trovato o pwd errata)
                    when(mock.doRetrieveByEmailAndPassword(anyString(), anyString())).thenReturn(null);
                });
             MockedStatic<PasswordHasher> hasherMock = mockStatic(PasswordHasher.class)) {

            when(request.getParameter("email")).thenReturn("ignoto@test.it");
            when(request.getParameter("password")).thenReturn("errata");

            hasherMock.when(() -> PasswordHasher.hash(anyString())).thenReturn("HASH_QUALSIASI");

            executeDoPost();

            // Verifiche
            verify(session, never()).setAttribute(eq("user"), any()); // Niente sessione
            verify(request).setAttribute(eq("error"), contains("Email o Password errati")); // Messaggio errore
            verify(dispatcher).forward(request, response); // Forward a login.jsp
        }
    }

    // --- TC4: Account Non Attivo ---
    @Test
    void testTC4_NonAttivo() throws Exception {
        try (MockedConstruction<UserDAO> mockedDAO = mockConstruction(UserDAO.class,
                (mock, context) -> {
                    // Utente trovato ma NON attivo
                    User u = new User();
                    u.setEmail("mario@test.it");
                    u.setIs_active(false); // <--- INATTIVO

                    when(mock.doRetrieveByEmailAndPassword(anyString(), anyString())).thenReturn(u);
                });
             MockedStatic<PasswordHasher> hasherMock = mockStatic(PasswordHasher.class)) {

            when(request.getParameter("email")).thenReturn("mario@test.it");
            when(request.getParameter("password")).thenReturn("Password1!");

            hasherMock.when(() -> PasswordHasher.hash(anyString())).thenReturn("HASH");

            executeDoPost();

            // Verifiche
            verify(session, never()).setAttribute(eq("user"), any()); // Niente login
            verify(request).setAttribute(eq("error"), contains("Account non attivo")); // Errore specifico
            verify(dispatcher).forward(request, response);
        }
    }
}