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

        when(request.getSession(false)).thenReturn(session);
    }

    private void executeDoPost() throws Exception {
        Method doPost = UpdateProfileServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    @Test
    void testTC1_UpdateSuccess() throws Exception {
        try (MockedConstruction<UserDAO> mockedDAO = mockConstruction(UserDAO.class)) {

            User user = new User();
            user.setNome("VecchioNome");
            user.setCognome("VecchioCognome");
            when(session.getAttribute("user")).thenReturn(user);

            when(request.getParameter("nome")).thenReturn("Mario");
            when(request.getParameter("cognome")).thenReturn("Rossi");

            executeDoPost();

            assertEquals("Mario", user.getNome());
            assertEquals("Rossi", user.getCognome());

            UserDAO dao = mockedDAO.constructed().get(0);
            verify(dao).doUpdateInfo(user);

            verify(session).setAttribute("user", user);
            verify(response).sendRedirect(contains("ProfileUpdated"));
        }
    }

    @Test
    void testTC2_NoChanges() throws Exception {
        try (MockedConstruction<UserDAO> mockedDAO = mockConstruction(UserDAO.class)) {

            User user = new User();
            user.setNome("Mario");
            user.setCognome("Rossi");
            when(session.getAttribute("user")).thenReturn(user);

            when(request.getParameter("nome")).thenReturn("Mario");
            when(request.getParameter("cognome")).thenReturn("");

            executeDoPost();

            assertTrue(mockedDAO.constructed().isEmpty(), "Il DAO non dovrebbe essere creato se non ci sono modifiche.");

            verify(response).sendRedirect("view/profilo.jsp");
        }
    }

    @Test
    void testTC3_DatabaseError() throws Exception {
        try (MockedConstruction<UserDAO> mockedDAO = mockConstruction(UserDAO.class,
                (mock, context) -> {
                    doThrow(new RuntimeException("DB Error")).when(mock).doUpdateInfo(any());
                })) {

            User user = new User();
            user.setNome("Mario");
            when(session.getAttribute("user")).thenReturn(user);

            when(request.getParameter("nome")).thenReturn("Luigi");

            executeDoPost();

            verify(response).sendRedirect(contains("error=UpdateFailed"));
        }
    }

    @Test
    void testTC4_NotLogged() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        executeDoPost();

        verify(response).sendRedirect("view/login.jsp");
    }
}