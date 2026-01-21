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
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import subsystems.access_profile.model.User;
import subsystems.community.model.CommentDAO;
import subsystems.community.model.Post;
import subsystems.community.model.PostDAO;
import subsystems.community.model.ReactionDAO;
import utils.ReactionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PostServletTest {

    private PostServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new PostServlet();

        // Setup base: Utente loggato
        User user = new User();
        user.setEmail("user@test.it");
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session); // Per doGet
        when(session.getAttribute("user")).thenReturn(user);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    private void executeDoPost() throws Exception {
        Method doPost = PostServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    private void executeDoGet() throws Exception {
        Method doGet = PostServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);
    }

    // --- TEST CASE CREAZIONE (UC10) ---

    // TC1: Testo presente, Allegato Null -> Pubblicato
    @Test
    void testTC1_SoloTesto() throws Exception {
        try (MockedConstruction<PostDAO> mockedDAO = mockConstruction(PostDAO.class)) {

            // Input
            when(request.getAttribute("testo")).thenReturn(null);
            when(request.getParameter("testo")).thenReturn("Ciao a tutti!");
            when(request.getAttribute("formationId")).thenReturn(null);
            when(request.getParameter("formationId")).thenReturn(null);

            executeDoPost();

            // Verifiche
            PostDAO dao = mockedDAO.constructed().get(0);
            ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
            verify(dao).doSave(postCaptor.capture());

            Post saved = postCaptor.getValue();
            assertEquals("Ciao a tutti!", saved.getTesto());
            assertNull(saved.getFormationId());

            verify(response).sendRedirect("/PostServlet");
        }
    }

    // TC2: Testo Null, Allegato Formazione -> Pubblicato
    @Test
    void testTC2_SoloFormazione() throws Exception {
        try (MockedConstruction<PostDAO> mockedDAO = mockConstruction(PostDAO.class)) {

            // Input
            when(request.getParameter("testo")).thenReturn("");
            when(request.getParameter("formationId")).thenReturn("10");

            executeDoPost();

            PostDAO dao = mockedDAO.constructed().get(0);
            ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
            verify(dao).doSave(postCaptor.capture());

            Post saved = postCaptor.getValue();
            assertEquals(10, saved.getFormationId());
            assertEquals("", saved.getTesto());

            verify(response).sendRedirect("/PostServlet");
        }
    }

    // TC3: Testo e Allegato presenti -> Pubblicato
    @Test
    void testTC3_Completo() throws Exception {
        try (MockedConstruction<PostDAO> mockedDAO = mockConstruction(PostDAO.class)) {

            when(request.getParameter("testo")).thenReturn("Ecco la mia rosa");
            when(request.getParameter("formationId")).thenReturn("5");

            executeDoPost();

            PostDAO dao = mockedDAO.constructed().get(0);
            ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
            verify(dao).doSave(postCaptor.capture());

            Post saved = postCaptor.getValue();
            assertEquals("Ecco la mia rosa", saved.getTesto());
            assertEquals(5, saved.getFormationId());
        }
    }

    // TC4: Testo Null, Allegato Null -> Errore Vuoto
    @Test
    void testTC4_PostVuoto() throws Exception {
        try (MockedConstruction<PostDAO> mockedDAO = mockConstruction(PostDAO.class)) {

            // Input vuoti
            when(request.getParameter("testo")).thenReturn(null);
            when(request.getParameter("formationId")).thenReturn(null);

            executeDoPost();

            // --- CORREZIONE QUI ---
            // La servlet fa return prima di creare il DAO.
            // Quindi la lista constructed() è vuota.
            assertTrue(mockedDAO.constructed().isEmpty(), "Il DAO non deve essere creato se il post è vuoto");

            // Verifica: Redirect errore
            verify(response).sendRedirect("view/community.jsp?error=EmptyContent");
        }
    }

    // TC5: Provenienza da FormationServlet (Uso attributi request)
    @Test
    void testTC5_FromAttributes() throws Exception {
        try (MockedConstruction<PostDAO> mockedDAO = mockConstruction(PostDAO.class)) {

            // Simuliamo attributi settati da un forward precedente
            when(request.getAttribute("testo")).thenReturn("Post automatico");
            when(request.getAttribute("formationId")).thenReturn(100); // Integer Object

            executeDoPost();

            PostDAO dao = mockedDAO.constructed().get(0);
            ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
            verify(dao).doSave(postCaptor.capture());

            Post saved = postCaptor.getValue();
            assertEquals("Post automatico", saved.getTesto());
            assertEquals(100, saved.getFormationId());
        }
    }

    // --- TEST CASE VISUALIZZAZIONE (doGet) ---
    @Test
    void testDoGet_VisualizzaBacheca() throws Exception {
        // Dobbiamo mockare la costruzione di 3 DAO diversi + ReactionUtils statico
        try (MockedConstruction<PostDAO> mockedPostDAO = mockConstruction(PostDAO.class,
                (mock, context) -> {
                    List<Post> posts = new ArrayList<>();
                    Post p = new Post();
                    p.setId(1);
                    posts.add(p);
                    when(mock.doRetrieveAll()).thenReturn(posts);
                });
             MockedConstruction<CommentDAO> mockedCommentDAO = mockConstruction(CommentDAO.class,
                     (mock, context) -> when(mock.doRetrieveByPostId(anyInt())).thenReturn(new ArrayList<>()));
             MockedConstruction<ReactionDAO> mockedReactionDAO = mockConstruction(ReactionDAO.class,
                     (mock, context) -> when(mock.doRetrieveUserReaction(anyString(), anyInt())).thenReturn("LIKE"));
             MockedStatic<ReactionUtils> utilsMock = mockStatic(ReactionUtils.class)) {

            // Mock ReactionUtils
            utilsMock.when(() -> ReactionUtils.calculateReactionCounts(anyInt())).thenReturn(new HashMap<>());

            executeDoGet();

            // Verifiche
            verify(request).setAttribute(eq("posts"), anyList());
            verify(dispatcher).forward(request, response);
        }
    }


}