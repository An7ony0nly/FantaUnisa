package utils;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import jakarta.servlet.Filter;
import subsystems.access_profile.model.Role;
import subsystems.access_profile.model.User;

@WebFilter("/*")
public class AuthFilter implements Filter {

    // 1. RISORSE PUBBLICHE (Accessibili a TUTTI)
    private static final String[] PUBLIC_URLS = {
            "/index.jsp",
            "/login.jsp",
            "/LoginServlet",
            "/RegisterServlet",
            "/ActivationServlet",
            "/ResetPasswordServlet",
            "/css/", "/js/", "/images/", "/styles/", "/scripts/"
    };

    // 2. RISORSE GESTORE DATI
    private static final String[] DATA_ADMIN_URLS = {
            "/view/admin_upload.jsp",
            "/StatisticsImportServlet"
    };

    // 3. RISORSE GESTORE UTENTI
    private static final String[] USER_ADMIN_URLS = {
            "/view/admin_moderation.jsp",
            "/BanUserServlet",
            "/DeleteContentServlet"
    };

    // 4. RISORSE FANTALLENATORE (E GIOCO)
    private static final String[] GAME_URLS = {
            "/view/rosa.jsp",
            "/view/formazione.jsp",
            "/SquadServlet",
            "/FormationServlet",
            "/ModuleServlet",
            "/CommentServlet",
            "/ReactionServlet",
            "ProfileServlet",
    };

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();

        // 1. Controllo Risorse Pubbliche
        for (String publicUrl : PUBLIC_URLS) {
            if (uri.contains(publicUrl)) {
                chain.doFilter(request, response);
                return;
            }
        }

        // 2. Controllo Login
        boolean isLoggedIn = (session != null && session.getAttribute("user") != null);
        if (!isLoggedIn) {
            response.sendRedirect(contextPath + "/view/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");
        Role role = user.getRole();

        // 3. Controllo GESTORE DATI
        for (String url : DATA_ADMIN_URLS) {
            if (uri.contains(url)) {
                if (!role.equals(Role.GESTORE_DATI)) {
                    response.sendRedirect(contextPath + "/view/index.jsp");
                    return;
                }
            }
        }

        // 4. Controllo GESTORE UTENTI
        for (String url : USER_ADMIN_URLS) {
            if (uri.contains(url)) {
                if (!role.equals(Role.GESTORE_UTENTI)) {
                    response.sendRedirect(contextPath + "/view/index.jsp");
                    return;
                }
            }
        }

        /* 5. Controllo RISORSE GIOCO
           MODIFICA: Ora permettiamo l'accesso anche agli Admin (Gestore Utenti e Dati)
           invece di reindirizzarli altrove.
        */
        for (String url : GAME_URLS) {
            if (uri.contains(url)) {
                // Se NON sei nessuno dei tre ruoli autorizzati (quindi un ruolo sconosciuto o errato), via.
                // In pratica qui lasciamo passare tutti i ruoli definiti (Fantallenatore, Gestore Utenti, Gestore Dati)
                if (!role.equals(Role.FANTALLENATORE) && !role.equals(Role.GESTORE_UTENTI) && !role.equals(Role.GESTORE_DATI)) {
                    response.sendRedirect(contextPath + "/view/index.jsp");
                    return;
                }
                // Se sei uno di questi tre, passi.
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}