package pl.wasyluva.spring_messengerapi.data.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.wasyluva.spring_messengerapi.data.repository.MessageRepository;
import pl.wasyluva.spring_messengerapi.data.repository.UserProfileRepository;
import pl.wasyluva.spring_messengerapi.domain.message.Message;
import pl.wasyluva.spring_messengerapi.domain.userdetails.UserProfile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private UserProfileRepository userProfileRepository;

    public ResponseEntity<List<Message>> getAllPersistedMessages(){
        return new ResponseEntity<>(messageRepository.findAll(), HttpStatus.OK);
    }

    public ResponseEntity<Message> saveMessage(@NonNull Message messageToSave){
        if (messageToSave.getId() != null){
            Optional<Message> messageById = messageRepository.findById(messageToSave.getId());
            if (messageById.isPresent()) {
                log.debug("Message with ID " + messageToSave.getId() + " already exists");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        Optional<UserProfile> targetUserById = userProfileRepository.findById(messageToSave.getTargetUserId());
        if (!targetUserById.isPresent()) {
            log.debug("User with ID " + messageToSave.getTargetUserId() + " does not exist");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Message persistedMessage = messageRepository.save(messageToSave);
        log.debug("Message saved");
        return new ResponseEntity<>(persistedMessage, HttpStatus.OK);
    }

    public ResponseEntity<Message> updateMessage(@NonNull UUID requestingUserId, @NonNull Message updatedMessage){
        if (updatedMessage.getId() == null){
            log.debug("Message provided as updated has to have an ID");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<Message> optionalPersistedMessage = messageRepository.findById(updatedMessage.getId());

        if (!optionalPersistedMessage.isPresent()) {
            log.debug("Message with ID " + updatedMessage.getId() + " does not exist");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Message persistedMessage = optionalPersistedMessage.get();

        if (!persistedMessage.getSourceUserId().equals(requestingUserId)){
            log.debug("Requesting User does not have permission to update the message");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
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
        return new ResponseEntity<>(updatedPersistedMessage, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteMessage(@NonNull UUID requestingUserId, @NonNull UUID deletingMessageId){
        Optional<Message> optionalPersistedMessage = messageRepository.findById(deletingMessageId);

        if (!optionalPersistedMessage.isPresent()) {
            log.debug("Message with ID " + deletingMessageId + " does not exist");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Message persistedMessage = optionalPersistedMessage.get();

        if (!persistedMessage.getSourceUserId().equals(requestingUserId)){
            log.debug("Requesting User does not have permission to delete the message");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        messageRepository.deleteById(deletingMessageId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
