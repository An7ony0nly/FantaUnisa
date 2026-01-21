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

import subsystems.community.control.CommentServlet;
import subsystems.access_profile.model.User;
import subsystems.community.model.Comment;
import subsystems.community.model.CommentDAO;
import connection.DBConnection;

import org.h2.jdbcx.JdbcDataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import javax.sql.DataSource;

public class CommentServletIntegrationTest {

    private CommentServlet servlet;
    private CommentDAO commentDAO;
    private JdbcDataSource h2DataSource;

    private final String TEST_EMAIL = "commentatore@test.it";
    private final String TEST_COMMENT_TEXT = "Questo è un commento di prova integrazione.";
    private final int TEST_POST_ID = 1;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);


        h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:testdb_comment;DB_CLOSE_DELAY=-1;MODE=MySQL;NON_KEYWORDS=USER");
        h2DataSource.setUser("sa");
        h2DataSource.setPassword("");


        injectDataSourceIntoDBConnection(h2DataSource);


        servlet = new CommentServlet();
        commentDAO = new CommentDAO();


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
                    "VALUES ('" + TEST_EMAIL + "', 'Mario', 'Commenti', 'FANTALLENATORE', true)");


            stmt.execute("CREATE TABLE IF NOT EXISTS post (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "titolo VARCHAR(255), " +
                    "contenuto TEXT, " +
                    "user_email VARCHAR(255)" +
                    ")");
            stmt.execute("INSERT INTO post (id, titolo, user_email) VALUES (" + TEST_POST_ID + ", 'Post Test', '" + TEST_EMAIL + "')");


            stmt.execute("CREATE TABLE IF NOT EXISTS comment (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "post_id INT, " +
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
            stmt.execute("DROP TABLE comment");
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
        Method doPost = CommentServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    //TEST 1: Inserimento Commento con Successo
    @Test
    void testIntegration_PostComment_Success() throws Exception {

        User user = new User();
        user.setEmail(TEST_EMAIL);
        when(session.getAttribute("user")).thenReturn(user);


        when(request.getParameter("testo")).thenReturn(TEST_COMMENT_TEXT);
        when(request.getParameter("body")).thenReturn(TEST_COMMENT_TEXT);
        when(request.getParameter("comment")).thenReturn(TEST_COMMENT_TEXT);
        when(request.getParameter("contenuto")).thenReturn(TEST_COMMENT_TEXT);

        String idStr = String.valueOf(TEST_POST_ID);
        when(request.getParameter("idPost")).thenReturn(idStr);
        when(request.getParameter("postId")).thenReturn(idStr);
        when(request.getParameter("post_id")).thenReturn(idStr);
        when(request.getParameter("id")).thenReturn(idStr);


        executeDoPost();


        List<Comment> comments = commentDAO.doRetrieveByPostId(TEST_POST_ID);

        System.out.println("Numero commenti trovati nel DB: " + comments.size());

        assertFalse(comments.isEmpty(), "Deve esserci almeno un commento nel DB.");

        boolean found = comments.stream()
                .anyMatch(c -> c.getTesto().equals(TEST_COMMENT_TEXT) && c.getUserEmail().equals(TEST_EMAIL));

        assertTrue(found, "Il commento inserito deve esistere nel database con il testo corretto");


        verify(response).sendRedirect(contains("PostServlet"));
    }

    //TEST 2: Utente Non Loggato
    @Test
    void testIntegration_PostComment_NotLogged() throws Exception {
        when(session.getAttribute("user")).thenReturn(null);

        executeDoPost();

        List<Comment> comments = commentDAO.doRetrieveByPostId(TEST_POST_ID);
        assertTrue(comments.isEmpty(), "Non devono essere salvati commenti se utente non è loggato");

        verify(response).sendRedirect(contains("login.jsp"));
    }
}
