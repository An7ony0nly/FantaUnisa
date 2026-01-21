package subsystems.statistics_viewer.control;

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

import subsystems.statistics_import.model.StatisticheDAO;
import subsystems.statistics_viewer.model.Statistiche;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class LoadStatisticsServletTest {

    private LoadStatisticsServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        servlet = new LoadStatisticsServlet();

        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    private void executeDoGet() throws Exception {
        Method doGet = LoadStatisticsServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);
    }

    @Test
    void testTC1_LoadSuccess() throws Exception {
        try (MockedConstruction<StatisticheDAO> mockedDAO = mockConstruction(StatisticheDAO.class,
                (mock, context) -> {
                    List<Statistiche> stats = new ArrayList<>();
                    stats.add(new Statistiche());

                    when(mock.findByPlayerAndRange(eq(10), any(), any())).thenReturn(stats);
                    when(mock.findLastStatByPlayer(10)).thenReturn(new Statistiche());
                })) {

            when(request.getParameter("playerId")).thenReturn("10");
            when(request.getParameter("fromGiornata")).thenReturn(null);
            when(request.getParameter("toGiornata")).thenReturn(null);

            executeDoGet();

            verify(request).setAttribute(eq("statisticheList"), anyList());
            verify(request).setAttribute(eq("lastStat"), any(Statistiche.class));
            verify(request).setAttribute(eq("selectedPlayerId"), eq(10));

            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testTC2_MissingPlayerId() throws Exception {
        when(request.getParameter("playerId")).thenReturn(null);

        executeDoGet();

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametro playerId mancante");
        verifyNoInteractions(dispatcher);
    }

    @Test
    void testTC3_InvalidFormat() throws Exception {
        when(request.getParameter("playerId")).thenReturn("abc");

        executeDoGet();

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Formato parametri non valido");
    }
}