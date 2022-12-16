package pl.wasyluva.spring_messengerapi.web.http.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import pl.wasyluva.spring_messengerapi.data.service.ConversationService;
import pl.wasyluva.spring_messengerapi.data.service.MessageService;
import pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponse;
import pl.wasyluva.spring_messengerapi.domain.message.Message;
import pl.wasyluva.spring_messengerapi.web.http.support.PrincipalService;

import java.util.Date;

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
    private final SimpMessagingTemplate messagingTemplate;

    // TODO: Remove after tests
    @GetMapping
    public ResponseEntity<?> getAllMessages(){
        return messageService.getAllPersistedMessages().getResponseEntity();
    }

    // PostMapping ("/send/user/{id}
    @PostMapping("/send/conversation/{conversationIdAsString}")
    public ResponseEntity<?> addMessageToConversation(@PathVariable String conversationIdAsString,
                                                             @RequestBody Message.TempMessage message) throws JsonProcessingException {
        Message messageToPersist = new Message(principalService.getPrincipalProfileId(), message);
        messageToPersist.setSentDate(new Date());

        ServiceResponse<?> serviceResponse = conversationService.addMessageToConversationById(
                principalService.getPrincipalProfileId(),
                conversationIdAsString,
                messageToPersist);

        if (serviceResponse.getBody() instanceof Message) {
            Message persistentMessage = (Message) serviceResponse.getBody();
            ObjectMapper mapper = new ObjectMapper();
            messagingTemplate.convertAndSend(
                    "/topic/" + persistentMessage.getConversation().getId(),
                    mapper.writeValueAsString(persistentMessage));
        }

        return serviceResponse.getResponseEntity();
    }

    @GetMapping("/conversation/{conversationIdAsString}")
    public ResponseEntity<?> getMessagesByConversationId (@PathVariable String conversationIdAsString,
                                                          @RequestParam(name = "page", defaultValue = "0") Integer page){
        int pageSize = 15;

        return messageService.getAllMessagesByConversationId(
                principalService.getPrincipalProfileId(),
                conversationIdAsString,
                PageRequest.of(page, pageSize)
                        .withSort(Sort.by("sentDate").descending()))
                .getResponseEntity();
    }

    @PatchMapping("/update")
    public ResponseEntity<?> updateMessage(@RequestBody Message updatedMessage){
        return messageService.updateMessage(
                principalService.getPrincipalProfileId(),
                updatedMessage)
                .getResponseEntity();
    }

    @DeleteMapping("/delete/{messageIdAsString}")
    public ResponseEntity<?> deleteMessage(@PathVariable String messageIdAsString){
        return messageService.deleteMessage(
                principalService.getPrincipalProfileId(),
                messageIdAsString)
                .getResponseEntity();
    }
}
