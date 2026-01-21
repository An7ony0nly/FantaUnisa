package subsystems.team_management.control;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;

import subsystems.access_profile.model.User;
import subsystems.team_management.model.Formation;
import subsystems.team_management.model.FormationDAO;
import subsystems.team_management.model.Player;
import subsystems.team_management.model.PlayerDAO;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class FormationServletTest {

    private FormationServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        servlet = new FormationServlet();

        User user = new User();
        user.setEmail("mister@test.it");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);

        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    private void executeDoPost() throws Exception {
        Method doPost = FormationServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    @Test
    void testTC14_1_InserimentoValido() throws Exception {
        try (MockedConstruction<PlayerDAO> mockedPlayerDAO = mockConstruction(PlayerDAO.class,
                (mock, context) -> when(mock.doRetrieveById(anyInt())).thenReturn(new Player()));
             MockedConstruction<FormationDAO> mockedFormationDAO = mockConstruction(FormationDAO.class,
                     (mock, context) -> when(mock.doSave(any(Formation.class))).thenReturn(100))) {

            when(request.getParameter("giornata")).thenReturn("1");
            when(request.getParameter("modulo")).thenReturn("3-4-3");

            String[] giocatori = new String[11];
            giocatori[0] = "1:P:titolare";
            for (int i = 1; i <= 3; i++) giocatori[i] = (i+1) + ":D:titolare";
            for (int i = 4; i <= 7; i++) giocatori[i] = (i+1) + ":C:titolare";
            for (int i = 8; i <= 10; i++) giocatori[i] = (i+1) + ":A:titolare";

            when(request.getParameterValues("giocatori")).thenReturn(giocatori);

            executeDoPost();

            FormationDAO dao = mockedFormationDAO.constructed().get(0);
            verify(dao).doSave(any(Formation.class));

            verify(request).getRequestDispatcher("/FormationServlet");
        }
    }

    @Test
    void testTC14_2_ErroreModulo() throws Exception {
        try (MockedConstruction<PlayerDAO> mockedPlayerDAO = mockConstruction(PlayerDAO.class,
                (mock, context) -> when(mock.doRetrieveById(anyInt())).thenReturn(new Player()));
             MockedConstruction<FormationDAO> mockedFormationDAO = mockConstruction(FormationDAO.class,
                     (mock, context) -> {
                         when(mock.doSave(any(Formation.class))).thenThrow(new RuntimeException("Errore Coerenza Modulo"));
                     })) {

            when(request.getParameter("giornata")).thenReturn("1");
            when(request.getParameter("modulo")).thenReturn("4-4-2");

            String[] giocatori = new String[11];
            giocatori[0] = "1:P:titolare";
            for (int i = 1; i <= 3; i++) giocatori[i] = (i+1) + ":D:titolare";
            for (int i = 4; i <= 8; i++) giocatori[i] = (i+1) + ":C:titolare";
            for (int i = 9; i <= 10; i++) giocatori[i] = (i+1) + ":A:titolare";

            when(request.getParameterValues("giocatori")).thenReturn(giocatori);

            executeDoPost();

            verify(response).sendRedirect(contains("error="));
        }
    }

    @Test
    void testTC14_3_IncoerenzaModulo() throws Exception {
        try (MockedConstruction<PlayerDAO> mockedPlayerDAO = mockConstruction(PlayerDAO.class);
             MockedConstruction<FormationDAO> mockedFormationDAO = mockConstruction(FormationDAO.class,
                     (mock, context) -> {
                         when(mock.doSave(any(Formation.class))).thenThrow(new RuntimeException("Troppi difensori"));
                     })) {

            when(request.getParameter("giornata")).thenReturn("1");
            when(request.getParameter("modulo")).thenReturn("3-5-2");

            String[] giocatori = new String[11];
            giocatori[0] = "1:P:titolare";
            for (int i = 1; i <= 4; i++) giocatori[i] = (i+1) + ":D:titolare";
            for (int i = 5; i <= 8; i++) giocatori[i] = (i+1) + ":C:titolare";
            for (int i = 9; i <= 10; i++) giocatori[i] = (i+1) + ":A:titolare";

            when(request.getParameterValues("giocatori")).thenReturn(giocatori);

            executeDoPost();

            verify(response).sendRedirect(contains("error="));
        }
    }

    @Test
    void testTC14_4_GiocatoreNonPosseduto() throws Exception {
        try (MockedConstruction<PlayerDAO> mockedPlayerDAO = mockConstruction(PlayerDAO.class);
             MockedConstruction<FormationDAO> mockedFormationDAO = mockConstruction(FormationDAO.class,
                     (mock, context) -> {
                         when(mock.doSave(any(Formation.class))).thenThrow(new RuntimeException("Giocatore non in rosa"));
                     })) {

            when(request.getParameter("giornata")).thenReturn("1");
            when(request.getParameter("modulo")).thenReturn("3-4-3");

            String[] giocatori = new String[11];
            for (int i = 0; i < 10; i++) giocatori[i] = (i+1) + ":D:titolare";
            giocatori[10] = "999:A:titolare";

            when(request.getParameterValues("giocatori")).thenReturn(giocatori);

            executeDoPost();

            verify(response).sendRedirect(contains("error="));
        }
    }

    @Test
    void testTC14_5_GiocatoreDuplicato() throws Exception {
        try (MockedConstruction<PlayerDAO> mockedPlayerDAO = mockConstruction(PlayerDAO.class);
             MockedConstruction<FormationDAO> mockedFormationDAO = mockConstruction(FormationDAO.class,
                     (mock, context) -> {
                         when(mock.doSave(any(Formation.class))).thenThrow(new RuntimeException("Giocatore duplicato"));
                     })) {

            when(request.getParameter("giornata")).thenReturn("1");
            when(request.getParameter("modulo")).thenReturn("3-4-3");

            String[] giocatori = new String[11];
            for (int i = 0; i < 9; i++) giocatori[i] = (i+1) + ":D:titolare";

            giocatori[9] = "10:A:titolare";
            giocatori[10] = "10:A:titolare";

            when(request.getParameterValues("giocatori")).thenReturn(giocatori);

            executeDoPost();

            verify(response).sendRedirect(contains("error="));
        }
    }
}