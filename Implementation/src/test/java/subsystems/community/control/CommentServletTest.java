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

        // Setup base: Utente loggato
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

    // --- TC1: Commento OK (Testo valido) ---
    @Test
    void testTC1_CommentoOK() throws Exception {
        try (MockedConstruction<CommentDAO> mockedDAO = mockConstruction(CommentDAO.class)) {

            // Input Validi
            when(request.getParameter("postId")).thenReturn("10");
            when(request.getParameter("testo")).thenReturn("Bel centrocampo!");
            when(request.getParameter("formationId")).thenReturn(null);

            executeDoPost();

            // Verifiche
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

    // --- TC2: Commento Vuoto ---
    @Test
    void testTC2_CommentoVuoto() throws Exception {
        try (MockedConstruction<CommentDAO> mockedDAO = mockConstruction(CommentDAO.class)) {

            // Input: Testo vuoto e Nessuna formazione
            when(request.getParameter("postId")).thenReturn("10");
            when(request.getParameter("testo")).thenReturn("   ");
            when(request.getParameter("formationId")).thenReturn(null);

            executeDoPost();

            // --- CORREZIONE QUI ---
            // La servlet fa return PRIMA di fare "new CommentDAO()".
            // Quindi non dobbiamo cercare di prenderlo con .get(0), ma verificare che NON esista.
            assertTrue(mockedDAO.constructed().isEmpty(), "Il DAO non dovrebbe essere creato se l'input non è valido");

            verify(response).sendRedirect("view/community.jsp?error=EmptyComment");
        }
    }

    // --- TC3: Post Non Trovato (Simulazione Errore DB) ---
    @Test
    void testTC3_PostNonTrovato() throws Exception {
        try (MockedConstruction<CommentDAO> mockedDAO = mockConstruction(CommentDAO.class,
                (mock, context) -> {
                    // Simuliamo errore salvataggio
                    doThrow(new RuntimeException("Post non trovato")).when(mock).doSave(any());
                })) {

            when(request.getParameter("postId")).thenReturn("999");
            when(request.getParameter("testo")).thenReturn("Commento su post fantasma");

            executeDoPost();

            // La servlet cattura l'eccezione e manda un errore 500
            verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), contains("Errore salvataggio commento"));
        }
    }


}