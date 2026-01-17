package observer_pattern;

import subsystems.access_profile.model.User;

public interface PasswordChangeObserverInterface {
        void onPasswordUpdate(User user, String eventType);
}
