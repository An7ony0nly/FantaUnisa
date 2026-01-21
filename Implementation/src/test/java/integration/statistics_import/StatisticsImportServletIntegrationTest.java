package integration.statistics_import;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import subsystems.statistics_import.control.StatisticsImportServlet;
import subsystems.access_profile.model.Role;
import subsystems.access_profile.model.User;
import subsystems.team_management.model.Player;
import subsystems.team_management.model.PlayerDAO;
import subsystems.statistics_import.model.StatisticheDAO;
import subsystems.statistics_viewer.model.Statistiche;
import connection.DBConnection;

import org.h2.jdbcx.JdbcDataSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;
import java.nio.charset.StandardCharsets;
import javax.sql.DataSource;

public class StatisticsImportServletIntegrationTest {

    private StatisticsImportServlet servlet;
    private PlayerDAO playerDAO;
    private StatisticheDAO statisticheDAO;
    private JdbcDataSource h2DataSource;
    private StringWriter stringWriter;
    private PrintWriter writer;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;
    @Mock private Part filePart;
    @Mock private ServletContext servletContext;
    @Mock private ServletConfig servletConfig;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);


        h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:testdb_import;DB_CLOSE_DELAY=-1;MODE=MySQL;NON_KEYWORDS=USER");
        h2DataSource.setUser("sa");
        h2DataSource.setPassword("");

        injectDataSourceIntoDBConnection(h2DataSource);


        servlet = new StatisticsImportServlet();
        playerDAO = new PlayerDAO();
        statisticheDAO = new StatisticheDAO();


        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(servletConfig.getServletContext()).thenReturn(servletContext);
        servlet.init(servletConfig);

        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);


        try (Connection con = h2DataSource.getConnection();
             Statement stmt = con.createStatement()) {


            stmt.execute("CREATE TABLE IF NOT EXISTS player (" +
                    "id INT PRIMARY KEY, " +
                    "nome VARCHAR(255), " +
                    "cognome VARCHAR(255), " +
                    "ruolo VARCHAR(50), " +
                    "squadra_seriea VARCHAR(50), " +
                    "media_voto DOUBLE DEFAULT 0.0, " +
                    "fantamedia DOUBLE DEFAULT 0.0, " +
                    "gol_fatti INT DEFAULT 0, " +
                    "gol_subiti INT DEFAULT 0, " +
                    "assist INT DEFAULT 0, " +
                    "quotazione_attuale INT DEFAULT 1, " +
                    "user_email VARCHAR(255)" +
                    ")");


            stmt.execute("CREATE TABLE IF NOT EXISTS statistic (" +
                    "player_id INT, " +
                    "giornata INT, " +
                    "partite_voto INT DEFAULT 0, " +
                    "media_voto DOUBLE DEFAULT 0.0, " +
                    "fanta_media DOUBLE DEFAULT 0.0, " +
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
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        try (Connection con = h2DataSource.getConnection();
             Statement stmt = con.createStatement()) {

            stmt.execute("DROP TABLE IF EXISTS statistic");
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
                throw new RuntimeException("Campo DataSource non trovato in DBConnection");
            }
        }
        field.setAccessible(true);
        field.set(null, ds);
    }

    private void executeDoPost() throws Exception {
        Method doPost = StatisticsImportServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    @Test
    void testIntegration_ImportSuccess() throws Exception {
        User admin = new User();
        admin.setRole(Role.GESTORE_DATI);
        when(session.getAttribute("user")).thenReturn(admin);

        when(request.getParameter("giornata")).thenReturn("3");


        String header = "Id;Ruolo;Nome;Squadra;Pv;Mv;Fm;Gf;Gs;Rp;Rc;R+;R-;Ass;Amm;Esp;Au";
        String row1 = "101;A;Victor Osimhen;Napoli;1;7.5;10.5;1;0;0;0;0;0;0;0;0;0";

        String csvContent = header + "\n" + row1;

        InputStream fileStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
        when(filePart.getInputStream()).thenReturn(fileStream);
        when(request.getPart("file")).thenReturn(filePart);

        executeDoPost();

        if (!stringWriter.toString().contains("Importazione completata")) {
            System.out.println("DEBUG - Errore Servlet: " + stringWriter.toString());
        }

        Player osimhen = playerDAO.doRetrieveById(101);
        assertNotNull(osimhen, "Il giocatore Osimhen deve essere stato inserito nel DB.");
        assertEquals("Napoli", osimhen.getSquadra());

        Statistiche stats = statisticheDAO.findByPlayerAndGiornata(101, 3);
        assertNotNull(stats);
        assertEquals(7.5, stats.getMediaVoto());

        verify(servletContext).setAttribute(eq("LISTA_GIOCATORI_CACHE"), anyList());
    }

    @Test
    void testIntegration_ImportRollback() throws Exception {
        User admin = new User();
        admin.setRole(Role.GESTORE_DATI);
        when(session.getAttribute("user")).thenReturn(admin);

        when(request.getParameter("giornata")).thenReturn("5");

        String csvContent = "RIGA_INVALIDA_SENZA_FORMATO_GIUSTO";

        InputStream fileStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
        when(filePart.getInputStream()).thenReturn(fileStream);
        when(request.getPart("file")).thenReturn(filePart);


        try (Connection con = h2DataSource.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute("DROP TABLE player");
        }

        executeDoPost();

        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());
    }

    @Test
    void testIntegration_ImportForbidden() throws Exception {
        User utente = new User();
        utente.setRole(Role.FANTALLENATORE);
        when(session.getAttribute("user")).thenReturn(utente);

        executeDoPost();

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Non autorizzato");
    }
}