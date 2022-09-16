package pl.wasyluva.spring_messengerapi.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.wasyluva.spring_messengerapi.data.repository.MessageRepository;
import pl.wasyluva.spring_messengerapi.data.repository.UserProfileRepository;
import pl.wasyluva.spring_messengerapi.domain.message.Message;
import pl.wasyluva.spring_messengerapi.domain.userdetails.UserDetails;
import pl.wasyluva.spring_messengerapi.domain.userdetails.UserProfile;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j

@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private MessageRepository messageRepository;

    @GetMapping
    public ResponseEntity<List<Message>> getAllMessages(){
        return new ResponseEntity<>(messageRepository.findAll(), HttpStatus.OK);
    }

    @PostMapping("/send/{targetUserIdAsString}")
    public ResponseEntity<Message> sendMessageToUserByUserId(Authentication authentication, @PathVariable String targetUserIdAsString, @RequestBody Message.TempMessage message){

        UUID sourceUserId = ((UserDetails) authentication.getPrincipal()).getId();
        UUID targetUserId = UUID.fromString(targetUserIdAsString);

        Optional<UserProfile> userById = userProfileRepository.findById(targetUserId);
        if (!userById.isPresent()) {
            log.error("Message sending failure: " +
                    "\nTarget user with id " + targetUserId + " does not exist.");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Message messageToPersist = new Message(sourceUserId, targetUserId, message);
        messageToPersist.setSentDate(new Date());
        Message savedMessage = messageRepository.save(messageToPersist);

        log.info("New message persisted: " +
                "\n" + savedMessage);

        return new ResponseEntity<>(savedMessage, HttpStatus.OK);
    }

    @PatchMapping("/update")
    /* TODO: Create a MessageChange domain that contains information about the type of change and the change itself
*            Swap the class required by the method below with the new domain class  */
    public ResponseEntity<Message> updateMessage(Authentication authentication, @RequestBody Message messageUpdate){
        log.info("Received updated message:" +
                "\n" + messageUpdate);

        UUID sourceUserId = ((UserDetails) authentication.getPrincipal()).getId();

        Optional<Message> optionalMessage = messageRepository.findById(messageUpdate.getId());

        if (optionalMessage.isPresent()){

            Message messageById = optionalMessage.get();

            if (!sourceUserId.equals(messageById.getSourceUserId())) {
                log.error("Message updating failure: \n" +
                        "Message source user id " + messageById.getSourceUserId() + " is different than logged in user id " + sourceUserId);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            if (messageUpdate.getContent() != null) messageById.setContent(messageUpdate.getContent());
            if (messageUpdate.getSentDate() != null && messageById.getSentDate() == null) messageById.setSentDate(messageUpdate.getSentDate());
            if (messageUpdate.getDeliveryDate() != null && messageById.getDeliveryDate() == null) messageById.setDeliveryDate(messageUpdate.getDeliveryDate());
            if (messageUpdate.getReadDate() != null && messageById.getReadDate() == null) messageById.setReadDate(messageUpdate.getReadDate());

            Message savedMessage = messageRepository.save(messageById);

            return new ResponseEntity<>(savedMessage, HttpStatus.OK);
        }
        log.error("Message updating failure: \n" +
                "Message with id " + messageUpdate.getId() + " does not exist.");
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // TODO: Find messages only for an actual Principal + Find messages by a second User ID as a request parameter
}
