package pl.wasyluva.spring_messengerapi.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pl.wasyluva.spring_messengerapi.data.service.MessageService;
import pl.wasyluva.spring_messengerapi.domain.message.Message;
import pl.wasyluva.spring_messengerapi.domain.userdetails.UserDetails;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/messages")
public class MessageController {
    // TODO: Find messages only for an actual Principal
    // TODO: Find messages by a targetUserId as a request parameter

    @Autowired
    private MessageService messageService;

    private UUID getPrincipalId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ((UserDetails) authentication.getPrincipal()).getId();
    }

    @GetMapping
    public ResponseEntity<List<Message>> getAllMessages(){
        return messageService.getAllPersistedMessages();
    }

    @PostMapping("/send/{targetUserIdAsString}")
    public ResponseEntity<Message> sendMessageToUserByUserId(@PathVariable String targetUserIdAsString,
                                                             @RequestBody Message.TempMessage message){
        Message messageToPersist = new Message(getPrincipalId(), UUID.fromString(targetUserIdAsString), message);
        messageToPersist.setSentDate(new Date());

        return messageService.saveMessage(messageToPersist);
    }

    @PatchMapping("/update")
    public ResponseEntity<Message> updateMessage(@RequestBody Message updatedMessage){
        return messageService.updateMessage(getPrincipalId(), updatedMessage);
    }

    @DeleteMapping("/delete/{messageIdAsString}")
    public ResponseEntity<?> deleteMessage(@PathVariable String messageIdAsString){
        return messageService.deleteMessage(getPrincipalId(), UUID.fromString(messageIdAsString));
    }

}
