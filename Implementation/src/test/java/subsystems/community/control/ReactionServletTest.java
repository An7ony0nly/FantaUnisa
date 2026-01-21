package subsystems.community.control;

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
import org.mockito.MockitoAnnotations;

import subsystems.access_profile.model.User;
import subsystems.community.model.Reaction;
import subsystems.community.model.ReactionDAO;

import java.lang.reflect.Method;

public class ReactionServletTest {

    private ReactionServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new ReactionServlet();

        User user = new User();
        user.setEmail("user@test.it");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
    }

    private void executeDoPost() throws Exception {
        Method doPost = ReactionServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    @Test
    void testTC1_AggiuntaReazione() throws Exception {
        try (MockedConstruction<ReactionDAO> mockedDAO = mockConstruction(ReactionDAO.class,
                (mock, context) -> {
                    when(mock.doRetrieveUserReaction(anyString(), anyInt())).thenReturn(null);
                })) {

            when(request.getParameter("postId")).thenReturn("10");
            when(request.getParameter("tipo")).thenReturn("LIKE");

            executeDoPost();

            ReactionDAO dao = mockedDAO.constructed().get(0);
            ArgumentCaptor<Reaction> captor = ArgumentCaptor.forClass(Reaction.class);

            verify(dao).doSaveOrUpdate(captor.capture());
            verify(dao, never()).doDelete(anyString(), anyInt());

            Reaction saved = captor.getValue();

            assertEquals("LIKE", saved.getTipo());
            assertEquals(10, saved.getPostId());
            assertEquals("user@test.it", saved.getUserEmail());

            verify(response).sendRedirect("PostServlet#10");
        }
    }

    @Test
    void testTC2_RimozioneReazione() throws Exception {
        try (MockedConstruction<ReactionDAO> mockedDAO = mockConstruction(ReactionDAO.class,
                (mock, context) -> {
                    when(mock.doRetrieveUserReaction("user@test.it", 10)).thenReturn("LIKE");
                })) {

            when(request.getParameter("postId")).thenReturn("10");
            when(request.getParameter("tipo")).thenReturn("LIKE");

            executeDoPost();

            ReactionDAO dao = mockedDAO.constructed().get(0);

            verify(dao).doDelete("user@test.it", 10);
            verify(dao, never()).doSaveOrUpdate(any());

            verify(response).sendRedirect("PostServlet#10");
        }
    }
}