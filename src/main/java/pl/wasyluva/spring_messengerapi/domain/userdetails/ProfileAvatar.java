package pl.wasyluva.spring_messengerapi.domain.userdetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor

@Slf4j
@Entity
@Table(name = "avatars")
public class ProfileAvatar {
    public static final List<String> SUPPORTED_MEDIA_TYPES_VALUES = Arrays.asList(MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE);

    @Id
    private UUID id = UUID.randomUUID();

    @JsonIgnore
    private MediaType mediaType;

    @JsonIgnore
    private byte[] bytesArray;

    public ProfileAvatar(byte[] bytesArray, MediaType mediaType){
        this.bytesArray = bytesArray;
        this.mediaType = mediaType;
    }

    public static class DefaultProfileAvatar{
        private final String id = "default";
    }
}
