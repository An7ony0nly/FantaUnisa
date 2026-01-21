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

    private void executeDoGet() throws Exception {
        Method doGet = LogoutServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);
    }

    @Test
    void testTC1_LogoutSuccess() throws Exception {
        when(request.getSession(false)).thenReturn(session);

        executeDoGet();

        verify(session, times(1)).invalidate();
        verify(response).sendRedirect("view/index.jsp");
    }

    @Test
    void testTC2_LogoutNoSession() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        executeDoGet();

        verify(session, never()).invalidate();
        verify(response).sendRedirect("view/index.jsp");
    }
}