package pl.wasyluva.spring_messengerapi.domain.userdetails;

import org.springframework.security.core.GrantedAuthority;

import javax.persistence.Entity;
import javax.persistence.Id;

public enum UserAuthority implements GrantedAuthority {
    ADMIN("ADMIN"), USER("USER");

    private final String authority;

    UserAuthority(String authority) {
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return this.authority;
    }
}
