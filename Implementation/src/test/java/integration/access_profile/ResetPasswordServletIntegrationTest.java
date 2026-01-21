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

import subsystems.access_profile.control.ResetPasswordServlet;
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

public class ResetPasswordServletIntegrationTest {

    private ResetPasswordServlet servlet;
    private UserDAO userDAO;
    private JdbcDataSource h2DataSource;

    private final String TEST_EMAIL = "reset@test.it";
    private final String OLD_PASSWORD = "OldPassword";
    private final String VALID_TOKEN = "token_valido_123";
    private final String NEW_PASSWORD = "NewPassword1!";
    private final String NEW_HASH = "HASHED_NEW_PASSWORD";

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);


        h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:testdb_reset;DB_CLOSE_DELAY=-1;MODE=MySQL;NON_KEYWORDS=USER");
        h2DataSource.setUser("sa");
        h2DataSource.setPassword("");


        injectDataSourceIntoDBConnection(h2DataSource);


        servlet = new ResetPasswordServlet();
        userDAO = new UserDAO();

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
                    "reset_token VARCHAR(255), " +
                    "reset_expiry DATETIME" +
                    ")");


            String sqlInsert = String.format("INSERT INTO user (email, password, nome, cognome, ruolo, is_active, reset_token, reset_expiry) " +
                            "VALUES ('%s', '%s', 'Mario', 'Reset', 'FANTALLENATORE', true, '%s', DATEADD('DAY', 1, CURRENT_TIMESTAMP))",
                    TEST_EMAIL, OLD_PASSWORD, VALID_TOKEN);

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

    private void executeDoGet() throws Exception {
        Method doGet = ResetPasswordServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);
    }

    private void executeDoPost() throws Exception {
        Method doPost = ResetPasswordServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    //TEST 1: doGet - Visualizzazione Form con Token Valido
    @Test
    void testIntegration_ShowResetForm_Success() throws Exception {
        when(request.getParameter("token")).thenReturn(VALID_TOKEN);

        executeDoGet();

        verify(request).setAttribute(eq("token"), eq(VALID_TOKEN));
        verify(request).getRequestDispatcher("view/reset_password.jsp");
        verify(dispatcher).forward(request, response);
    }

    //TEST 2: doGet - Token Non Valido
    @Test
    void testIntegration_ShowResetForm_InvalidToken() throws Exception {
        when(request.getParameter("token")).thenReturn("token_fasullo");

        executeDoGet();

        verify(response).sendRedirect(contains("error=TokenExpired"));
    }

    //TEST 3: doPost - Cambio Password Effettivo
    @Test
    void testIntegration_UpdatePassword_Success() throws Exception {
        try (MockedStatic<PasswordHasher> hasherMock = mockStatic(PasswordHasher.class)) {

            hasherMock.when(() -> PasswordHasher.hash(NEW_PASSWORD)).thenReturn(NEW_HASH);

            when(request.getParameter("token")).thenReturn(VALID_TOKEN);
            when(request.getParameter("password")).thenReturn(NEW_PASSWORD);

            executeDoPost();


            User updatedUser = userDAO.doRetrieveByEmail(TEST_EMAIL);
            assertEquals(NEW_HASH, updatedUser.getPassword(), "La password nel DB deve essere aggiornata");

            verify(response).sendRedirect(contains("PasswordUpdated"));
        }
    }

    // TEST 4: doPost - Tentativo con Token Invalido
    @Test
    void testIntegration_UpdatePassword_InvalidToken() throws Exception {
        when(request.getParameter("token")).thenReturn("token_sbagliato");
        when(request.getParameter("password")).thenReturn(NEW_PASSWORD);

        executeDoPost();


        User currentUser = userDAO.doRetrieveByEmail(TEST_EMAIL);
        assertEquals(OLD_PASSWORD, currentUser.getPassword(), "La password NON deve cambiare se il token è errato");

        verify(response).sendRedirect(contains("ErrorUpdating"));
    }
}