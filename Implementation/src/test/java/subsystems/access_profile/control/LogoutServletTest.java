package subsystems.access_profile.control;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;

public class LogoutServletTest {

    private LogoutServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        servlet = new LogoutServlet();
    }

    // Helper per invocare il metodo protected doGet tramite Reflection
    private void executeDoGet() throws Exception {
        Method doGet = LogoutServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);
    }

    // --- TC1: Utente Loggato ---
    // Oracolo: Sessione invalidata, Redirect alla Home
    @Test
    void testTC1_LogoutSuccess() throws Exception {
        // 1. Simuliamo che esista una sessione attiva
        when(request.getSession(false)).thenReturn(session);

        // 2. Eseguiamo la servlet
        executeDoGet();

        // 3. Verifiche
        // Verifica che session.invalidate() sia stato chiamato esattamente una volta
        verify(session, times(1)).invalidate();

        // Verifica che venga fatto il redirect alla pagina corretta
        verify(response).sendRedirect("view/index.jsp");
    }

    // --- TC2: Utente Non Loggato (o sessione scaduta) ---
    // Oracolo: Nessuna invalidazione (sessione null), Redirect alla Home
    @Test
    void testTC2_LogoutNoSession() throws Exception {
        // 1. Simuliamo che NON esista una sessione (getSession ritorna null)
        when(request.getSession(false)).thenReturn(null);

        // 2. Eseguiamo la servlet
        executeDoGet();

        // 3. Verifiche
        // Verifica che invalidate() NON sia mai stato chiamato (perché session è null)
        verify(session, never()).invalidate();

        // Verifica che il redirect avvenga comunque (comportamento standard di sicurezza)
        verify(response).sendRedirect("view/index.jsp");
    }
}