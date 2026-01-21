package integration.statistics_viewer;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import subsystems.statistics_viewer.control.LoadStatisticsServlet;
import subsystems.statistics_viewer.model.Statistiche;
import connection.DBConnection;

import org.h2.jdbcx.JdbcDataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import javax.sql.DataSource;

public class LoadStatisticsServletIntegrationTest {

    private LoadStatisticsServlet servlet;
    private JdbcDataSource h2DataSource;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;


    private final int PLAYER_ID = 10;
    private final int PLAYER_ID_NO_STATS = 99;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);


        h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:testdb_loadstats;DB_CLOSE_DELAY=-1;MODE=MySQL;NON_KEYWORDS=USER");
        h2DataSource.setUser("sa");
        h2DataSource.setPassword("");


        injectDataSourceIntoDBConnection(h2DataSource);


        servlet = new LoadStatisticsServlet();


        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);


        try (Connection con = h2DataSource.getConnection();
             Statement stmt = con.createStatement()) {


            stmt.execute("CREATE TABLE IF NOT EXISTS statistic (" +
                    "player_id INT, " +
                    "giornata INT, " +
                    "partite_voto INT DEFAULT 1, " +
                    "media_voto DOUBLE DEFAULT 6.0, " +
                    "fanta_media DOUBLE DEFAULT 6.0, " +
                    "gol_fatti INT DEFAULT 0, " +
                    "gol_subiti INT DEFAULT 0, " +
                    "rigori_parati INT DEFAULT 0, " +
                    "rigori_calciati INT DEFAULT 0, " +
                    "rigori_segnati INT DEFAULT 0, " +
                    "rigori_sbagliati INT DEFAULT 0, " +
                    "assist INT DEFAULT 0, " +
                    "ammonizioni INT DEFAULT 0, " +
                    "espulsioni INT DEFAULT 0, " +
                    "autogol INT DEFAULT 0, " +
                    "PRIMARY KEY (player_id, giornata)" +
                    ")");


            stmt.execute("INSERT INTO statistic (player_id, giornata, media_voto, fanta_media, gol_fatti) VALUES (10, 1, 6.0, 6.0, 0)");
            stmt.execute("INSERT INTO statistic (player_id, giornata, media_voto, fanta_media, gol_fatti) VALUES (10, 2, 7.0, 10.0, 1)");
            stmt.execute("INSERT INTO statistic (player_id, giornata, media_voto, fanta_media, gol_fatti) VALUES (10, 3, 5.0, 4.5, 0)");


            stmt.execute("INSERT INTO statistic (player_id, giornata, media_voto) VALUES (20, 1, 8.0)");
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        try (Connection con = h2DataSource.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute("DROP TABLE statistic");
        }
    }

    private void injectDataSourceIntoDBConnection(DataSource ds) throws Exception {
        Field field = null;
        try {
            field = DBConnection.class.getDeclaredField("ds");
        } catch (NoSuchFieldException e) {
            try {
                field = DBConnection.class.getDeclaredField("datasource");
            } catch (NoSuchFieldException ex) {
                throw new RuntimeException("Campo DataSource non trovato in DBConnection");
            }
        }
        field.setAccessible(true);
        field.set(null, ds);
    }

    private void executeDoGet() throws Exception {
        Method doGet = LoadStatisticsServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);
    }

    //TEST 1: Recupero Statistiche Completo
    @Test
    void testIntegration_LoadStats_All() throws Exception {

        when(request.getParameter("playerId")).thenReturn(String.valueOf(PLAYER_ID));
        when(request.getParameter("fromGiornata")).thenReturn(null);
        when(request.getParameter("toGiornata")).thenReturn(null);


        executeDoGet();


        verify(request).getRequestDispatcher("view/statistiche_view.jsp");
        verify(dispatcher).forward(request, response);


        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Statistiche>> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(request).setAttribute(eq("statisticheList"), listCaptor.capture());

        List<Statistiche> resultList = listCaptor.getValue();
        assertEquals(3, resultList.size(), "Dovrebbe restituire 3 record per il giocatore 10");


        ArgumentCaptor<Statistiche> statCaptor = ArgumentCaptor.forClass(Statistiche.class);
        verify(request).setAttribute(eq("lastStat"), statCaptor.capture());
        assertEquals(3, statCaptor.getValue().getGiornata(), "L'ultima statistica dovrebbe essere della giornata 3");
    }

    //TEST 2: Recupero con Range Giornate
    @Test
    void testIntegration_LoadStats_WithRange() throws Exception {

        when(request.getParameter("playerId")).thenReturn(String.valueOf(PLAYER_ID));
        when(request.getParameter("fromGiornata")).thenReturn("1");
        when(request.getParameter("toGiornata")).thenReturn("2");

        executeDoGet();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Statistiche>> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(request).setAttribute(eq("statisticheList"), listCaptor.capture());

        List<Statistiche> resultList = listCaptor.getValue();
        assertEquals(2, resultList.size(), "Nel range 1-2 dovrebbero esserci 2 record");
    }

    //TEST 3: Giocatore Senza Statistiche
    @Test
    void testIntegration_LoadStats_NoData() throws Exception {
        when(request.getParameter("playerId")).thenReturn(String.valueOf(PLAYER_ID_NO_STATS));

        executeDoGet();

        verify(dispatcher).forward(request, response);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Statistiche>> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(request).setAttribute(eq("statisticheList"), listCaptor.capture());

        assertTrue(listCaptor.getValue().isEmpty(), "La lista dovrebbe essere vuota");
        verify(request).setAttribute(eq("lastStat"), isNull());
    }

    //TEST 4: Parametro Mancante
    @Test
    void testIntegration_MissingParameter() throws Exception {
        when(request.getParameter("playerId")).thenReturn(null);

        executeDoGet();

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), contains("mancante"));
        verify(dispatcher, never()).forward(any(), any());
    }

    //TEST 5: Parametro Invalido
    @Test
    void testIntegration_InvalidFormat() throws Exception {
        when(request.getParameter("playerId")).thenReturn("ciao");

        executeDoGet();

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), contains("non valido"));
    }
}