package integration.module_selection;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import subsystems.module_selection.control.ModuleServlet;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ModuleServletIntegrationTest {

    private ModuleServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        servlet = new ModuleServlet();
    }

    @Test
    void testIntegration_GetModules_ReturnsJson() throws Exception {

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);


        when(response.getWriter()).thenReturn(printWriter);


        java.lang.reflect.Method doGet = ModuleServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);


        printWriter.flush();


        verify(response).setContentType("application/json;charset=UTF-8");


        String jsonOutput = stringWriter.toString();

        System.out.println("JSON Restituito: " + jsonOutput);


        assertNotNull(jsonOutput);
        assertFalse(jsonOutput.isEmpty());


        assertTrue(jsonOutput.startsWith("[") && jsonOutput.endsWith("]"));


        assertTrue(jsonOutput.contains("\"id\":\"3-4-3\""));
        assertTrue(jsonOutput.contains("\"id\":\"4-4-2\""));
        assertTrue(jsonOutput.contains("\"id\":\"5-3-2\""));

        // Verifica la struttura dei dati
        assertTrue(jsonOutput.contains("\"difensori\":3"));
        assertTrue(jsonOutput.contains("\"centrocampisti\":4"));
    }
}