package integration.access_profile;

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

import subsystems.access_profile.control.UpdateProfileServlet;
import subsystems.access_profile.model.User;
import subsystems.access_profile.model.UserDAO;
import connection.DBConnection;

import org.h2.jdbcx.JdbcDataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;

public class UpdateProfileServletIntegrationTest {

    private UpdateProfileServlet servlet;
    private UserDAO userDAO;
    private JdbcDataSource h2DataSource;

    private final String TEST_EMAIL = "profile@test.it";
    private final String OLD_NAME = "VecchioNome";
    private final String OLD_SURNAME = "VecchioCognome";
    private final String NEW_NAME = "NuovoNome";
    private final String NEW_SURNAME = "NuovoCognome";

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);


        h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:testdb_profile;DB_CLOSE_DELAY=-1;MODE=MySQL;NON_KEYWORDS=USER");
        h2DataSource.setUser("sa");
        h2DataSource.setPassword("");


        injectDataSourceIntoDBConnection(h2DataSource);


        servlet = new UpdateProfileServlet();
        userDAO = new UserDAO();


        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);


        try (Connection con = h2DataSource.getConnection();
             Statement stmt = con.createStatement()) {


            stmt.execute("CREATE TABLE IF NOT EXISTS user (" +
                    "email VARCHAR(255) PRIMARY KEY, " +
                    "password VARCHAR(255), " +
                    "nome VARCHAR(255), " +
                    "cognome VARCHAR(255), " +
                    "username VARCHAR(255), " +
                    "ruolo VARCHAR(50), " +
                    "is_active BOOLEAN, " +
                    "verification_token VARCHAR(255), " +
                    "reset_token VARCHAR(255), " +
                    "reset_expiry DATETIME" +
                    ")");


            String sqlInsert = String.format("INSERT INTO user (email, password, nome, cognome, ruolo, is_active) " +
                            "VALUES ('%s', 'pass', '%s', '%s', 'FANTALLENATORE', true)",
                    TEST_EMAIL, OLD_NAME, OLD_SURNAME);

            stmt.execute(sqlInsert);
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        try (Connection con = h2DataSource.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute("DROP TABLE user");
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
        Method doPost = UpdateProfileServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    //TEST 1: Modifica Nome e Cognome con Successo
    @Test
    void testIntegration_UpdateProfileSuccess() throws Exception {

        User sessionUser = new User();
        sessionUser.setEmail(TEST_EMAIL);
        sessionUser.setNome(OLD_NAME);
        sessionUser.setCognome(OLD_SURNAME);
        when(session.getAttribute("user")).thenReturn(sessionUser);


        when(request.getParameter("nome")).thenReturn(NEW_NAME);
        when(request.getParameter("cognome")).thenReturn(NEW_SURNAME);


        executeDoPost();


        User updatedUserDB = userDAO.doRetrieveByEmail(TEST_EMAIL);
        assertEquals(NEW_NAME, updatedUserDB.getNome(), "Il nome nel DB deve essere aggiornato");
        assertEquals(NEW_SURNAME, updatedUserDB.getCognome(), "Il cognome nel DB deve essere aggiornato");


        assertEquals(NEW_NAME, sessionUser.getNome(), "L'oggetto in sessione deve essere aggiornato");


        verify(response).sendRedirect(contains("ProfileUpdated"));
    }

    //TEST 2: Nessuna Modifica
    @Test
    void testIntegration_NoChanges() throws Exception {

        User sessionUser = new User();
        sessionUser.setEmail(TEST_EMAIL);
        sessionUser.setNome(OLD_NAME);
        sessionUser.setCognome(OLD_SURNAME);
        when(session.getAttribute("user")).thenReturn(sessionUser);


        when(request.getParameter("nome")).thenReturn(OLD_NAME);
        when(request.getParameter("cognome")).thenReturn(OLD_SURNAME);

        executeDoPost();


        User dbUser = userDAO.doRetrieveByEmail(TEST_EMAIL);
        assertEquals(OLD_NAME, dbUser.getNome());


        verify(response).sendRedirect(argThat(url -> !url.contains("ProfileUpdated") && url.contains("profilo.jsp")));
    }

    //TEST 3: Utente Non Loggato
    @Test
    void testIntegration_NotLogged() throws Exception {

        when(request.getSession(false)).thenReturn(null);

        executeDoPost();


        verify(response).sendRedirect(contains("login.jsp"));
    }
}