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

import subsystems.access_profile.control.RegisterServlet;
import subsystems.access_profile.model.User;
import subsystems.access_profile.model.UserDAO;
import connection.DBConnection;

import org.h2.jdbcx.JdbcDataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;

public class RegisterServletIntegrationTest {

    private RegisterServlet servlet;
    private UserDAO userDAO;
    private JdbcDataSource h2DataSource;

    private final String TEST_EMAIL = "integrazione@test.it";
    private final String TEST_PASSWORD = "Password1!";

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);


        h2DataSource = new JdbcDataSource();

        h2DataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;NON_KEYWORDS=USER");
        h2DataSource.setUser("sa");
        h2DataSource.setPassword("");


        injectDataSourceIntoDBConnection(h2DataSource);


        servlet = new RegisterServlet();
        userDAO = new UserDAO();

        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);


        try (Connection con = h2DataSource.getConnection();
             Statement stmt = con.createStatement()) {


            String sql = "CREATE TABLE IF NOT EXISTS user (" +
                    "email VARCHAR(255) PRIMARY KEY, " +
                    "password VARCHAR(255), " +
                    "nome VARCHAR(255), " +
                    "cognome VARCHAR(255), " +
                    "username VARCHAR(255), " +
                    "ruolo VARCHAR(50), " +
                    "is_active BOOLEAN, " +
                    "verification_token VARCHAR(255)" +
                    ")";
            stmt.execute(sql);
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        try (Connection con = h2DataSource.getConnection();
             Statement stmt = con.createStatement()) {

            stmt.execute("DELETE FROM user");
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
                // Tentativo disperato: stampa i campi disponibili per debug
                for(Field f : DBConnection.class.getDeclaredFields()){
                    System.out.println("Campo trovato: " + f.getName());
                }
                throw new RuntimeException("Non trovo il campo DataSource in DBConnection.");
            }
        }
        field.setAccessible(true);
        field.set(null, ds);
    }

    private void executeDoPost() throws Exception {
        Method doPost = RegisterServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    @Test
    void testIntegration_RegisterSuccess() throws Exception {

        when(request.getParameter("nome")).thenReturn("Mario");
        when(request.getParameter("cognome")).thenReturn("Rossi");
        when(request.getParameter("email")).thenReturn(TEST_EMAIL);
        when(request.getParameter("username")).thenReturn("MarioRossi88"); // Aggiunto se serve
        when(request.getParameter("password")).thenReturn(TEST_PASSWORD);
        when(request.getParameter("conferma_password")).thenReturn(TEST_PASSWORD);


        executeDoPost();


        User savedUser = userDAO.doRetrieveByEmail(TEST_EMAIL);

        assertNotNull(savedUser, "L'utente dovrebbe essere stato salvato nel database H2");
        assertEquals("Mario", savedUser.getNome());
        assertEquals("Rossi", savedUser.getCognome());

        verify(response).sendRedirect(contains("login.jsp"));
    }
}