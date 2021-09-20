package McLauncher.Utils.Event;

import McLauncher.Auth.AbstractLogin;

public class LoginEvent {
    public AbstractLogin loginMethod;

    public LoginEvent(AbstractLogin loginMethod) {
        this.loginMethod = loginMethod;
    }

    
}
