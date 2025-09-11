package no.vebb.f1.controller.admin;

import java.util.Optional;

public class LogType {
    private final String type;

    public static Optional<LogType> getLogType(String type) {
        LogType logType = new LogType(type);
        if (type == null || !logType.isValidLogType()) {
            return Optional.empty();
        }
        return Optional.of(logType);
    }

    private LogType (String type) {
        this.type = type;
    }

    private boolean isValidLogType() {
        return type.matches("error|info|importer");
    }

    public String type() {
        return type;
    }
}