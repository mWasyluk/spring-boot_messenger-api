package pl.wasyluva.spring_messengerapi.domain.message;

import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "messages")
public class Message implements Comparable<Message> {
    // TODO: Persist messages in Document database (MongoDB)

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "messages_uuid_generator")
    @GenericGenerator(name = "messages_uuid_generator", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    private UUID sourceUserId;

    private UUID targetUserId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date sentDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date deliveryDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date readDate;

    @NotNull
    private String content;

    public Message(UUID sourceUserId, UUID targetUserId, TempMessage tempMessage) {
        this.sourceUserId = sourceUserId;
        this.targetUserId = targetUserId;
        this.content = tempMessage.getContent();
    }

    public MessageState getMessageState(){
        return  sentDate == null ? MessageState.NOT_SENT :
                deliveryDate == null ? MessageState.NOT_DELIVERED :
                readDate == null ? MessageState.NOT_READ :
                MessageState.READ;
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
        return Objects.equals(id, message.id) && Objects.equals(sourceUserId, message.sourceUserId) && Objects.equals(targetUserId, message.targetUserId) && Objects.equals(content, message.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sourceUserId, targetUserId, content);
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", sourceUserId=" + sourceUserId +
                ", targetUserId=" + targetUserId +
                ", sentDate=" + sentDate +
                ", deliveryDate=" + deliveryDate +
                ", readDate=" + readDate +
                ", content='" + content + '\'' +
                '}';
    }

    @Data
    @NoArgsConstructor
    public static class TempMessage{
        private String content;

        public TempMessage(String content) {
            this.content = content;
        }
    }
}

