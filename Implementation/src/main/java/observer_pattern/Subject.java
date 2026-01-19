package observer_pattern;

import subsystems.access_profile.model.User;

public interface Subject {

    void attach(PasswordChangeObserverInterface observer);

    void detach(PasswordChangeObserverInterface observer);

    void notifyObservers(User user, String eventType);
}