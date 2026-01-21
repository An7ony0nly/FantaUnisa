package subsystems.statistics_import.control;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import connection.DBConnection;
import subsystems.access_profile.model.Role;
import subsystems.access_profile.model.User;
import subsystems.statistics_import.model.StatisticheDAO;
import subsystems.team_management.model.Player;
import subsystems.team_management.model.PlayerDAO;
import subsystems.statistics_viewer.model.Statistiche;
import utils.CsvParser;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class StatisticsImportServletTest {


    public static class MockJndiFactory implements InitialContextFactory {
        @Override
        public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {

            Context rootContext = mock(Context.class);
            Context envContext = mock(Context.class);
            DataSource dataSource = mock(DataSource.class);


            when(rootContext.lookup("java:comp/env")).thenReturn(envContext);


            when(envContext.lookup(anyString())).thenReturn(dataSource);

            return rootContext;
        }
    }


    static {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, MockJndiFactory.class.getName());
    }


    private StatisticsImportServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private Part filePart;
    @Mock private ServletContext servletContext;
    @Mock private Connection connection;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        servlet = new StatisticsImportServlet() {
            @Override
            public ServletContext getServletContext() {
                return servletContext;
            }
        };

        User admin = new User();
        admin.setEmail("admin@test.it");
        admin.setRole(Role.GESTORE_DATI);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(admin);
    }

    private void executeDoPost() throws Exception {
        Method doPost = StatisticsImportServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    @Test
    void testTC1_ImportSuccess() throws Exception {
        try (MockedStatic<DBConnection> dbMock = mockStatic(DBConnection.class);
             MockedConstruction<CsvParser> parserMock = mockConstruction(CsvParser.class,
                     (mock, context) -> {
                         List<CsvParser.ImportData> fakeData = new ArrayList<>();
                         Player p = new Player();
                         Statistiche s = new Statistiche();
                         // Usiamo il costruttore corretto (che nel tuo screenshot richiedeva argomenti)
                         CsvParser.ImportData item = new CsvParser.ImportData(p, s);
                         fakeData.add(item);

                         when(mock.parse(any(), anyInt())).thenReturn(fakeData);
                     });
             MockedConstruction<PlayerDAO> playerDaoMock = mockConstruction(PlayerDAO.class);
             MockedConstruction<StatisticheDAO> statDaoMock = mockConstruction(StatisticheDAO.class)) {

            dbMock.when(DBConnection::getConnection).thenReturn(connection);

            when(request.getParameter("giornata")).thenReturn("1");
            when(request.getPart("file")).thenReturn(filePart);
            when(filePart.getInputStream()).thenReturn(new ByteArrayInputStream("contenuto,fake".getBytes()));

            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);

            executeDoPost();

            verify(connection).setAutoCommit(false);
            verify(connection).commit();
            verify(connection).close();

            PlayerDAO pDao = playerDaoMock.constructed().get(0);
            StatisticheDAO sDao = statDaoMock.constructed().get(0);
            verify(pDao).doSaveOrUpdate(eq(connection), any());
            verify(sDao).doSaveOrUpdate(eq(connection), any());

            verify(servletContext).setAttribute(eq("LISTA_GIOCATORI_CACHE"), anyList());
            assertTrue(stringWriter.toString().contains("Importazione completata"));
        }
    }

    @Test
    void testTC2_ParsingError() throws Exception {
        try (MockedStatic<DBConnection> dbMock = mockStatic(DBConnection.class);
             MockedConstruction<CsvParser> parserMock = mockConstruction(CsvParser.class,
                     (mock, context) -> {

                         when(mock.parse(any(), anyInt())).thenThrow(new RuntimeException("Formato CSV non valido"));
                     })) {



            when(request.getParameter("giornata")).thenReturn("1");
            when(request.getPart("file")).thenReturn(filePart);
            when(filePart.getInputStream()).thenReturn(new ByteArrayInputStream("dati,errati".getBytes()));

            executeDoPost();


            verify(connection, never()).rollback();

            verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());
        }
    }

    @Test
    void testTC4_AccessDenied() throws Exception {
        User userStandard = new User();
        userStandard.setRole(Role.FANTALLENATORE);
        when(session.getAttribute("user")).thenReturn(userStandard);

        executeDoPost();

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Non autorizzato");
    }

    @Test
    void testTC3_InvalidParams() throws Exception {
        when(request.getParameter("giornata")).thenReturn(null);
        executeDoPost();
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametri mancanti");

        reset(response);

        when(request.getParameter("giornata")).thenReturn("abc");
        when(request.getPart("file")).thenReturn(filePart);
        executeDoPost();
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Giornata non valida");
    }


}