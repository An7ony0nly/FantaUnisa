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
import subsystems.team_management.model.Player;
import subsystems.team_management.model.PlayerDAO;
import subsystems.team_management.model.Squad;
import subsystems.team_management.model.SquadDAO;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class SquadServletTest {

    private SquadServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        servlet = new SquadServlet();

        User user = new User();
        user.setEmail("mister@test.it");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);

        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    private void executeDoGet() throws Exception {
        Method doGet = SquadServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);
    }

    private void executeDoPost() throws Exception {
        Method doPost = SquadServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    @Test
    void testDoGet_ViewSquad() throws Exception {
        try (MockedConstruction<PlayerDAO> mockedPlayerDAO = mockConstruction(PlayerDAO.class,
                (mock, context) -> when(mock.doRetrieveByFilter(any(), any())).thenReturn(new ArrayList<>()));
             MockedConstruction<SquadDAO> mockedSquadDAO = mockConstruction(SquadDAO.class,
                     (mock, context) -> when(mock.doRetrieveByEmail(anyString())).thenReturn(new ArrayList<>()))) {

            executeDoGet();

            verify(request).setAttribute(eq("allPlayers"), anyList());
            verify(request).setAttribute(eq("mySquad"), any(Squad.class));
            verify(request).getRequestDispatcher("/view/rosa.jsp");
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testTC1_SquadSaved() throws Exception {
        try (MockedConstruction<SquadDAO> mockedSquadDAO = mockConstruction(SquadDAO.class)) {

            String[] ids = new String[25];
            for (int i = 0; i < 25; i++) { ids[i] = String.valueOf(i + 1); }

            when(request.getParameterValues("selectedPlayers")).thenReturn(ids);

            executeDoPost();

            SquadDAO dao = mockedSquadDAO.constructed().get(0);
            ArgumentCaptor<List<Integer>> listCaptor = ArgumentCaptor.forClass(List.class);
            verify(dao).doUpdateSquad(eq("mister@test.it"), listCaptor.capture());

            assertEquals(25, listCaptor.getValue().size());
            verify(response).sendRedirect("SquadServlet?msg=SquadSaved");
        }
    }

    @Test
    void testTC2_CountError() throws Exception {
        String[] ids = new String[20];
        for (int i = 0; i < 20; i++) { ids[i] = String.valueOf(i + 1); }

        when(request.getParameterValues("selectedPlayers")).thenReturn(ids);

        executeDoPost();

        verify(response).sendRedirect(contains("error=CountError"));
        verify(response).sendRedirect(contains("count=20"));
    }

    @Test
    void testInvalidData() throws Exception {
        String[] ids = new String[25];
        for (int i = 0; i < 24; i++) { ids[i] = "1"; }
        ids[24] = "invalid";

        when(request.getParameterValues("selectedPlayers")).thenReturn(ids);

        executeDoPost();

        verify(response).sendRedirect("SquadServlet?error=InvalidData");
    }

    @Test
    void testTC34_RuoliErrati() throws Exception {
        try (MockedConstruction<SquadDAO> mockedSquadDAO = mockConstruction(SquadDAO.class,
                (mock, context) -> {
                    doThrow(new RuntimeException("DB Connection Fail")).when(mock).doUpdateSquad(anyString(), anyList());
                })) {

            String[] ids = new String[25];
            for (int i = 0; i < 25; i++) { ids[i] = String.valueOf(i + 1); }
            when(request.getParameterValues("selectedPlayers")).thenReturn(ids);

            executeDoPost();

            verify(response).sendRedirect("SquadServlet?error=DbError");
        }
    }
}