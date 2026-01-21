package integration.team_management;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import subsystems.team_management.control.FormationServlet;
import subsystems.access_profile.model.User;
import subsystems.team_management.model.Formation;
import subsystems.team_management.model.FormationDAO;
import connection.DBConnection;

import org.h2.jdbcx.JdbcDataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;
import javax.sql.DataSource;

public class FormationServletIntegrationTest {

    private FormationServlet servlet;
    private FormationDAO formationDAO;
    private JdbcDataSource h2DataSource;

    private final String TEST_USER = "allenatore@test.it";

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);


        h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:testdb_formation;DB_CLOSE_DELAY=-1;MODE=MySQL;NON_KEYWORDS=USER");
        h2DataSource.setUser("sa");
        h2DataSource.setPassword("");

        injectDataSourceIntoDBConnection(h2DataSource);


        servlet = new FormationServlet();
        formationDAO = new FormationDAO();

        when(request.getSession(false)).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);


        try (Connection con = h2DataSource.getConnection();
             Statement stmt = con.createStatement()) {


            stmt.execute("CREATE TABLE IF NOT EXISTS formation (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "user_email VARCHAR(255), " +
                    "giornata INT, " +
                    "modulo VARCHAR(10)" +
                    ")");


            stmt.execute("CREATE TABLE IF NOT EXISTS formation_player (" +
                    "formation_id INT, " +
                    "player_id INT, " +
                    "posizione VARCHAR(1), " +
                    "tipo VARCHAR(20), " +
                    "PRIMARY KEY (formation_id, player_id)" +
                    ")");


            stmt.execute("CREATE TABLE IF NOT EXISTS player (" +
                    "id INT PRIMARY KEY, " +
                    "nome VARCHAR(255), " +
                    "ruolo VARCHAR(1)" +
                    ")");


            for (int i = 1; i <= 11; i++) {
                stmt.execute(String.format("INSERT INTO player (id, nome, ruolo) VALUES (%d, 'Player %d', 'C')", i, i));
            }
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        try (Connection con = h2DataSource.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute("DROP TABLE formation_player");
            stmt.execute("DROP TABLE formation");
            stmt.execute("DROP TABLE player");
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
                throw new RuntimeException("Campo DataSource non trovato");
            }
        }
        field.setAccessible(true);
        field.set(null, ds);
    }

    private void executeDoPost() throws Exception {
        Method doPost = FormationServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    //TEST 1: Salvataggio Formazione con Successo
    @Test
    void testIntegration_SaveFormation_Success() throws Exception {

        User user = new User();
        user.setEmail(TEST_USER);
        when(session.getAttribute("user")).thenReturn(user);


        when(request.getParameter("giornata")).thenReturn("18");
        when(request.getParameter("modulo")).thenReturn("4-4-2");

        String[] giocatori = {
                "1:P:titolare", "2:D:titolare", "3:D:titolare", "4:D:titolare", "5:D:titolare",
                "6:C:titolare", "7:C:titolare", "8:C:titolare", "9:C:titolare", "10:A:titolare", "11:A:titolare"
        };
        when(request.getParameterValues("giocatori")).thenReturn(giocatori);
        when(request.getParameter("testo")).thenReturn("");


        executeDoPost();


        Formation saved = formationDAO.doRetrieveById(1);

        assertNotNull(saved, "La formazione deve essere salvata nel DB");
        assertEquals("4-4-2", saved.getModulo());


        assertEquals(11, saved.getPlayers().size(), "Devono esserci 11 giocatori salvati come titolari");


        verify(request).getRequestDispatcher("/FormationServlet");
        verify(dispatcher).forward(request, response);
    }

    // TEST 2: Errore Validazione
    @Test
    void testIntegration_SaveFormation_InvalidCount() throws Exception {
        User user = new User();
        user.setEmail(TEST_USER);
        when(session.getAttribute("user")).thenReturn(user);

        when(request.getParameter("giornata")).thenReturn("18");
        when(request.getParameter("modulo")).thenReturn("4-4-2");


        String[] giocatori = {"1:P:titolare", "2:D:titolare"};
        when(request.getParameterValues("giocatori")).thenReturn(giocatori);

        executeDoPost();


        assertNull(formationDAO.doRetrieveById(1));


        verify(response).sendRedirect(contains("error="));
    }

    //TEST 3: Schiera e Pubblica
    @Test
    void testIntegration_SchieraEPubblica() throws Exception {
        User user = new User();
        user.setEmail(TEST_USER);
        when(session.getAttribute("user")).thenReturn(user);

        when(request.getParameter("giornata")).thenReturn("18");
        when(request.getParameter("modulo")).thenReturn("3-4-3");


        String[] giocatori = new String[11];
        for(int i=0; i<11; i++) giocatori[i] = (i+1) + ":C:titolare";
        when(request.getParameterValues("giocatori")).thenReturn(giocatori);


        String postText = "Ecco la mia formazione per la giornata 18!";
        when(request.getParameter("testo")).thenReturn(postText);

        executeDoPost();


        Formation saved = formationDAO.doRetrieveById(1);
        assertNotNull(saved);


        verify(request).setAttribute(eq("formationId"), anyInt());
        verify(request).setAttribute(eq("postText"), eq(postText));
        verify(request).getRequestDispatcher("/PostServlet");
        verify(dispatcher).forward(request, response);
    }
}