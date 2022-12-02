package pl.wasyluva.spring_messengerapi.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wasyluva.spring_messengerapi.data.service.ConversationService;
import pl.wasyluva.spring_messengerapi.data.service.MessageService;
import pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponse;
import pl.wasyluva.spring_messengerapi.domain.message.Conversation;
import pl.wasyluva.spring_messengerapi.domain.message.Message;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponseMessages.EXISTING_ID_REQUIRED;

@Slf4j
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {
    // TODO: Find messages only for an actual Principal
    // TODO: Find messages by a targetUserId as a request parameter

    private final MessageService messageService;
    private final ConversationService conversationService;
    private final PrincipalService principalService;

    @GetMapping
    public ResponseEntity<?> getAllMessages(){
        ServiceResponse<List<Message>> serviceResponse = messageService.getAllPersistedMessages();
        return new ResponseEntity<>(
                serviceResponse,
                serviceResponse.getHttpStatus());
    }

    // PostMapping ("/send/user/{id}
    @PostMapping("/send/conversation/{conversationIdAsString}")
    public ResponseEntity<?> addMessageToConversation(@PathVariable String conversationIdAsString,
                                                             @RequestBody Message.TempMessage message){
        ServiceResponse<Conversation> conversationServiceResponseById = conversationService.getById(UUID.fromString(conversationIdAsString));
        if (conversationServiceResponseById.getHttpStatusCode() != HttpStatus.OK.value() || conversationServiceResponseById.getObject() == null){
            return new ResponseEntity<>(
                    conversationServiceResponseById,
                    conversationServiceResponseById.getHttpStatus());
        }
        Conversation conversation = conversationServiceResponseById.getObject();
        boolean isParticipator = conversation.getParticipators().stream()
                .anyMatch(profile -> profile.getId().equals(principalService.getPrincipalProfileId()));
        if (!isParticipator){
            return new ResponseEntity<>(
                    null,
                    HttpStatus.UNAUTHORIZED);
        }

        Message messageToPersist = new Message(principalService.getPrincipalProfileId(), message);
        messageToPersist.setSentDate(new Date());

        ServiceResponse<?> serviceResponse = conversationService.addMessageToConversationById(
                conversation.getId(),
                messageToPersist);
        return new ResponseEntity<>(
                serviceResponse,
                serviceResponse.getHttpStatus());
    }

    @GetMapping("/conversation/{conversationIdAsString}")
    public ResponseEntity<?> getMessagesByConversationId (@PathVariable String conversationIdAsString,
                                                          @RequestParam(name = "page", defaultValue = "0") Integer page){

        int pageSize = 15;
        UUID conversationUuid = null;
        try {
            conversationUuid = UUID.fromString(conversationIdAsString);
        }catch (IllegalArgumentException e){
            return new ResponseEntity<>(
                    EXISTING_ID_REQUIRED,
                    HttpStatus.BAD_REQUEST);
        }
        ServiceResponse<?> conversationMessages = messageService.getAllByConversationId(
                conversationUuid,
                PageRequest.of(page, pageSize)
                        .withSort(Sort.by("sentDate").descending()));

        return new ResponseEntity<>(
                conversationMessages,
                conversationMessages.getHttpStatus());
    }

    @PatchMapping("/update")
    public ResponseEntity<?> updateMessage(@RequestBody Message updatedMessage){
        ServiceResponse<Message> messageServiceResponse = messageService.updateMessage(
                principalService.getPrincipalProfileId(),
                updatedMessage);
        return new ResponseEntity<>(
                messageServiceResponse,
                messageServiceResponse.getHttpStatus());
    }

    @DeleteMapping("/delete/{messageIdAsString}")
    public ResponseEntity<?> deleteMessage(@PathVariable String messageIdAsString){
        ServiceResponse<Message> messageServiceResponse = messageService.deleteMessage(
                principalService.getPrincipalProfileId(),
                UUID.fromString(messageIdAsString));
        return new ResponseEntity<>(
                messageServiceResponse,
                messageServiceResponse.getHttpStatus());
    }
}
