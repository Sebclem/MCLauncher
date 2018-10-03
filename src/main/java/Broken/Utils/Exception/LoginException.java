package Broken.Utils.Exception;

public class LoginException extends Exception{
    public LoginException(String s) {



        super(s);
    }
    @Override
    public String getMessage() {
        String message = super.getMessage();
        if(message.contains("errorMessage")){
            message = message.substring(message.indexOf("errorMessage"));
            message = message.substring(message.indexOf(":")+1);
            message = message.replaceAll("\"","");
            message = message.replaceAll("}", "");
        }
        return message;
    }
}
