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
import subsystems.community.model.Comment;
import subsystems.community.model.CommentDAO;

import java.lang.reflect.Method;

public class CommentServletTest {

    private CommentServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new CommentServlet();

        User user = new User();
        user.setEmail("user@test.it");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
    }

    private void executeDoPost() throws Exception {
        Method doPost = CommentServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    @Test
    void testTC1_CommentoOK() throws Exception {
        try (MockedConstruction<CommentDAO> mockedDAO = mockConstruction(CommentDAO.class)) {

            when(request.getParameter("postId")).thenReturn("10");
            when(request.getParameter("testo")).thenReturn("Bel centrocampo!");
            when(request.getParameter("formationId")).thenReturn(null);

            executeDoPost();

            CommentDAO dao = mockedDAO.constructed().get(0);
            ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
            verify(dao).doSave(commentCaptor.capture());

            Comment saved = commentCaptor.getValue();
            assertEquals("Bel centrocampo!", saved.getTesto());
            assertEquals(10, saved.getPostId());
            assertEquals("user@test.it", saved.getUserEmail());

            verify(response).sendRedirect("PostServlet");
        }
    }

    @Test
    void testTC2_CommentoVuoto() throws Exception {
        try (MockedConstruction<CommentDAO> mockedDAO = mockConstruction(CommentDAO.class)) {

            when(request.getParameter("postId")).thenReturn("10");
            when(request.getParameter("testo")).thenReturn("   ");
            when(request.getParameter("formationId")).thenReturn(null);

            executeDoPost();

            assertTrue(mockedDAO.constructed().isEmpty());

            verify(response).sendRedirect("view/community.jsp?error=EmptyComment");
        }
    }

    @Test
    void testTC3_PostNonTrovato() throws Exception {
        try (MockedConstruction<CommentDAO> mockedDAO = mockConstruction(CommentDAO.class,
                (mock, context) -> {
                    doThrow(new RuntimeException("Post non trovato")).when(mock).doSave(any());
                })) {

            when(request.getParameter("postId")).thenReturn("999");
            when(request.getParameter("testo")).thenReturn("Commento su post fantasma");

            executeDoPost();

            verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), contains("Errore salvataggio commento"));
        }
    }
}