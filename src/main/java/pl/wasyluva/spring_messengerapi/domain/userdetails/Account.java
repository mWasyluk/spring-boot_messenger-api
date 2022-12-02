package pl.wasyluva.spring_messengerapi.domain.userdetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.wasyluva.spring_messengerapi.domain.serializer.AccountDeserializer;

import javax.persistence.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

@Data
@NoArgsConstructor
@JsonDeserialize(using = AccountDeserializer.class)

@Entity
// TODO: Change to "accounts" and update the default query in AuthenticationProvider
@Table(name = "USERS")
public class Account implements UserDetails {
    @Id
    private UUID id = UUID.randomUUID();

    @Column(unique = true)
    private String email;
    private String password;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "profile_id", unique = true)
    @JsonIgnoreProperties("account")
    private Profile profile;

    @ElementCollection(targetClass = UserAuthority.class, fetch = FetchType.EAGER)
    @JoinTable(name = "AUTHORITIES", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "authority", nullable = false)
    @Enumerated(EnumType.STRING)
    private Collection<? extends GrantedAuthority> authorities;

    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;

    public Account(@NonNull String email, @NonNull String password) {
        this(email, password, Collections.singleton(UserAuthority.USER));
    }

    public Account(@NonNull String email, @NonNull String password, @NonNull Collection<? extends GrantedAuthority> authorities) {
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.accountNonExpired = true;
        this.accountNonLocked = true;
        this.credentialsNonExpired = true;
        this.enabled = true;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return this.email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return accountNonExpired == account.accountNonExpired && accountNonLocked == account.accountNonLocked && credentialsNonExpired == account.credentialsNonExpired && enabled == account.enabled && id.equals(account.id) && email.equals(account.email) && Objects.equals(authorities, account.authorities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, authorities, accountNonExpired, accountNonLocked, credentialsNonExpired, enabled);
    }
}
