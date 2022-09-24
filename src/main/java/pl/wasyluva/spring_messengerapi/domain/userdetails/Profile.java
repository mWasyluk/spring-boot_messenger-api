package pl.wasyluva.spring_messengerapi.domain.userdetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor

@Entity
// TODO: Change to "profiles"
@Table(name = "user_profiles")
public class Profile {

    @Id
    private UUID id = UUID.randomUUID();
    private String firstName;
    private String lastName;

    @OneToOne(mappedBy = "profile")
    @JsonIgnore
    private Account account;

    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "avatar_id", referencedColumnName = "id", nullable = false)
    private ProfileAvatar avatar = ProfileAvatar.DEFAULT_AVATAR;

    @Temporal(TemporalType.DATE)
    private Date birthDate;

    public Profile(String firstName, String lastName, Date birthDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
    }

    public ProfileAvatar getAvatar() {
        if (this.avatar == null)
            return null; //TODO: return a default avatar if no one set.
        return avatar;
    }
}
