package observer_pattern;

import subsystems.access_profile.model.User;

public class LogObserver implements PasswordChangeObserverInterface {

    @Override
    public void onPasswordUpdate(User user, String eventType) {
        System.out.println("--- LOG OBSERVER ---");
        System.out.println("Scrittura log: L'utente " + user.getEmail() + " ha modificato la password alle ore " + java.time.LocalTime.now());
    }
}
