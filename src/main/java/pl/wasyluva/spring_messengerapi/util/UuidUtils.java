package pl.wasyluva.spring_messengerapi.util;

import java.util.UUID;

public class UuidUtils {
    public static boolean isStringCorrectUuid(String toCheck){
        try {
            UUID uuid = UUID.fromString(toCheck);
        } catch (IllegalArgumentException exception) {
            return false;
        }
        return true;
    }
}
