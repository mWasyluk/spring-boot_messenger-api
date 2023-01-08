package pl.wasyluva.spring_messengerapi.data.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.wasyluva.spring_messengerapi.data.repository.MessageRepository;
import pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponse;
import pl.wasyluva.spring_messengerapi.domain.message.Conversation;
import pl.wasyluva.spring_messengerapi.domain.message.Message;
import pl.wasyluva.spring_messengerapi.util.UuidUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor

@Service
@Slf4j
public class MessageService {
    private final MessageRepository messageRepository;
    private final ConversationService conversationService;

    public ServiceResponse<?> getAllPersistedMessages(){
        return new ServiceResponse<>(
                messageRepository.findAll(),
                HttpStatus.OK);
    }

    public ServiceResponse<?> updateMessage(@NonNull UUID requestingProfileId, @NonNull Message updatedMessage){
        if (updatedMessage.getId() == null){
            log.debug("Message provided as updated has to have an ID");
            return ServiceResponse.INCORRECT_ID;
        }

        Optional<Message> optionalPersistedMessage = messageRepository.findById(updatedMessage.getId());

        if (!optionalPersistedMessage.isPresent()) {
            log.debug("Message with ID " + updatedMessage.getId() + " does not exist");
            return ServiceResponse.INCORRECT_ID;
        }

        Message persistedMessage = optionalPersistedMessage.get();

        if (!persistedMessage.getSourceUserId().equals(requestingProfileId)){
            log.debug("Requesting User does not have permission to update the message");
            return ServiceResponse.UNAUTHORIZED;
        }

        if (updatedMessage.getContent() != null)
            persistedMessage.setContent(updatedMessage.getContent());
        if (updatedMessage.getSentDate() != null && persistedMessage.getSentDate() == null)
            persistedMessage.setSentDate(updatedMessage.getSentDate());
        if (updatedMessage.getDeliveryDate() != null && persistedMessage.getDeliveryDate() == null)
            persistedMessage.setDeliveryDate(updatedMessage.getDeliveryDate());
        if (updatedMessage.getReadDate() != null && persistedMessage.getReadDate() == null)
            persistedMessage.setReadDate(updatedMessage.getReadDate());

        Message updatedPersistedMessage = messageRepository.save(persistedMessage);
        log.debug("Message updated");

        return new ServiceResponse<>(
                updatedPersistedMessage,
                HttpStatus.OK);
    }

    public ServiceResponse<?> deleteMessage(@NonNull UUID requestingProfileUuid, @NonNull UUID messageUuid){
        Optional<Message> byId = messageRepository.findById(messageUuid);
        if (!byId.isPresent()){
            return ServiceResponse.INCORRECT_ID;
        }

        Message message = byId.get();

        if (!message.getSourceUserId().equals(requestingProfileUuid)){
            return ServiceResponse.UNAUTHORIZED;
        }

        ServiceResponse<?> conversationServiceResponse = conversationService.getById(requestingProfileUuid, message.getConversation().getId());
        if (!(conversationServiceResponse.getBody() instanceof Conversation)) {
            return conversationServiceResponse;
        }

        Conversation conversation = (Conversation) conversationServiceResponse.getBody();
        conversation.removeMessageById(messageUuid);

        messageRepository.deleteById(messageUuid);
        return ServiceResponse.OK;
    }

    public ServiceResponse<?> deleteMessage(@NonNull UUID requestingUserId, @NonNull String messageUuid){
        if (!UuidUtils.isStringCorrectUuid(messageUuid)){
            return ServiceResponse.INCORRECT_ID;
        }
        return deleteMessage(requestingUserId, UUID.fromString(messageUuid));

    }

    // TODO: Test pageable
    public ServiceResponse<?> getAllMessagesByConversationId(@NonNull UUID requestingProfileId, @NonNull UUID conversationId, @NonNull Pageable pageable) {
        ServiceResponse<?> conversationServiceResponse = conversationService.getById(requestingProfileId, conversationId);
        if (!(conversationServiceResponse.getBody() instanceof Conversation)){
            return conversationServiceResponse;
        }

        List<Message> messages = messageRepository.findAllByConversationIdOrderBySentDateDesc(conversationId, pageable);

        return new ServiceResponse<>(
                messages,
                HttpStatus.OK);
    }

    public ServiceResponse<?> getAllMessagesByConversationId(@NonNull UUID requestingProfileId, @NonNull String conversationStringUuid, @NonNull Pageable pageable) {
        if (!UuidUtils.isStringCorrectUuid(conversationStringUuid)){
            return ServiceResponse.INCORRECT_ID;
        }
        return getAllMessagesByConversationId(requestingProfileId, UUID.fromString(conversationStringUuid), pageable);
    }

//    public ServiceResponse<?> getLatestMessageWithLatestContacts(@NonNull UUID forProfileId, int howManyProfiles, int profileOffset){
//        if (howManyProfiles < 1 || profileOffset < 1){
//            log.debug("Profile range is incorrect");
//            return new ServiceResponse<>(
//                    Arrays.asList(howManyProfiles, profileOffset),
//                    HttpStatus.BAD_REQUEST,
//                    INCORRECT_RANGE);
//        }
//
//        Optional<Profile> byId = profileRepository.findById(forProfileId);
//        if (!byId.isPresent()){
//            log.debug("User with ID " + forProfileId + " does not exist");
//
//            return new ServiceResponse<>(
//                    forProfileId,
//                    HttpStatus.BAD_REQUEST,
//                    TARGET_USER_DOES_NOT_EXIST);
//        }
//
////        return new ServiceResponse<>(
////                forProfileId,
////                HttpStatus.BAD_REQUEST,
////                TARGET_USER_DOES_NOT_EXIST);
////        return messageRepository.findAllByProfileId(forProfileId);
//    }
}
