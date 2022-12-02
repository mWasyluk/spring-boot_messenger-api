package pl.wasyluva.spring_messengerapi.data.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.core.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import pl.wasyluva.spring_messengerapi.data.repository.ConversationRepository;
import pl.wasyluva.spring_messengerapi.data.repository.MessageRepository;
import pl.wasyluva.spring_messengerapi.data.repository.ProfileRepository;
import pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponse;
import pl.wasyluva.spring_messengerapi.domain.message.Conversation;
import pl.wasyluva.spring_messengerapi.domain.message.Message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponseMessages.*;

@RequiredArgsConstructor

@Service
@Slf4j
public class MessageService {
    private final MessageRepository messageRepository;
    private final ProfileRepository profileRepository;
    private final ConversationRepository conversationRepository;

    public ServiceResponse<List<Message>> getAllPersistedMessages(){
        return new ServiceResponse<>(
                messageRepository.findAll(),
                HttpStatus.OK,
                OK);
    }

    public ServiceResponse<Message> updateMessage(@NonNull UUID requestingUserId, @NonNull Message updatedMessage){
        if (updatedMessage.getId() == null){
            log.debug("Message provided as updated has to have an ID");

            return new ServiceResponse<>(
                    null,
                    HttpStatus.BAD_REQUEST,
                    ID_REQUIRED);
        }

        Optional<Message> optionalPersistedMessage = messageRepository.findById(updatedMessage.getId());

        if (!optionalPersistedMessage.isPresent()) {
            log.debug("Message with ID " + updatedMessage.getId() + " does not exist");

            return new ServiceResponse<>(
                    null,
                    HttpStatus.NOT_FOUND,
                    EXISTING_ID_REQUIRED);
        }

        Message persistedMessage = optionalPersistedMessage.get();

        if (!persistedMessage.getSourceUserId().equals(requestingUserId)){
            log.debug("Requesting User does not have permission to update the message");

            return new ServiceResponse<>(
                    null,
                    HttpStatus.UNAUTHORIZED,
                    UNAUTHORIZED);
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
                HttpStatus.OK,
                OK);
    }

    public ServiceResponse<Message> deleteMessage(@NonNull UUID requestingUserId, @NonNull UUID deletingMessageId){
        Optional<Message> optionalPersistedMessage = messageRepository.findById(deletingMessageId);

        if (!optionalPersistedMessage.isPresent()) {
            log.debug("Message with ID " + deletingMessageId + " does not exist");

            return new ServiceResponse<>(
                    null,
                    HttpStatus.NOT_FOUND,
                    EXISTING_ID_REQUIRED);
        }

        Message persistedMessage = optionalPersistedMessage.get();

        if (!persistedMessage.getSourceUserId().equals(requestingUserId)){
            log.debug("Requesting User does not have permission to delete the message");

            return new ServiceResponse<>(
                    null,
                    HttpStatus.UNAUTHORIZED,
                    UNAUTHORIZED);
        }

        if (persistedMessage.getConversation() != null) {
            Optional<Conversation> conversationByMessage = conversationRepository.findById(persistedMessage.getConversation().getId());
            if (conversationByMessage.isPresent()) {
                conversationByMessage.get().removeMessageById(deletingMessageId);
                conversationRepository.save(conversationByMessage.get());
                log.debug("The Message entity has been removed from its Conversation");
            }
            else conversationRepository.deleteById(null);
        }

        messageRepository.deleteById(deletingMessageId);

        return new ServiceResponse<>(
                null,
                HttpStatus.OK,
                OK);
    }

    public ServiceResponse<?> getAllByConversationId(UUID conversationId, Pageable pageable) {
        Optional<Conversation> optionalConversation = conversationRepository.findById(conversationId);
        if (!optionalConversation.isPresent()){
            log.debug("Provided conversationId does not exist in the database");
            return new ServiceResponse<>(
                    null,
                    HttpStatus.BAD_REQUEST,
                    EXISTING_ID_REQUIRED);
        }

        List<Message> conversationMessages = messageRepository.findAllByConversationId(conversationId, pageable);
        return new ServiceResponse<>(
                conversationMessages,
                HttpStatus.OK,
                OK);
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
