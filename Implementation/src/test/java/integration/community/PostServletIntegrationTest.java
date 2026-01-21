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

import subsystems.community.control.PostServlet;
import subsystems.access_profile.model.User;
import subsystems.community.model.Post;
import subsystems.community.model.PostDAO;
import connection.DBConnection;

import org.h2.jdbcx.JdbcDataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import javax.sql.DataSource;

public class PostServletIntegrationTest {

    private PostServlet servlet;
    private PostDAO postDAO;
    private JdbcDataSource h2DataSource;

    private final String TEST_EMAIL = "blogger@test.it";
    private final String TEST_CONTENT = "Secondo me vince il Napoli.";

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);


        h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:testdb_post;DB_CLOSE_DELAY=-1;MODE=MySQL;NON_KEYWORDS=USER");
        h2DataSource.setUser("sa");
        h2DataSource.setPassword("");


        injectDataSourceIntoDBConnection(h2DataSource);


        servlet = new PostServlet();
        postDAO = new PostDAO();


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
                    "VALUES ('" + TEST_EMAIL + "', 'Mario', 'Blogger', 'FANTALLENATORE', true)");


            stmt.execute("CREATE TABLE IF NOT EXISTS post (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "user_email VARCHAR(255), " +
                    "testo TEXT, " +
                    "data_ora DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "formation_id INT" +
                    ")");
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        try (Connection con = h2DataSource.getConnection();
             Statement stmt = con.createStatement()) {
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
        Method doPost = PostServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    //TEST 1: Creazione Post con Successo
    @Test
    void testIntegration_CreatePost_Success() throws Exception {

        User user = new User();
        user.setEmail(TEST_EMAIL);
        when(session.getAttribute("user")).thenReturn(user);


        when(request.getParameter("testo")).thenReturn(TEST_CONTENT);
        when(request.getParameter("contenuto")).thenReturn(TEST_CONTENT);
        when(request.getParameter("body")).thenReturn(TEST_CONTENT);


        executeDoPost();


        List<Post> posts = postDAO.doRetrieveAll();

        assertFalse(posts.isEmpty(), "Deve esserci almeno un post nel DB.");


        boolean found = posts.stream()
                .anyMatch(p -> p.getTesto().equals(TEST_CONTENT) && p.getUserEmail().equals(TEST_EMAIL));

        assertTrue(found, "Il post creato deve esistere nel database con il contenuto corretto");


        verify(response).sendRedirect(argThat(url -> url.contains("community") || url.contains("PostServlet") || url.contains("index")));
    }

    //TEST 2: Utente Non Loggato
    @Test
    void testIntegration_CreatePost_NotLogged() throws Exception {
        when(session.getAttribute("user")).thenReturn(null);

        executeDoPost();

        List<Post> posts = postDAO.doRetrieveAll();
        assertTrue(posts.isEmpty(), "Non devono essere salvati post se l'utente non è loggato");

        verify(response).sendRedirect(contains("login.jsp"));
    }
}