package pl.wasyluva.spring_messengerapi.domain.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Profile;

import javax.persistence.*;
import java.util.*;

@Data
@NoArgsConstructor

@Entity
@Table(name = "conversations")
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "conversation_uuid_generator")
    @GenericGenerator(name = "conversation_uuid_generator", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;
    @OneToMany(cascade = {CascadeType.ALL})
    @ElementCollection(targetClass = Message.class)
    @JoinTable(name = "conversation_messages")
    @JsonIgnore
    private List<Message> messages;
    @ManyToMany
    @JoinTable(name = "conversation_participators")
    @JsonIgnoreProperties({"birthDate"})
    private List<Profile> participators;

    public Conversation(List<Profile> participators) {
        this.messages = new ArrayList<>();
        this.participators = new ArrayList<>(participators);
    }

    public boolean addMessage(Message message){
        message.setConversation(this);
        return this.messages.add(message);
    }

    public boolean removeMessageById(UUID messageId){
        Optional<Message> matchingMessage = this.messages.stream()
                .filter(message -> message.getId().equals(messageId)).findAny();
        matchingMessage.ifPresent(messages::remove);
        return messages.stream().noneMatch(message -> message.getId().equals(messageId));
    }

    public Set<Message> getLatestMessages(int quantity){
        Iterator<Message> iterator = this.messages.iterator();
        List<Message> latest = new LinkedList<>();

        while (iterator.hasNext() && latest.size() < quantity)
            latest.add(iterator.next());

        return new TreeSet<>(latest);
    }
//
//    public void setParticipators(Set<Profile> participators){
//        this.participators = new ArrayList<>(participators);
//    }

    // TODO: remove, add -Participator : Profile; removeAll, addAll -Participators : List<Profile>
}
