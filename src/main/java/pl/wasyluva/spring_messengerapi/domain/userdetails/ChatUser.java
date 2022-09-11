package pl.wasyluva.spring_messengerapi.domain.userdetails;

import pl.wasyluva.spring_messengerapi.domain.message.Message;
import pl.wasyluva.spring_messengerapi.domain.message.MessageState;

import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;

public interface ChatUser {
    void addUserMessage(Message message);
    void removeUserMessageByMessageId(UUID messageId);

    Optional<TreeSet<Message>> getMessagesByUserId(UUID userId);
    Optional<MessageState> getMessageStateByMessageId(UUID messageId);
}
