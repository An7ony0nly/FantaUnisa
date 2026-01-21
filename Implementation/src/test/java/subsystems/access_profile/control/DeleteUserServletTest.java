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


    private void executeDoPost() throws Exception {
        Method doPost = DeleteUserServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    //TC1: Utente loggato conferma eliminazione
    @Test
    void testTC1_ConfermaEliminazione() throws Exception {
        try (MockedConstruction<UserDAO> mockedDAO = mockConstruction(UserDAO.class)) {


            User me = new User();
            me.setEmail("me@test.it");
            me.setRole(Role.FANTALLENATORE);

            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("user")).thenReturn(me);


            when(request.getParameter("email")).thenReturn(null);


            executeDoPost();


            UserDAO dao = mockedDAO.constructed().get(0);
            verify(dao).doDelete("me@test.it");

            verify(session).invalidate();
            verify(response).sendRedirect("view/login.jsp?msg=AccountDeleted");
        }
    }

    //TC3: Utente sessione scaduta clicca elimina
    @Test
    void testTC3_SessioneScaduta() throws Exception {

        when(request.getSession(false)).thenReturn(null);


        executeDoPost();


        verify(response).sendRedirect("view/login.jsp");

    }


    @Test
    void testAdminDeleteUser() throws Exception {
        try (MockedConstruction<UserDAO> mockedDAO = mockConstruction(UserDAO.class)) {


            User admin = new User();
            admin.setEmail("admin@test.it");
            admin.setRole(Role.GESTORE_UTENTI);

            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("user")).thenReturn(admin);


            String targetEmail = "target@test.it";
            when(request.getParameter("email")).thenReturn(targetEmail);

            executeDoPost();

            UserDAO dao = mockedDAO.constructed().get(0);
            verify(dao).doDelete(targetEmail);


            verify(session, never()).invalidate();
            verify(response).sendRedirect(contains("admin/gestione_utenti.jsp"));
        }
    }


    @Test
    void testUnauthorizedDelete() throws Exception {
        try (MockedConstruction<UserDAO> mockedDAO = mockConstruction(UserDAO.class)) {


            User hacker = new User();
            hacker.setEmail("hacker@test.it");
            hacker.setRole(Role.FANTALLENATORE);

            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("user")).thenReturn(hacker);


            when(request.getParameter("email")).thenReturn("victim@test.it");

            executeDoPost();


            UserDAO dao = mockedDAO.constructed().get(0);
            verify(dao, never()).doDelete(anyString());

            verify(response).sendRedirect(contains("error=Unauthorized"));
        }
    }
}