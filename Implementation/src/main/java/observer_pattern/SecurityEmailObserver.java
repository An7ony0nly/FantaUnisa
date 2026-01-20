package observer_pattern;

import subsystems.access_profile.model.User;
import utils.EmailSender;
/*+*/
public class SecurityEmailObserver implements PasswordChangeObserverInterface {

    @Override
    public void onPasswordUpdate(User user, String eventType) {

        if ("PASSWORD_CHANGED".equals(eventType)) {

            System.out.println("[Observer Email] Rilevato cambio password per: " + user.getEmail());

            new Thread(() -> {
                EmailSender.sendSecurityAlert(user.getEmail());
            }).start();
        }
    }
}
