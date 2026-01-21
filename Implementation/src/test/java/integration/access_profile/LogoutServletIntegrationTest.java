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

import subsystems.access_profile.control.LogoutServlet;
import connection.DBConnection;

import org.h2.jdbcx.JdbcDataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.sql.DataSource;

public class LogoutServletIntegrationTest {

    private LogoutServlet servlet;
    private JdbcDataSource h2DataSource;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);


        h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:testdb_logout;DB_CLOSE_DELAY=-1;MODE=MySQL;NON_KEYWORDS=USER");
        h2DataSource.setUser("sa");
        h2DataSource.setPassword("");


        injectDataSourceIntoDBConnection(h2DataSource);


        servlet = new LogoutServlet();


        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @AfterEach
    void tearDown() {

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
        Method doGet = LogoutServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);
    }

    private void executeDoPost() throws Exception {
        Method doPost = LogoutServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    //TEST 1: Logout Eseguito Correttamente
    @Test
    void testIntegration_LogoutSuccess() throws Exception {

        when(request.getSession(false)).thenReturn(session); // Simula utente loggato
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn(""); // Per evitare "null/..." nei redirect


        executeDoGet();

        verify(session).invalidate();


        verify(response).sendRedirect(argThat(url -> url.contains("login.jsp") || url.contains("index.jsp")));
    }

    //TEST 2: Logout senza Sessione
    @Test
    void testIntegration_LogoutNoSession() throws Exception {

        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("");


        executeDoGet();


        verify(response).sendRedirect(argThat(url -> url.contains("login.jsp") || url.contains("index.jsp")));
    }
}