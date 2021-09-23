package McLauncher;


import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * TextAreaAppender for Log4j 2
 */
@Plugin(
        name = "LogGuiAppender",
        category = Core.CATEGORY_NAME,
        elementType = Appender.ELEMENT_TYPE,
        printObject = true
)
public final class LogGuiAppender extends AbstractAppender {

    private static LogViewerController controller;


    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();


    protected LogGuiAppender(String name, Filter filter) {
        super(name, filter, null);
    }

    @PluginFactory
    public static LogGuiAppender createAppender(
            @PluginAttribute("name") String name, @PluginElement("Filter") final Filter filter) {
        return new LogGuiAppender(name, filter);
    }

    /**
     * Set TextArea to append
     *
     * @param controller TextArea to append
     */
    public static void setController(LogViewerController controller) {
        LogGuiAppender.controller = controller;
    }

    @Override
    public void append(LogEvent event) {
        readLock.lock();
        // append log text to TextArea
        try {
            String message;
            if(event.getThrown() == null)
                message = event.getMessage().getFormattedMessage();
            else{
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                event.getThrown().printStackTrace(pw);
                message = sw.toString();
            }
            MyLogEvent newEvent = new MyLogEvent(event.getInstant().getEpochMillisecond(), event.getLevel().name(), event.getLoggerName(), message);
            try {
                if (controller != null) {
                    controller.addData(newEvent);
                }
            } catch (final Throwable t) {
                error("Error while append to TextArea: "
                        + t.getMessage());
            }
        } catch (final IllegalStateException ex) {
            ex.printStackTrace();

        } finally {
            readLock.unlock();
        }
    }
}