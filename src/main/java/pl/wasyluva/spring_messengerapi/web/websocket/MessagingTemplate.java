package pl.wasyluva.spring_messengerapi.web.websocket;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import pl.wasyluva.spring_messengerapi.data.repository.ConversationRepository;
import pl.wasyluva.spring_messengerapi.domain.message.Conversation;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Profile;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class MessagingTemplate {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ConversationRepository conversationRepository;

    public void sendMessageToAll(@NonNull Collection<UUID> collection, @NonNull String message){
        for (UUID id: collection){
            simpMessagingTemplate.convertAndSend("/topic/messages/" + id, message);
        }
    }

    public void sendMessageToAllConversationParticipators(@NonNull UUID conversationId, @NonNull String message){
        Optional<Conversation> byId = conversationRepository.findById(conversationId);
        if (byId.isPresent()){
            List<UUID> participatorsIds = byId.get().getParticipators().stream().map(Profile::getId).collect(Collectors.toList());
            sendMessageToAll(participatorsIds, message);
        }
    }

    public void sendConversationToAll(@NonNull Collection<UUID> collection, @NonNull String conversation){
        for (UUID id: collection){
            simpMessagingTemplate.convertAndSend("/topic/conversations/" + id, conversation);
        }
    }
}
