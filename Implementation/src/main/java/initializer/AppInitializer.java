package initializer;

import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.annotation.WebListener;
import observer_pattern.LogObserver;
import observer_pattern.SecurityEmailObserver;
import service.ChangePasswordService;
import subsystems.team_management.model.Player;
import subsystems.team_management.model.PlayerDAO;

import java.util.List;
/*+*/
@WebListener
public class AppInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("--------------------------------------------------");
        System.out.println("   AVVIO FANTAUNISA - INIZIALIZZAZIONE SISTEMA    ");
        System.out.println("--------------------------------------------------");

        // 1. Configurazione Observer (Design Pattern)
        System.out.println("1. Registrazione Observer...");
        ChangePasswordService service = ChangePasswordService.getInstance();
        service.attach(new SecurityEmailObserver());
        service.attach(new LogObserver());
        System.out.println("   OK -> Observer attivi.");

        // 2. Popolamento Database (DBPopulator)
        System.out.println("2. Verifica dati iniziali (DBPopulator)...");
        DBPopulator.ensureGestoriExist();

        // 3. Caricamento giocatori all'avvio
        PlayerDAO playerDAO = new PlayerDAO();
        List<Player> tuttiIGiocatori = playerDAO.doRetrieveAll();

        sce.getServletContext().setAttribute("LISTA_GIOCATORI_CACHE", tuttiIGiocatori);

        System.out.println("--------------------------------------------------");
        System.out.println("   SYSTEM READY - SERVER AVVIATO                  ");
        System.out.println("--------------------------------------------------");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("--------------------------------------------------");
        System.out.println("   FANTAUNISA - SPEGNIMENTO SISTEMA               ");

        // 2. STOP CLEANUP THREAD MYSQL (Via Reflection)
        // Usiamo la Reflection perché il driver è in Tomcat (context.xml) e non nel pom.xml
        try {
            Class<?> cls = Class.forName("com.mysql.cj.jdbc.AbandonedConnectionCleanupThread");

            java.lang.reflect.Method method = cls.getMethod("checkedShutdown");

            method.invoke(null);

            System.out.println("   [CLEANUP] MySQL Cleanup Thread fermato (Reflection).");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("   [WARNING] Impossibile fermare il thread MySQL: " + e.getMessage());
        }
        System.out.println("--------------------------------------------------");
    }
}