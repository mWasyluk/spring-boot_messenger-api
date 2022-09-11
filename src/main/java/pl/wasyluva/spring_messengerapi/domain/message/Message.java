package pl.wasyluva.spring_messengerapi.domain.message;

import com.sun.istack.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import pl.wasyluva.spring_messengerapi.domain.userdetails.UserProfile;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor

@Entity
@Table(name = "messages")
public class Message implements Comparable<Message> {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "messages_uuid_generator")
    @GenericGenerator(name = "messages_uuid_generator", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @NotNull
    @ManyToOne //TODO
    private UserProfile sourceUser;

    @NotNull
    @ManyToOne //TODO
    private UserProfile targetUser;

    @Temporal(TemporalType.TIMESTAMP)
    private Date sentDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date deliveryDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date readDate;

    @NotNull
    private String content;

    public Message(UserProfile sourceUser, UserProfile targetUser, String content) {
        this.sourceUser = sourceUser;
        this.targetUser = targetUser;
        this.content = content;
    }

    public MessageState getMessageState(){
        return  sentDate == null ? MessageState.NOT_SENT :
                deliveryDate == null ? MessageState.NOT_DELIVERED :
                readDate == null ? MessageState.NOT_READ :
                MessageState.READ;
    }

    public void addNextDate(Date date) {
        MessageState messageState = this.getMessageState();

        if (messageState != null) {
            switch (messageState) {
                case NOT_DELIVERED: {
                    this.setDeliveryDate(date);
                    break;
                }
                case NOT_SENT: {
                    this.setSentDate(date);
                    break;
                }
                case NOT_READ: {
                    this.setReadDate(date);
                    break;
                }
            }
        }
    }

    @Override
    public int compareTo(Message o) {
        return this.getSentDate().compareTo(o.getSentDate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(id, message.id) && Objects.equals(sourceUser, message.sourceUser) && Objects.equals(targetUser, message.targetUser) && Objects.equals(content, message.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sourceUser, targetUser, content);
    }
}

