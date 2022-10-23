package pl.wasyluva.spring_messengerapi.domain.userdetails;

import com.sun.istack.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor

@Entity
@Table(name = "avatars")
public class ProfileAvatar {
    private static final String DEFAULT_AVATAR_URL = "https://example.com/messenger/api/images/avatars/default";
    public static final ProfileAvatar DEFAULT_AVATAR = new ProfileAvatar(DEFAULT_AVATAR_URL);
    @Id
//    @GeneratedValue(generator = "avatar_id_generator")
//    @GenericGenerator(name = "avatar_id_generator", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id = UUID.randomUUID();

    @NotNull
    private String imageUrl;

    public ProfileAvatar(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
