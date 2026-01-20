package subsystems.access_profile.service;

import observer_pattern.PasswordChangeObserverInterface;
import observer_pattern.Subject;
import subsystems.access_profile.model.User;
import subsystems.access_profile.model.UserDAO;

import java.util.ArrayList;
import java.util.List;
/*+*/
public class ChangePasswordService implements Subject {

    private static ChangePasswordService instance = new ChangePasswordService();
    private ChangePasswordService() {}
    public static ChangePasswordService getInstance() { return instance; }

    private List<PasswordChangeObserverInterface> iscritti = new ArrayList<>();

    @Override
    public void attach(PasswordChangeObserverInterface obs) {
        iscritti.add(obs);
    }

    @Override
    public void detach(PasswordChangeObserverInterface obs) {
        iscritti.remove(obs);
    }

    @Override
    public void notifyObservers(User user, String eventType) {
        for (PasswordChangeObserverInterface observer : iscritti) {
            observer.onPasswordUpdate(user, eventType);
        }
    }

    public void updatePassword(User user, String newHashedPassword) {

        UserDAO dao = new UserDAO();
        dao.updatePassword(user.getEmail(), newHashedPassword);

        user.setPassword(newHashedPassword);

        System.out.println("ProfileService: Password cambiata nel DB. Ora avviso gli observer...");

        notifyObservers(user, "PASSWORD_CHANGED");
    }
}