package pl.wasyluva.spring_messengerapi.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wasyluva.spring_messengerapi.data.service.ConversationService;
import pl.wasyluva.spring_messengerapi.data.service.ProfileService;
import pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponse;
import pl.wasyluva.spring_messengerapi.domain.message.Conversation;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Profile;
import pl.wasyluva.spring_messengerapi.web.controller.support.PrincipalService;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor

@RestController
@RequestMapping("/conversations")
public class ConversationController {
    private final ConversationService conversationService;
    private final ProfileService profileService;
    private final PrincipalService principalService;

    @GetMapping
    public ResponseEntity<?> getAllConversations(){
        return conversationService.getAll().getResponseEntity();
    }

    @GetMapping("/{conversationIdAsString}")
    public ResponseEntity<?> getConversationById(@PathVariable String conversationIdAsString){
        return conversationService.getById(
                    principalService.getPrincipalProfileId(),
                    conversationIdAsString)
                .getResponseEntity();

    }

    // TODO: Object with already existing Conversation should be send in response only when the request
    //      is sent by any of participators

    @PostMapping("/create")
    public ResponseEntity<?> createConversation(@RequestBody Conversation conversation){
        List<Profile> participators = conversation.getParticipators().stream()
                .map(Profile::getId)
                .map(id -> profileService.getProfileById(id).getBody())
                .filter(obj -> obj instanceof Profile)
                .map(obj -> (Profile)obj)
                .collect(Collectors.toList());

        // return 4** if any of profileService::getProfileById is null (it means that some ids do not exist in the database)
        if (conversation.getParticipators().size() != participators.size()){
            return ServiceResponse.INCORRECT_ID
                    .getResponseEntity();
        }
        return conversationService.createConversation(
                    principalService.getPrincipalProfileId(),
                    participators)
                .getResponseEntity();
    }

    // TODO: save to list of participators which have deleted this conversation.
    //  Check if request is sent form all participators already.
    //  if true -> remove conversation and all its messages from the database;
    //  else -> add user to Conversation's List<Profile> deleters;
    @DeleteMapping(value = "/delete/{conversationIdAsString}")
    public ResponseEntity<?> deleteConversationById(@PathVariable String conversationIdAsString){
        return conversationService.deleteConversationById(
                    principalService.getPrincipalProfileId(),
                    conversationIdAsString)
                .getResponseEntity();
    }
}
