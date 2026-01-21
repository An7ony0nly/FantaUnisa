package subsystems.community.control;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import subsystems.access_profile.model.User;
import subsystems.community.model.*;
import utils.ReactionUtils;


@WebServlet("/PostServlet")
public class PostServlet extends HttpServlet {

    // Gestione pubblicazione nuovi post
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("view/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");

        // --- LOGICA DI RECUPERO DATI (IBRIDA: ATTRIBUTI O PARAMETRI) ---

        // 1. Recupero TESTO
        // Prima controlla se è stato passato come attributo (dal forward), altrimenti dal form parameter
        Object testoAttr = request.getAttribute("testo");
        String testo = (testoAttr != null) ? (String) testoAttr : request.getParameter("testo");

        // 2. Recupero ID FORMAZIONE
        // Prima controlla attributi (nuova formazione appena creata), altrimenti parameter
        Object formationIdAttr = request.getAttribute("formationId");
        String formationIdParam = request.getParameter("formationId");

        Integer formationId = null;

        // Caso A: Arrivo da FormationServlet (è un Integer nell'attributo)
        if (formationIdAttr != null) {
            if (formationIdAttr instanceof Integer) {
                formationId = (Integer) formationIdAttr;
            } else {
                // Caso raro: è una stringa nell'attributo
                try { formationId = Integer.parseInt(formationIdAttr.toString()); } catch (Exception e) {}
            }
        }
        // Caso B: Arrivo da form normale (è una Stringa nel parametro)
        else if (formationIdParam != null && !formationIdParam.trim().isEmpty()) {
            try {
                formationId = Integer.parseInt(formationIdParam);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        // --- VALIDAZIONE ---

        boolean hasText = (testo != null && !testo.trim().isEmpty());
        boolean hasFormation = (formationId != null);

        // Se non c'è né testo né formazione, è un errore
        if (!hasText && !hasFormation) {
            response.sendRedirect("view/community.jsp?error=EmptyContent");
            return;
        }

        if (testo == null) testo = "";

        // --- SALVATAGGIO ---

        Post post = new Post(user.getEmail(), testo, formationId);
        PostDAO postDAO = new PostDAO();

        try {
            postDAO.doSave(post);
            // Redirect pulito alla pagina community/feed
            response.sendRedirect("/PostServlet");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore pubblicazione post");
        }
    }

    // Gestione visualizzazione bacheca
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = (User) request.getSession().getAttribute("user");
        ReactionDAO reactionDAO = new ReactionDAO();
        PostDAO postDAO = new PostDAO();
        CommentDAO commentDAO = new CommentDAO();

        // 1. Recupera tutti i post
        List<Post> posts = postDAO.doRetrieveAll();

        // 2. Per ogni post, recupera i suoi commenti
        for (Post p : posts) {
            p.setComments(commentDAO.doRetrieveByPostId(p.getId()));

            Map<String, Integer> counts = ReactionUtils.calculateReactionCounts(p.getId());
            p.setReactionCounts(counts);

            if (user != null) {
                String userReaction = reactionDAO.doRetrieveUserReaction(user.getEmail(), p.getId());
                p.setCurrentUserReaction(userReaction);
            }
        }
        request.setAttribute("posts", posts);
        request.getRequestDispatcher("view/community.jsp").forward(request, response);
    }
}