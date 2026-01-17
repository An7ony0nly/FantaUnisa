package initializer;


import subsystems.access_profile.model.Role;
import subsystems.access_profile.model.User;
import subsystems.access_profile.model.UserDAO;
import utils.PasswordHasher;

public class DBPopulator {

    public static void ensureGestoreUtentiExists() {
        UserDAO userDAO = new UserDAO();

        String gestore_utentiEmail = "gestoreutenti@fantaunisa.it";

        // Controlliamo se esiste già
        if (userDAO.doRetrieveByEmail(gestore_utentiEmail) == null) {

            System.out.println("[DBPopulator] GESTORE UTENTI non trovato. Creazione in corso...");

            User gestore_utenti = new User();

            gestore_utenti.setNome("Super");
            gestore_utenti.setCognome("Admin");
            gestore_utenti.setEmail(gestore_utentiEmail);
            gestore_utenti.setPassword(PasswordHasher.hash("admin123"));
            gestore_utenti.setRole(Role.GESTORE_UTENTI);
            gestore_utenti.setIs_active(true);
            gestore_utenti.setVerificationToken(null);
            gestore_utenti.setResetToken(null);
            gestore_utenti.setResetExpiry(null);

            try {
                userDAO.doSave(gestore_utenti);
                System.out.println("[DBPopulator] Gestore Utenti creato con successo.");
            } catch (Exception e) {
                System.err.println("[DBPopulator] Errore creazione Gestore Utenti: " + e.getMessage());
                e.printStackTrace();
            }

        } else {
            System.out.println("[DBPopulator] Gestore Utenti già presente nel database.");
        }
    }
}