package initializer;

import subsystems.access_profile.model.Role;
import subsystems.access_profile.model.User;
import subsystems.access_profile.model.UserDAO;
import utils.PasswordHasher;
/*+*/
public class DBPopulator {

    public static void ensureGestoriExist() {
        UserDAO userDAO = new UserDAO();

        System.out.println("[DBPopulator] Verifica esistenza account amministrativi...");

        checkAndCreateUser(
                userDAO,
                "gestoreutenti@fantaunisa.it", // Email
                "gestore_utenti99",            // Username
                "admin123",                    // Password
                Role.GESTORE_UTENTI,           // Ruolo
                "Super",                       // Nome
                "Gestore"                      // Cognome
        );

        checkAndCreateUser(
                userDAO,
                "gestoredati@fantaunisa.it",   // Email
                "gestore_dati99",              // Username
                "stats123",                    // Password
                Role.GESTORE_DATI,             // Ruolo
                "Master",                      // Nome
                "Statistico"                   // Cognome
        );

        System.out.println("[DBPopulator] Verifica completata.");
    }

    /**
     * Metodo helper privato per evitare duplicazione di codice.
     * Controlla se un utente esiste tramite email, altrimenti lo crea.
     */
    private static void checkAndCreateUser(UserDAO userDAO, String email, String username, String password, Role role, String nome, String cognome) {

        // Controlliamo se esiste già nel DB
        if (userDAO.doRetrieveByEmail(email) == null) {

            System.out.println("[DBPopulator] " + role + " non trovato. Creazione in corso...");

            User newGestore = new User();
            newGestore.setNome(nome);
            newGestore.setCognome(cognome);
            newGestore.setEmail(email);
            newGestore.setUsername(username);
            newGestore.setPassword(PasswordHasher.hash(password)); // Hash della password
            newGestore.setRole(role);
            newGestore.setIs_active(true);
            newGestore.setVerificationToken(null);
            newGestore.setResetToken(null);
            newGestore.setResetExpiry(null);

            try {
                userDAO.doSave(newGestore);
                System.out.println("[DBPopulator] " + role + " (" + email + ") creato con successo.");
            } catch (Exception e) {
                System.err.println("[DBPopulator] Errore critico creazione " + role + ": " + e.getMessage());
                e.printStackTrace();
            }

        } else {
            System.out.println("[DBPopulator] " + role + " (" + email + ") già presente nel database.");
        }
    }
}