package eu.xenit.contentcloud.blacksmith.infra.build;

public class BuildFailedException extends RuntimeException {
    public BuildFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public BuildFailedException(String message) {
        super(message);
    }
}
