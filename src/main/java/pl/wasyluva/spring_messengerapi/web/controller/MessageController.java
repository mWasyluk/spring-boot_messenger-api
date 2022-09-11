package pl.wasyluva.spring_messengerapi.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.wasyluva.spring_messengerapi.data.repository.MessageRepository;
import pl.wasyluva.spring_messengerapi.data.repository.UserProfileRepository;
import pl.wasyluva.spring_messengerapi.domain.message.Message;
import pl.wasyluva.spring_messengerapi.domain.message.MessageState;
import pl.wasyluva.spring_messengerapi.domain.userdetails.UserProfile;

import java.util.Date;
import java.util.List;

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

    @GetMapping("/send")
    public ResponseEntity<Message> sendTestMessageByUserId(){
        List<UserProfile> all = userProfileRepository.findAll();
        Message save = messageRepository.save(new Message(all.get(0), all.get(1), "Hello, World!"));
        save.addNextDate(new Date());
        return new ResponseEntity<>( save , HttpStatus.OK);
    }

    @GetMapping("/update")
    public ResponseEntity<Message> updateMessageDate(){
        List<Message> all = messageRepository.findAll();
        if (!all.isEmpty() && all.get(0).getMessageState() != MessageState.READ) {
            Message message = all.get(0);
            message.addNextDate(new Date());
            return new ResponseEntity<>(messageRepository.save(message), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
    }

    // TODO: Find messages only for an actual Principal + Find messages by a second User ID as a request parameter
}
