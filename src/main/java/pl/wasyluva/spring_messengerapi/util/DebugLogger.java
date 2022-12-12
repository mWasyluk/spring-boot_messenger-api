package pl.wasyluva.spring_messengerapi.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DebugLogger {
    public static void logUnauthorizedProfile(String id){
        String message = String.format("The requesting Profile with id '%s' does not have permission for this operation", id);
        log.debug(message);
    }

    public static void logInvalidUuidAsString(String id){
        String message = String.format("String '%s' is not a valid UUID", id);
        log.debug(message);
    }

    public static void logObjectNotFound(String id){
        String message = String.format("Object with id '%s' could not be found", id);
        log.debug(message);
    }

    public static void logSuccess() {
        log.debug("The operation has been completed successfully");
    }
}
