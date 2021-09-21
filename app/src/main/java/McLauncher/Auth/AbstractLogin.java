package McLauncher.Auth;

import McLauncher.Utils.Exception.TokenRefreshException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public abstract class AbstractLogin {
    protected Account account;
    protected Exception exception;
    protected String username, password;
    protected LoginEventListener onLoginSuccess;
    protected LoginEventListener onBadCredentials;
    protected LoginEventListener onConnectionError;
    protected LoginEventListener onLoginCancel;
    protected Logger logger = LogManager.getLogger();

    public void login(String username, String password) {
        this.username = username;
        this.password = password;
        new Thread(this::loginThreadMethod).start();
    }

    ;

    public abstract Account refreshToken(Account account) throws IOException, TokenRefreshException;

    /**
     * This method is the actual job for login !
     */
    protected abstract void loginThreadMethod();

    protected void loginSuccess() {
        onLoginSuccess.eventReceived(this);
    }

    protected void badCredentials(Exception e) {
        logger.catching(e);
        this.exception = e;
        onBadCredentials.eventReceived(this);
    }

    protected void connectionError(Exception e) {
        logger.catching(e);
        this.exception = e;
        onConnectionError.eventReceived(this);
    }

    protected void loginCancel() {
        onLoginCancel.eventReceived(this);
    }

    public void setOnLoginSuccess(LoginEventListener onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
    }

    public void setOnBadCredentials(LoginEventListener onBadCredentials) {
        this.onBadCredentials = onBadCredentials;
    }

    public void setOnConnectionError(LoginEventListener onConnectionError) {
        this.onConnectionError = onConnectionError;
    }

    public void setOnLoginCancel(LoginEventListener onLoginCancel) {
        this.onLoginCancel = onLoginCancel;
    }

    public Account getAccount() {
        return account;
    }

    public Exception getException() {
        return exception;
    }

}
