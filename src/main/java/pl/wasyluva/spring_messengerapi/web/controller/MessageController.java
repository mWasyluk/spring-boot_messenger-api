package pl.wasyluva.spring_messengerapi.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wasyluva.spring_messengerapi.data.repository.AccountRepository;
import pl.wasyluva.spring_messengerapi.data.service.MessageService;
import pl.wasyluva.spring_messengerapi.domain.message.Message;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/messages")
public class MessageController {
    // TODO: Find messages only for an actual Principal
    // TODO: Find messages by a targetUserId as a request parameter

    private final MessageService messageService;
    private final AccountRepository accountRepository;
    private final PrincipalService principalService;

    public MessageController(MessageService messageService, AccountRepository accountRepository, PrincipalService principalService) {
        this.messageService = messageService;
        this.accountRepository = accountRepository;
        this.principalService = principalService;
    }

    @GetMapping
    public ResponseEntity<List<Message>> getAllMessages(){
        return messageService.getAllPersistedMessages();
    }

    @PostMapping("/send/{targetUserIdAsString}")
    public ResponseEntity<Message> sendMessageToUserByUserId(@PathVariable String targetUserIdAsString,
                                                             @RequestBody Message.TempMessage message){
        Message messageToPersist = new Message(principalService.getPrincipalProfileId(), UUID.fromString(targetUserIdAsString), message);
        messageToPersist.setSentDate(new Date());

        return messageService.saveMessage(messageToPersist);
    }

    @PatchMapping("/update")
    public ResponseEntity<Message> updateMessage(@RequestBody Message updatedMessage){
        return messageService.updateMessage(principalService.getPrincipalProfileId(), updatedMessage);
    }

    @DeleteMapping("/delete/{messageIdAsString}")
    public ResponseEntity<?> deleteMessage(@PathVariable String messageIdAsString){
        return messageService.deleteMessage(principalService.getPrincipalProfileId(), UUID.fromString(messageIdAsString));
    }

}
