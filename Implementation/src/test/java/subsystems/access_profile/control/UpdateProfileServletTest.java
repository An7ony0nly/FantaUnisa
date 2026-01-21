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
import org.mockito.MockitoAnnotations;

import subsystems.access_profile.model.User;
import subsystems.access_profile.model.UserDAO;

import java.lang.reflect.Method;

public class UpdateProfileServletTest {

    private UpdateProfileServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        servlet = new UpdateProfileServlet();

        // Setup base: la sessione esiste
        when(request.getSession(false)).thenReturn(session);
    }

    private void executeDoPost() throws Exception {
        Method doPost = UpdateProfileServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    // --- TC1: Aggiornamento Successo (Nome e Cognome cambiati) ---
    @Test
    void testTC1_UpdateSuccess() throws Exception {
        try (MockedConstruction<UserDAO> mockedDAO = mockConstruction(UserDAO.class)) {

            // 1. Utente corrente in sessione
            User user = new User();
            user.setNome("VecchioNome");
            user.setCognome("VecchioCognome");
            when(session.getAttribute("user")).thenReturn(user);

            // 2. Input: Nuovi dati
            when(request.getParameter("nome")).thenReturn("Mario");
            when(request.getParameter("cognome")).thenReturn("Rossi");

            // 3. Esecuzione
            executeDoPost();

            // 4. Verifiche
            // Verifica che l'oggetto User sia stato modificato
            assertEquals("Mario", user.getNome());
            assertEquals("Rossi", user.getCognome());

            // Verifica che il DAO abbia salvato le modifiche
            // Qui ci aspettiamo che il DAO venga creato, quindi .get(0) è sicuro
            UserDAO dao = mockedDAO.constructed().get(0);
            verify(dao).doUpdateInfo(user);

            // Verifica aggiornamento sessione e redirect
            verify(session).setAttribute("user", user);
            verify(response).sendRedirect(contains("ProfileUpdated"));
        }
    }

    // --- TC2: Nessuna Modifica (Input vuoti o uguali) ---
    @Test
    void testTC2_NoChanges() throws Exception {
        try (MockedConstruction<UserDAO> mockedDAO = mockConstruction(UserDAO.class)) {

            User user = new User();
            user.setNome("Mario");
            user.setCognome("Rossi");
            when(session.getAttribute("user")).thenReturn(user);

            // Input: Stessi dati di prima (o null)
            when(request.getParameter("nome")).thenReturn("Mario");
            when(request.getParameter("cognome")).thenReturn(""); // Vuoto -> ignorato

            executeDoPost();

            // --- CORREZIONE QUI ---
            // In questo caso il DAO NON viene mai istanziato ("new UserDAO()" non viene chiamato).
            // Quindi verifichiamo che la lista dei mock costruiti sia VUOTA.
            assertTrue(mockedDAO.constructed().isEmpty(), "Il DAO non dovrebbe essere creato se non ci sono modifiche.");

            // Redirect normale senza msg
            verify(response).sendRedirect("view/profilo.jsp");
        }
    }

    // --- TC3: Errore Database ---
    @Test
    void testTC3_DatabaseError() throws Exception {
        try (MockedConstruction<UserDAO> mockedDAO = mockConstruction(UserDAO.class,
                (mock, context) -> {
                    // Simuliamo un errore durante l'update
                    doThrow(new RuntimeException("DB Error")).when(mock).doUpdateInfo(any());
                })) {

            User user = new User();
            user.setNome("Mario");
            when(session.getAttribute("user")).thenReturn(user);

            // Input valido per scatenare l'update (così entra nell'if e crea il DAO)
            when(request.getParameter("nome")).thenReturn("Luigi");

            executeDoPost();

            verify(response).sendRedirect(contains("error=UpdateFailed"));
        }
    }

    // --- TC4: Utente Non Loggato ---
    @Test
    void testTC4_NotLogged() throws Exception {
        // Sessione null o user null
        when(request.getSession(false)).thenReturn(null);

        executeDoPost();

        verify(response).sendRedirect("view/login.jsp");
    }
}