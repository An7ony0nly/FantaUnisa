package subsystems.access_profile.control;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import subsystems.access_profile.model.User;
import subsystems.access_profile.model.UserDAO;
import utils.EmailSender;
import utils.PasswordHasher;

import java.lang.reflect.Method;

public class RegisterServletTest {

    private RegisterServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new RegisterServlet();

        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(request.getContextPath()).thenReturn("/FantaUnisa");
    }

    private void executeDoPost() throws Exception {
        Method doPost = RegisterServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    @Test
    void testTC1_RegOK() throws Exception {
        try (MockedConstruction<UserDAO> mockedDAO = mockConstruction(UserDAO.class,
                (mock, context) -> {
                    when(mock.doRetrieveByEmail("new@test.it")).thenReturn(null);
                });
             MockedStatic<PasswordHasher> hasherMock = mockStatic(PasswordHasher.class);
             MockedStatic<EmailSender> emailSenderMock = mockStatic(EmailSender.class)) {

            when(request.getParameter("nome")).thenReturn("Mario");
            when(request.getParameter("cognome")).thenReturn("Rossi");
            when(request.getParameter("email")).thenReturn("new@test.it");
            when(request.getParameter("password")).thenReturn("Secure1!pass");
            when(request.getParameter("username")).thenReturn("mariorossi");

            hasherMock.when(() -> PasswordHasher.hash(anyString())).thenReturn("HASHED_PWD");

            executeDoPost();

            UserDAO createdMock = mockedDAO.constructed().get(0);
            verify(createdMock).doSave(any(User.class));
            verify(response).sendRedirect(contains("/view/login.jsp"));
        }
    }

    @Test
    void testTC2_EmailDup() throws Exception {
        try (MockedConstruction<UserDAO> mockedDAO = mockConstruction(UserDAO.class,
                (mock, context) -> {
                    when(mock.doRetrieveByEmail("m.rossi@unisa.it")).thenReturn(new User());
                })) {

            when(request.getParameter("email")).thenReturn("m.rossi@unisa.it");

            executeDoPost();

            UserDAO createdMock = mockedDAO.constructed().get(0);
            verify(createdMock, never()).doSave(any());
            verify(request).setAttribute(eq("error"), contains("Email già registrata"));
        }
    }

    @Test
    void testTC3_PassCorta() throws Exception {
        testPasswordFailure("new@test.it", "Short1!", "Password troppo corta");
    }

    @Test
    void testTC4_PassNoSym() throws Exception {
        testPasswordFailure("new@test.it", "Password123", "Formato password errato");
    }

    @Test
    void testTC5_PassComplessita() throws Exception {
        testPasswordFailure("marco@test.it", "passwordlunga", "Errore Complessità Pass");
    }

    private void testPasswordFailure(String email, String password, String errorMsg) throws Exception {
        try (MockedConstruction<UserDAO> mockedDAO = mockConstruction(UserDAO.class,
                (mock, context) -> when(mock.doRetrieveByEmail(anyString())).thenReturn(null));
             MockedStatic<PasswordHasher> hasherMock = mockStatic(PasswordHasher.class)) {

            when(request.getParameter("email")).thenReturn(email);
            when(request.getParameter("password")).thenReturn(password);

            hasherMock.when(() -> PasswordHasher.hash(password))
                    .thenThrow(new RuntimeException(errorMsg));

            executeDoPost();

            UserDAO createdMock = mockedDAO.constructed().get(0);
            verify(createdMock, never()).doSave(any());
            verify(request).setAttribute(eq("error"), anyString());
        }
    }
}