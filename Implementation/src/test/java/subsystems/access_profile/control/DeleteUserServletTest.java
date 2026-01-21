package subsystems.access_profile.control;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;

import subsystems.access_profile.model.User;
import subsystems.access_profile.model.UserDAO;
import subsystems.access_profile.model.Role;

import java.lang.reflect.Method;

public class DeleteUserServletTest {

    private DeleteUserServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        servlet = new DeleteUserServlet();
    }

    // Helper per invocare doPost (metodo protected) tramite Reflection
    private void executeDoPost() throws Exception {
        Method doPost = DeleteUserServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    // --- TC1: Utente loggato conferma eliminazione (Self Delete) ---
    // Oracolo: Account eliminato, Logout forzato, Redirect
    @Test
    void testTC1_ConfermaEliminazione() throws Exception {
        try (MockedConstruction<UserDAO> mockedDAO = mockConstruction(UserDAO.class)) {

            // 1. Setup: Utente Loggato che vuole cancellarsi
            User me = new User();
            me.setEmail("me@test.it");
            me.setRole(Role.FANTALLENATORE);

            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("user")).thenReturn(me);

            // 2. Input: parametro email null (nella tua servlet significa "cancella me stesso")
            when(request.getParameter("email")).thenReturn(null);

            // 3. Esecuzione
            executeDoPost();

            // 4. Verifiche
            UserDAO dao = mockedDAO.constructed().get(0);
            verify(dao).doDelete("me@test.it"); // Verifica delete su DB [cite: 223]

            verify(session).invalidate(); // Verifica Logout [cite: 223]
            verify(response).sendRedirect("view/login.jsp?msg=AccountDeleted");
        }
    }

    // --- TC3: Utente sessione scaduta clicca elimina ---
    // Oracolo: Redirect Login
    @Test
    void testTC3_SessioneScaduta() throws Exception {
        // 1. Setup: Nessuna sessione (utente non loggato o scaduto)
        when(request.getSession(false)).thenReturn(null);

        // 2. Esecuzione
        executeDoPost();

        // 3. Verifica Redirect al Login [cite: 224]
        verify(response).sendRedirect("view/login.jsp");
        // Nota: non serve controllare il DAO perché la servlet esce prima
    }

    // --- TEST AGGIUNTIVI (Funzionalità Admin e Sicurezza) ---

    // Test Funzionale: Admin cancella un altro utente
    @Test
    void testAdminDeleteUser() throws Exception {
        try (MockedConstruction<UserDAO> mockedDAO = mockConstruction(UserDAO.class)) {

            // Setup Admin
            User admin = new User();
            admin.setEmail("admin@test.it");
            admin.setRole(Role.GESTORE_UTENTI); // Admin

            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("user")).thenReturn(admin);

            // Input: Admin vuole cancellare "target"
            String targetEmail = "target@test.it";
            when(request.getParameter("email")).thenReturn(targetEmail);

            executeDoPost();

            UserDAO dao = mockedDAO.constructed().get(0);
            verify(dao).doDelete(targetEmail);

            // L'admin NON deve essere sloggato
            verify(session, never()).invalidate();
            verify(response).sendRedirect(contains("admin/gestione_utenti.jsp"));
        }
    }

    // Test Sicurezza: Utente prova a cancellare un altro (Non Autorizzato)
    @Test
    void testUnauthorizedDelete() throws Exception {
        try (MockedConstruction<UserDAO> mockedDAO = mockConstruction(UserDAO.class)) {

            // Setup Utente Normale (Hacker)
            User hacker = new User();
            hacker.setEmail("hacker@test.it");
            hacker.setRole(Role.FANTALLENATORE);

            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("user")).thenReturn(hacker);

            // Input: Hacker prova a cancellare "victim"
            when(request.getParameter("email")).thenReturn("victim@test.it");

            executeDoPost();

            // Verifica: Il DAO NON deve essere chiamato per la cancellazione
            UserDAO dao = mockedDAO.constructed().get(0);
            verify(dao, never()).doDelete(anyString());

            verify(response).sendRedirect(contains("error=Unauthorized"));
        }
    }
}