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
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import subsystems.access_profile.control.LoginServlet;
import subsystems.access_profile.model.User;
import subsystems.access_profile.model.UserDAO;
import connection.DBConnection;
import utils.PasswordHasher;

import org.h2.jdbcx.JdbcDataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;

public class LoginServletIntegrationTest {

    private LoginServlet servlet;
    private JdbcDataSource h2DataSource;

    private final String TEST_EMAIL = "utente@test.it";
    private final String STORED_HASH = "HASH_CORRETTO_NEL_DB";

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);


        h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:testdb_login;DB_CLOSE_DELAY=-1;MODE=MySQL;NON_KEYWORDS=USER");
        h2DataSource.setUser("sa");
        h2DataSource.setPassword("");


        injectDataSourceIntoDBConnection(h2DataSource);


        servlet = new LoginServlet();


        when(request.getSession()).thenReturn(session);
        when(request.getSession(true)).thenReturn(session);
        when(request.getContextPath()).thenReturn("");
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

            String sqlInsert = String.format("INSERT INTO user (email, password, nome, cognome, ruolo, is_active) " +
                            "VALUES ('%s', '%s', 'Mario', 'Login', 'FANTALLENATORE', true)",
                    TEST_EMAIL, STORED_HASH);

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
        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    //TEST 1: Login con Successo
    @Test
    void testIntegration_LoginSuccess() throws Exception {
        try (MockedStatic<PasswordHasher> hasherMock = mockStatic(PasswordHasher.class)) {
            hasherMock.when(() -> PasswordHasher.hash("password123")).thenReturn(STORED_HASH);

            when(request.getParameter("email")).thenReturn(TEST_EMAIL);
            when(request.getParameter("password")).thenReturn("password123");

            executeDoPost();


            verify(session).setAttribute(eq("user"), any(User.class));


            verify(response).sendRedirect(contains("FormationServlet"));
        }
    }

    //TEST 2: Password Errata
    @Test
    void testIntegration_LoginWrongPassword() throws Exception {
        try (MockedStatic<PasswordHasher> hasherMock = mockStatic(PasswordHasher.class)) {
            hasherMock.when(() -> PasswordHasher.hash("sbagliata")).thenReturn("HASH_ERRATO");

            when(request.getParameter("email")).thenReturn(TEST_EMAIL);
            when(request.getParameter("password")).thenReturn("sbagliata");

            executeDoPost();


            verify(session, never()).setAttribute(eq("user"), any());


            verify(dispatcher).forward(request, response);


        }
    }

    // TEST 3: Utente Non Trovato
    @Test
    void testIntegration_UserNotFound() throws Exception {
        when(request.getParameter("email")).thenReturn("non_esiste@test.it");
        when(request.getParameter("password")).thenReturn("qualsiasi");

        executeDoPost();

        verify(session, never()).setAttribute(eq("user"), any());


        verify(dispatcher).forward(request, response);
    }
}