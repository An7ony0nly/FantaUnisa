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

import subsystems.team_management.control.SquadServlet;
import subsystems.access_profile.model.User;
import subsystems.team_management.model.Player;
import subsystems.team_management.model.Squad;
import subsystems.team_management.model.SquadDAO;
import connection.DBConnection;

import org.h2.jdbcx.JdbcDataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import javax.sql.DataSource;

public class SquadServletIntegrationTest {

    private SquadServlet servlet;
    private SquadDAO squadDAO;
    private JdbcDataSource h2DataSource;

    private final String TEST_USER_EMAIL = "utente@squad.it";

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);


        h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:testdb_squad;DB_CLOSE_DELAY=-1;MODE=MySQL;NON_KEYWORDS=USER");
        h2DataSource.setUser("sa");
        h2DataSource.setPassword("");

        injectDataSourceIntoDBConnection(h2DataSource);


        servlet = new SquadServlet();
        squadDAO = new SquadDAO();

        when(request.getSession(false)).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);


        try (Connection con = h2DataSource.getConnection();
             Statement stmt = con.createStatement()) {


            stmt.execute("CREATE TABLE IF NOT EXISTS player (" +
                    "id INT PRIMARY KEY, " +
                    "nome VARCHAR(255), " +
                    "squadra_seriea VARCHAR(50), " +
                    "ruolo VARCHAR(50), " +
                    "media_voto DOUBLE DEFAULT 0, " +
                    "fantamedia DOUBLE DEFAULT 0, " +
                    "gol_fatti INT DEFAULT 0, " +
                    "gol_subiti INT DEFAULT 0, " +
                    "assist INT DEFAULT 0" +
                    ")");


            stmt.execute("CREATE TABLE IF NOT EXISTS squad (" +
                    "user_email VARCHAR(255), " +
                    "player_id INT, " +
                    "PRIMARY KEY (user_email, player_id), " +
                    "FOREIGN KEY (player_id) REFERENCES player(id)" +
                    ")");


            for (int i = 1; i <= 30; i++) {
                stmt.execute(String.format(
                        "INSERT INTO player (id, nome, squadra_seriea, ruolo) VALUES (%d, 'Calciatore %d', 'Team X', 'C')", i, i));
            }
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        try (Connection con = h2DataSource.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS squad");
            stmt.execute("DROP TABLE IF EXISTS player");
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

    //TEST 1: Visualizzazione Rosa
    @Test
    void testIntegration_GetSquad_Success() throws Exception {
        User user = new User();
        user.setEmail(TEST_USER_EMAIL);
        when(session.getAttribute("user")).thenReturn(user);


        try (Connection con = h2DataSource.getConnection(); Statement stmt = con.createStatement()) {
            stmt.execute("INSERT INTO squad (user_email, player_id) VALUES ('"+TEST_USER_EMAIL+"', 1)");
            stmt.execute("INSERT INTO squad (user_email, player_id) VALUES ('"+TEST_USER_EMAIL+"', 2)");
        }

        executeDoGet();


        verify(request).setAttribute(eq("allPlayers"), anyList());
        verify(request).setAttribute(eq("mySquad"), any(Squad.class));
        verify(dispatcher).forward(request, response);
    }

    //TEST 2: Aggiornamento Rosa con 25 giocatori
    @Test
    void testIntegration_UpdateSquad_Success() throws Exception {
        User user = new User();
        user.setEmail(TEST_USER_EMAIL);
        when(session.getAttribute("user")).thenReturn(user);


        String[] selectedIds = new String[25];
        for (int i = 0; i < 25; i++) {
            selectedIds[i] = String.valueOf(i + 1);
        }
        when(request.getParameterValues("selectedPlayers")).thenReturn(selectedIds);

        executeDoPost();


        List<Player> players = squadDAO.doRetrieveByEmail(TEST_USER_EMAIL);
        assertEquals(25, players.size(), "La rosa nel DB deve avere 25 elementi");


        verify(response).sendRedirect(contains("msg=SquadSaved"));
    }

    //TEST 3: Errore nel numero di giocatori
    @Test
    void testIntegration_UpdateSquad_WrongCount() throws Exception {
        User user = new User();
        user.setEmail(TEST_USER_EMAIL);
        when(session.getAttribute("user")).thenReturn(user);


        String[] selectedIds = new String[10];
        for (int i = 0; i < 10; i++) selectedIds[i] = String.valueOf(i + 1);

        when(request.getParameterValues("selectedPlayers")).thenReturn(selectedIds);

        executeDoPost();


        List<Player> players = squadDAO.doRetrieveByEmail(TEST_USER_EMAIL);
        assertEquals(0, players.size(), "Nessun giocatore deve essere salvato se il numero è errato");


        verify(response).sendRedirect(contains("error=CountError"));
    }

    //TEST 4: Utente Non Loggato
    @Test
    void testIntegration_NoSession() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        executeDoPost();

        verify(response).sendRedirect(contains("login.jsp"));
    }
}