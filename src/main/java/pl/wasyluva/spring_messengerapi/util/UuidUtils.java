package pl.wasyluva.spring_messengerapi.util;

import java.util.UUID;

public class UuidUtils {
    public static final String INVALID_UUID_AS_STRING = "8ae5fbf0-7881-a1eb-0242ac120002";

    public static boolean isStringCorrectUuid(String toCheck){
        try {
            UUID uuid = UUID.fromString(toCheck);
        } catch (IllegalArgumentException exception) {
            return false;
        }
        return true;
    }
}
