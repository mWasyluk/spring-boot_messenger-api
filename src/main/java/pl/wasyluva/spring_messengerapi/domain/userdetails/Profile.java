package pl.wasyluva.spring_messengerapi.domain.userdetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.datetime.DateFormatter;

import javax.persistence.*;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor

@Entity
// TODO: Change to "profiles"
@Table(name = "user_profiles")
public class Profile {
    private static DateFormatter dateFormatter = new DateFormatter("dd-MM-yyyy");
    public static DateFormatter getBirthDateFormatter(){
        return dateFormatter;
    }

    @Id
    private UUID id = UUID.randomUUID();
    private String firstName;
    private String lastName;

    @OneToOne(mappedBy = "profile")
    @JsonIgnore
    private Account account;

    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "avatar_id", referencedColumnName = "id")
    private ProfileAvatar avatar = null;

    @Temporal(TemporalType.DATE)
    private Date birthDate;

    public Profile(String firstName, String lastName, Date birthDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
    }

    public ProfileAvatar getAvatar() {
        if (this.avatar == null)
            return ProfileAvatar.DEFAULT_AVATAR;
        return avatar;
    }

    public String getBirthDate(){
        return dateFormatter.print(birthDate, Locale.getDefault()).toString();
    }

    public void setBirthDate(String ddmmyyyy){
        String[] split = ddmmyyyy.split("-");
        try {
            dateFormatter.parse(split[0] + "-" + split[1] + "-" + split[2], Locale.getDefault());
        } catch (ParseException e) {
            System.err.println("Parsing string to date failed. String: " + ddmmyyyy);
        }
    }
}
