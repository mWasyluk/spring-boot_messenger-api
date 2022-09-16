package pl.wasyluva.spring_messengerapi.domain.userdetails;

import com.sun.istack.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.wasyluva.spring_messengerapi.domain.message.Message;
import pl.wasyluva.spring_messengerapi.domain.message.MessageState;

import javax.persistence.*;
import java.util.*;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor

@Entity
@Table(name = "user_profiles")
public class UserProfile implements ChatUser {

    @Id
//    @GeneratedValue(generator = "user_id_generator")
//    @GenericGenerator(name = "user_id_generator", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    private String firstName;

    private String lastName;

    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "avatar_id", referencedColumnName = "id", nullable = false)
    private UserAvatar avatar = new UserAvatar("http://localhost:8080/messenger/api/images/avatars/default");

    @Temporal(TemporalType.DATE)
    private Date birthDate;

    @ManyToMany(targetEntity = pl.wasyluva.spring_messengerapi.domain.message.Message.class) //TODO
    private Set<Message> messages = new TreeSet<>();

    public UserProfile(UUID userDetailsId, String firstName, String lastName, Date birthDate) {
        this.id = userDetailsId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
    }

    public UserAvatar getAvatar() {
        if (this.avatar == null)
            return null; //TODO: return a default avatar if no one set.
        return avatar;
    }

    @Override
    public void addUserMessage(Message message) {
        if (message != null && message.getId() != null)
            messages.add(message);
    }

    @Override
    public void removeUserMessageByMessageId(UUID messageId) {
        messages.removeIf(next -> next.getId() == id);
    }

    @Override
    public Optional<TreeSet<Message>> getMessagesByUserId(@NotNull UUID userId) {
        TreeSet<Message> messagesWithUser = new TreeSet<>();
        for (Message next : messages) {
            if (next.getTargetUserId().equals(userId) || next.getSourceUserId().equals(userId)) messagesWithUser.add(next);
        }
        return !messagesWithUser.isEmpty() ? Optional.of(messagesWithUser) : Optional.empty();
    }

    @Override
    public Optional<MessageState> getMessageStateByMessageId(UUID messageId) {
        Optional<Message> messageById = findMessageById(messageId);
        return messageById.isPresent() ? Optional.of(messageById.get().getMessageState()) : Optional.empty() ;
    }

    private Optional<Message> findMessageById(UUID id){
        for (Message next : messages) {
            if (next.getId() == id) return Optional.of(next);
        }
        return Optional.empty();
    }
}
