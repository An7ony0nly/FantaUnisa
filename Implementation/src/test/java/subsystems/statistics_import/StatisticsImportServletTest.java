package subsystems.statistics_import;

import connection.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import subsystems.access_profile.model.Role;
import subsystems.access_profile.model.User;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsImportServletTest {

    private StatisticsImportServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private Part filePart;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private HttpSession session;

    private MockedStatic<DBConnection> dbConnectionMock;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new StatisticsImportServlet();

        User u = new User();
        u.setEmail("gestore@test.it");
        u.setRole(Role.GESTORE_DATI);

        lenient().when(request.getSession(false)).thenReturn(session);
        lenient().when(session.getAttribute("user")).thenReturn(u);

        dbConnectionMock = mockStatic(DBConnection.class);
        dbConnectionMock.when(DBConnection::getConnection).thenReturn(connection);
        lenient().when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @AfterEach
    void tearDown() {
        dbConnectionMock.close();
    }

    @Test
    void testDoPostSuccess() throws Exception {
        String csvContent = """
                Id;R;Nome;Squadra;Pv;Mv;Fm;Gf;Gs;Rp;Rc;R+;R-;Ass;Amm;Esp;Au
                101;P;Szczesny;Juventus;1;6,5;6,5;0;0;0;0;0;0;0;0;0;0
                """;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

        when(request.getParameter("giornata")).thenReturn("1");
        when(request.getPart("file")).thenReturn(filePart);
        when(filePart.getInputStream()).thenReturn(inputStream);

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        servlet.doPost(request, response);

        verify(connection).setAutoCommit(false);
        verify(connection).commit();

        // CORREZIONE 2: Accetta che close() venga chiamato almeno una volta (spesso il DAO lo chiama internamente)
        verify(connection, atLeast(1)).close();
    }

    @Test
    void testDoPostUnauthorizedWhenNoSession() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Non autorizzato");
        dbConnectionMock.verify(DBConnection::getConnection, never());
    }

    @Test
    void testDoPostForbiddenWhenWrongRole() throws ServletException, IOException {
        User u = new User();
        u.setEmail("user@test.it");
        u.setRole(Role.FANTALLENATORE);
        when(session.getAttribute("user")).thenReturn(u);

        servlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Non autorizzato");
        dbConnectionMock.verify(DBConnection::getConnection, never());
    }

    @Test
    void testDoPostMissingParameters() throws ServletException, IOException {
        when(request.getParameter("giornata")).thenReturn(null);
        when(request.getPart("file")).thenReturn(filePart);

        servlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametri mancanti");
        dbConnectionMock.verify(DBConnection::getConnection, never());
    }

    @Test
    void testDoPostInvalidGiornata() throws ServletException, IOException {
        when(request.getParameter("giornata")).thenReturn("invalid");
        when(request.getPart("file")).thenReturn(filePart);

        servlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Giornata non valida");
        dbConnectionMock.verify(DBConnection::getConnection, never());
    }

    @Test
    void testDoPostImportErrorGeneric() throws Exception {
        String csvContent = "Invalid CSV Content";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

        when(request.getParameter("giornata")).thenReturn("1");
        when(request.getPart("file")).thenReturn(filePart);
        when(filePart.getInputStream()).thenReturn(inputStream);

        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        servlet.doPost(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());

    }
}