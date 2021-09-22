package McLauncher;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class MyLogEvent {

    private LocalDateTime time;
    private String level;
    private String className;
    private String Message;

    public MyLogEvent(long time, String level, String className, String message) {
        this.time = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDateTime();
        this.level = level;
        this.className = className;
        Message = message;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }
}
