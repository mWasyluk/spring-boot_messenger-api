package pl.wasyluva.spring_messengerapi.util;

import java.util.regex.Pattern;

public class BcryptUtils {
    public static final Pattern BCRYPT_PATTERN = Pattern.compile("\\A\\$2a?\\$\\d\\d\\$[./0-9A-Za-z]{53}");
}
