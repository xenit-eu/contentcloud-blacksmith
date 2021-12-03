package eu.xenit.contentcloud.blacksmith.util.exceptions;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ExceptionUtils {

    public static String[] collectCauses(Throwable throwable) {
        var summary = new ArrayList<String>();
        collectCauses(throwable.getCause(), summary);
        return summary.toArray(new String[0]);
    }

    private static void collectCauses(Throwable throwable, @NonNull List<String> summary) {
        if (throwable == null) {
            return;
        }

        summary.add(throwable.getClass().getSimpleName() + ": " + throwable.getMessage());

        var cause = throwable.getCause();
        if (cause != null && cause != throwable) {
            collectCauses(cause, summary);
        }
    }
}
