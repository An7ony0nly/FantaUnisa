package integration.community;

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

import subsystems.community.control.ReactionServlet;
import subsystems.access_profile.model.User;
import subsystems.community.model.ReactionDAO;
import connection.DBConnection;

import org.h2.jdbcx.JdbcDataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;

public class ReactionServletIntegrationTest {

    private ReactionServlet servlet;
    private ReactionDAO reactionDAO;
    private JdbcDataSource h2DataSource;

    private final String TEST_EMAIL = "fan@test.it";
    private final int TEST_POST_ID = 100;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);


        h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:testdb_reaction;DB_CLOSE_DELAY=-1;MODE=MySQL;NON_KEYWORDS=USER");
        h2DataSource.setUser("sa");
        h2DataSource.setPassword("");


        injectDataSourceIntoDBConnection(h2DataSource);


        servlet = new ReactionServlet();
        reactionDAO = new ReactionDAO();


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
                    "verification_token VARCHAR(255)" +
                    ")");

            stmt.execute("INSERT INTO user (email, nome, cognome, ruolo, is_active) " +
                    "VALUES ('" + TEST_EMAIL + "', 'Mario', 'Reazioni', 'FANTALLENATORE', true)");


            stmt.execute("CREATE TABLE IF NOT EXISTS post (" +
                    "id INT PRIMARY KEY, " +
                    "titolo VARCHAR(255), " +
                    "user_email VARCHAR(255)" +
                    ")");
            stmt.execute("INSERT INTO post (id, titolo, user_email) VALUES (" + TEST_POST_ID + ", 'Post da Votare', '" + TEST_EMAIL + "')");


            stmt.execute("CREATE TABLE IF NOT EXISTS reaction (" +
                    "user_email VARCHAR(255), " +
                    "post_id INT, " +
                    "tipo VARCHAR(50), " +
                    "PRIMARY KEY (user_email, post_id), " +
                    "FOREIGN KEY (user_email) REFERENCES user(email), " +
                    "FOREIGN KEY (post_id) REFERENCES post(id)" +
                    ")");
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        try (Connection con = h2DataSource.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute("DROP TABLE reaction");
            stmt.execute("DROP TABLE post");
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
        Method doPost = ReactionServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    //TEST 1: Nuova Reazione
    @Test
    void testIntegration_NewReaction_Like() throws Exception {

        User user = new User();
        user.setEmail(TEST_EMAIL);
        when(session.getAttribute("user")).thenReturn(user);


        String idStr = String.valueOf(TEST_POST_ID);
        when(request.getParameter("postId")).thenReturn(idStr);
        when(request.getParameter("idPost")).thenReturn(idStr);
        when(request.getParameter("post_id")).thenReturn(idStr);

        when(request.getParameter("reaction")).thenReturn("LIKE");
        when(request.getParameter("tipo")).thenReturn("LIKE");


        executeDoPost();


        String reactionFound = reactionDAO.doRetrieveUserReaction(TEST_EMAIL, TEST_POST_ID);

        assertEquals("LIKE", reactionFound, "La reazione LIKE deve essere salvata nel database");


        verify(response).sendRedirect(anyString());
    }


    @Test
    void testIntegration_UpdateReaction_ChangeToDislike() throws Exception {

        try (Connection con = h2DataSource.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute("INSERT INTO reaction (user_email, post_id, tipo) VALUES ('" + TEST_EMAIL + "', " + TEST_POST_ID + ", 'LIKE')");
        }


        User user = new User();
        user.setEmail(TEST_EMAIL);
        when(session.getAttribute("user")).thenReturn(user);


        when(request.getParameter("postId")).thenReturn(String.valueOf(TEST_POST_ID));
        when(request.getParameter("reaction")).thenReturn("DISLIKE");

        when(request.getParameter("idPost")).thenReturn(String.valueOf(TEST_POST_ID));
        when(request.getParameter("tipo")).thenReturn("DISLIKE");


        executeDoPost();


        String reactionFound = reactionDAO.doRetrieveUserReaction(TEST_EMAIL, TEST_POST_ID);

        assertEquals("DISLIKE", reactionFound, "La reazione deve essere aggiornata da LIKE a DISLIKE");
    }

    //TEST 3: Utente Non Loggato
    @Test
    void testIntegration_Reaction_NotLogged() throws Exception {
        when(session.getAttribute("user")).thenReturn(null);

        executeDoPost();


        String reactionFound = reactionDAO.doRetrieveUserReaction(TEST_EMAIL, TEST_POST_ID);
        assertNull(reactionFound, "Non deve essere salvata nessuna reazione se l'utente non è loggato");

        verify(response).sendRedirect(contains("login.jsp"));
    }
}