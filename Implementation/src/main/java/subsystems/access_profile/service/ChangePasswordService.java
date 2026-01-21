package service; // ADATTATO: Deve essere qui per AppInitializer

import observer_pattern.PasswordChangeObserverInterface;
import observer_pattern.Subject;
import subsystems.access_profile.model.User;
import subsystems.access_profile.model.UserDAO;

import java.util.ArrayList;
import java.util.List;

public class ChangePasswordService implements Subject {

    private static ChangePasswordService instance = new ChangePasswordService();

    private ChangePasswordService() {}

    public static ChangePasswordService getInstance() {
        return instance;
    }

    private List<PasswordChangeObserverInterface> iscritti = new ArrayList<>();

    // --- METODI OBSERVER (Subject) ---

    @Override
    public void attach(PasswordChangeObserverInterface obs) {
        iscritti.add(obs);
    }

    @Override
    public void detach(PasswordChangeObserverInterface obs) {
        iscritti.remove(obs);
    }

    // --- ADATTAMENTO PER APPINITIALIZER ---
    // AppInitializer chiama addObserver, quindi facciamo un ponte verso attach
    public void addObserver(PasswordChangeObserverInterface obs) {
        this.attach(obs);
    }

    @Override
    public void notifyObservers(User user, String eventType) {
        for (PasswordChangeObserverInterface observer : iscritti) {
            observer.onPasswordUpdate(user, eventType);
        }
    }

    // --- LOGICA BUSINESS ---

    public void updatePassword(User user, String newHashedPassword) {
        // Nota: Assicurati che UserDAO abbia il metodo updatePassword o usa doSave
        UserDAO dao = new UserDAO();

        try {
            // Se UserDAO non ha updatePassword, dovrai aggiungerlo o usare una query diretta qui
            // Per ora mantengo la tua logica
            dao.updatePassword(user.getEmail(), newHashedPassword);

            user.setPassword(newHashedPassword);
            System.out.println("ProfileService: Password cambiata nel DB. Ora avviso gli observer...");

            notifyObservers(user, "PASSWORD_CHANGED");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Errore durante l'aggiornamento della password: " + e.getMessage());
        }
    }
}