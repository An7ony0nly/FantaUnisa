package subsystems.statistics_import;

import connection.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

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
    
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Part filePart;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;

    private MockedStatic<DBConnection> dbConnectionMock;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new StatisticsImportServlet();
        
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
                101;P;Szczesny;Juventus;1;6,5;6,5;0;0;0;0;0;0;0;0;0;0""";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

        when(request.getParameter("giornata")).thenReturn("1");
        when(request.getPart("file")).thenReturn(filePart);
        when(filePart.getInputStream()).thenReturn(inputStream);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        servlet.doPost(request, response);

        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(response).getWriter();
        verify(connection).close();
    }

    @Test
    void testDoPostMissingParameters() throws ServletException, IOException {
        when(request.getParameter("giornata")).thenReturn(null);
        when(request.getPart("file")).thenReturn(filePart);

        servlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametri mancanti");
    }

    @Test
    void testDoPostInvalidGiornata() throws ServletException, IOException {
        when(request.getParameter("giornata")).thenReturn("invalid");
        when(request.getPart("file")).thenReturn(filePart);

        servlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Giornata non valida");
    }

    @Test
    void testDoPostImportError() throws Exception {
        String csvContent = "Invalid CSV Content";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

        when(request.getParameter("giornata")).thenReturn("1");
        when(request.getPart("file")).thenReturn(filePart);
        when(filePart.getInputStream()).thenReturn(inputStream);

        servlet.doPost(request, response);

        verify(connection).rollback();
        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());
        verify(connection).close();
    }
}
