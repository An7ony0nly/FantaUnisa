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

        when(request.getSession()).thenReturn(session);
        when(request.getSession(false)).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    private void executeDoPost() throws Exception {
        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    @Test
    void testTC1_LoginOK() throws Exception {
        try (MockedConstruction<UserDAO> mockedDAO = mockConstruction(UserDAO.class,
                (mock, context) -> {
                    User u = new User();
                    u.setEmail("mario@test.it");
                    u.setPassword("HASHED_PWD");
                    u.setRole(Role.FANTALLENATORE);
                    u.setIs_active(true);

                    when(mock.doRetrieveByEmailAndPassword(anyString(), anyString())).thenReturn(u);
                });
             MockedStatic<PasswordHasher> hasherMock = mockStatic(PasswordHasher.class);
             MockedStatic<NavigationUtils> navMock = mockStatic(NavigationUtils.class)) {

            when(request.getParameter("email")).thenReturn("mario@test.it");
            when(request.getParameter("password")).thenReturn("Password1!");

            hasherMock.when(() -> PasswordHasher.hash("Password1!")).thenReturn("HASHED_PWD");

            executeDoPost();

            verify(session).setAttribute(eq("user"), any(User.class));
            verify(session).setAttribute(eq("role"), eq(Role.FANTALLENATORE));

            navMock.verify(() -> NavigationUtils.redirectBasedOnRole(
                    eq(Role.FANTALLENATORE),
                    eq(request),
                    eq(response)
            ));
        }
    }

    @Test
    void testTC23_CredenzialiErrate() throws Exception {
        try (MockedConstruction<UserDAO> mockedDAO = mockConstruction(UserDAO.class,
                (mock, context) -> {
                    when(mock.doRetrieveByEmailAndPassword(anyString(), anyString())).thenReturn(null);
                });
             MockedStatic<PasswordHasher> hasherMock = mockStatic(PasswordHasher.class)) {

            when(request.getParameter("email")).thenReturn("ignoto@test.it");
            when(request.getParameter("password")).thenReturn("errata");

            hasherMock.when(() -> PasswordHasher.hash(anyString())).thenReturn("HASH_QUALSIASI");

            executeDoPost();

            verify(session, never()).setAttribute(eq("user"), any());
            verify(request).setAttribute(eq("error"), contains("Email o Password errati"));
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testTC4_NonAttivo() throws Exception {
        try (MockedConstruction<UserDAO> mockedDAO = mockConstruction(UserDAO.class,
                (mock, context) -> {
                    User u = new User();
                    u.setEmail("mario@test.it");
                    u.setIs_active(false);

                    when(mock.doRetrieveByEmailAndPassword(anyString(), anyString())).thenReturn(u);
                });
             MockedStatic<PasswordHasher> hasherMock = mockStatic(PasswordHasher.class)) {

            when(request.getParameter("email")).thenReturn("mario@test.it");
            when(request.getParameter("password")).thenReturn("Password1!");

            hasherMock.when(() -> PasswordHasher.hash(anyString())).thenReturn("HASH");

            executeDoPost();

            verify(session, never()).setAttribute(eq("user"), any());
            verify(request).setAttribute(eq("error"), contains("Account non attivo"));
            verify(dispatcher).forward(request, response);
        }
    }
}