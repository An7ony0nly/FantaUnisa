package subsystems.module_selection.control;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import subsystems.module_selection.model.Module;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ModuleServletTest {

    private ModuleServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new ModuleServlet();
    }

    private void executeDoGet() throws Exception {
        Method doGet = ModuleServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);
    }

    @Test
    void testTC1_GetModulesJSON() throws Exception {
        try (MockedStatic<Module> moduleMock = mockStatic(Module.class)) {

            List<Module> fakeModules = new ArrayList<>();

            Module m1 = mock(Module.class);
            when(m1.getId()).thenReturn("3-4-3");
            when(m1.getDifensori()).thenReturn(3);
            when(m1.getCentrocampisti()).thenReturn(4);
            when(m1.getAttaccanti()).thenReturn(3);

            Module m2 = mock(Module.class);
            when(m2.getId()).thenReturn("4-4-2");
            when(m2.getDifensori()).thenReturn(4);
            when(m2.getCentrocampisti()).thenReturn(4);
            when(m2.getAttaccanti()).thenReturn(2);

            fakeModules.add(m1);
            fakeModules.add(m2);

            moduleMock.when(Module::getValidModules).thenReturn(fakeModules);

            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);

            executeDoGet();

            verify(response).setContentType("application/json;charset=UTF-8");

            writer.flush();
            String jsonOutput = stringWriter.toString();

            assertTrue(jsonOutput.startsWith("[") && jsonOutput.endsWith("]"));

            assertTrue(jsonOutput.contains("\"id\":\"3-4-3\""));
            assertTrue(jsonOutput.contains("\"difensori\":3"));

            assertTrue(jsonOutput.contains("},{"));
        }
    }

    @Test
    void testTC2_EmptyList() throws Exception {
        try (MockedStatic<Module> moduleMock = mockStatic(Module.class)) {

            moduleMock.when(Module::getValidModules).thenReturn(new ArrayList<>());

            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);

            executeDoGet();

            String jsonOutput = stringWriter.toString();
            assertEquals("[]", jsonOutput);
        }
    }
}