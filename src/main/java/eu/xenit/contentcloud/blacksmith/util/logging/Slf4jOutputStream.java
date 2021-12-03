package eu.xenit.contentcloud.blacksmith.util.logging;

import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.function.Consumer;


public class Slf4jOutputStream extends OutputStream {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(1024);

    private final Logger logger;
    private final LogLevel level;

    private final Consumer<String> logDelegate;

    public Slf4jOutputStream(Logger logger, LogLevel level) {
        this.logger = logger;
        this.level = level;
        this.logDelegate = logLevelDelegate(logger, level);
    }

    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR;
    }

    @Override
    public void write(int b) {
        if (b == '\n') {
            String line = buffer.toString();
            buffer.reset();
            this.logDelegate.accept(line);

        } else {
            buffer.write(b);
        }
    }


    private Consumer<String> logLevelDelegate(Logger logger, LogLevel level) {
        return line -> {
            switch (level) {
                case TRACE:
                    logger.trace(line);
                    break;
                case DEBUG:
                    logger.debug(line);
                    break;
                case ERROR:
                    logger.error(line);
                    break;
                case INFO:
                    logger.info(line);
                    break;
                case WARN:
                    logger.warn(line);
                    break;
            }
        };
    }

}
