package service;

import observer_pattern.PasswordChangeObserverInterface;
import subsystems.access_profile.model.User;
import subsystems.access_profile.model.UserDAO;

import java.util.ArrayList;
import java.util.List;

public class ChangePasswordService {

    private static ChangePasswordService instance = new ChangePasswordService();
    private ChangePasswordService() {}
    public static ChangePasswordService getInstance() { return instance; }

    private List<PasswordChangeObserverInterface> iscritti = new ArrayList<>();

    public void addObserver(PasswordChangeObserverInterface obs) {
        iscritti.add(obs);
    }

    public void detachObserver(PasswordChangeObserverInterface obs) {
        iscritti.remove(obs);
    }

    private void notifyObservers(User user, String eventType) {
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