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

import subsystems.access_profile.control.DeleteUserServlet;
import subsystems.access_profile.model.Role; // Assicurati di avere questo import
import subsystems.access_profile.model.User;
import subsystems.access_profile.model.UserDAO;
import connection.DBConnection;

import org.h2.jdbcx.JdbcDataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;

public class DeleteUserServletIntegrationTest {

    private DeleteUserServlet servlet;
    private UserDAO userDAO;
    private JdbcDataSource h2DataSource;


    private final String TARGET_EMAIL = "da_cancellare@test.it";

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);


        h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:testdb_delete;DB_CLOSE_DELAY=-1;MODE=MySQL;NON_KEYWORDS=USER");
        h2DataSource.setUser("sa");
        h2DataSource.setPassword("");


        injectDataSourceIntoDBConnection(h2DataSource);


        servlet = new DeleteUserServlet();
        userDAO = new UserDAO();


        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);


        try (Connection con = h2DataSource.getConnection();
             Statement stmt = con.createStatement()) {


            String sqlCreate = "CREATE TABLE IF NOT EXISTS user (" +
                    "email VARCHAR(255) PRIMARY KEY, " +
                    "password VARCHAR(255), " +
                    "nome VARCHAR(255), " +
                    "cognome VARCHAR(255), " +
                    "username VARCHAR(255), " +
                    "ruolo VARCHAR(50), " +
                    "is_active BOOLEAN, " +
                    "verification_token VARCHAR(255)" +
                    ")";
            stmt.execute(sqlCreate);


            String sqlInsert = "INSERT INTO user (email, password, nome, cognome, ruolo, is_active) " +
                    "VALUES ('" + TARGET_EMAIL + "', 'pass', 'Mario', 'Vittima', 'FANTALLENATORE', true)";
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
                throw new RuntimeException("Impossibile trovare il campo DataSource in DBConnection.");
            }
        }
        field.setAccessible(true);
        field.set(null, ds);
    }

    private void executeDoPost() throws Exception {
        Method doPost = DeleteUserServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }


    @Test
    void testIntegration_DeleteUser() throws Exception {

        assertNotNull(userDAO.doRetrieveByEmail(TARGET_EMAIL), "L'utente target deve esistere prima del test");


        User adminUser = new User();
        adminUser.setEmail("admin@test.it");
        adminUser.setRole(Role.GESTORE_UTENTI); // O il ruolo appropriato per cancellare
        when(session.getAttribute("user")).thenReturn(adminUser);


        when(request.getParameter("email")).thenReturn(TARGET_EMAIL);


        executeDoPost();


        User deletedUser = userDAO.doRetrieveByEmail(TARGET_EMAIL);
        assertNull(deletedUser, "L'utente dovrebbe essere stato rimosso dal database");


    }
}