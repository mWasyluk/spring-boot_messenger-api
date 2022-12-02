package pl.wasyluva.spring_messengerapi.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wasyluva.spring_messengerapi.data.service.ConversationService;
import pl.wasyluva.spring_messengerapi.data.service.ProfileService;
import pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponse;
import pl.wasyluva.spring_messengerapi.domain.message.Conversation;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Profile;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponseMessages.CORRECT_RANGE_REQUIRED;
import static pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponseMessages.EXISTING_ID_REQUIRED;

@RequiredArgsConstructor

@RestController
@RequestMapping("/conversations")
public class ConversationController {
    private final ConversationService conversationService;
    private final ProfileService profileService;

    @GetMapping
    public ServiceResponse<List<Conversation>> getAll(){
        return conversationService.getAll();
    }

    @GetMapping("/{conversationIdAsString}")
    public ResponseEntity<?> getConversationById(@PathVariable String conversationIdAsString){
        UUID conversationId;
        try {
            conversationId = UUID.fromString(conversationIdAsString);
        } catch (IllegalArgumentException exception){
            return new ResponseEntity<>(
                    EXISTING_ID_REQUIRED,
                    HttpStatus.BAD_REQUEST);
        }

        ServiceResponse<Conversation> serviceResponse = conversationService.getById(conversationId);
        return new ResponseEntity<>(
                serviceResponse,
                serviceResponse.getHttpStatus());
    }

    // TODO: Object with already existing Conversation should be send in response only when the request
    //      is sent by any of participators
    // TODO: Add one more parameter - profile id of authenticated user and check if the user is one of the participators.
    //      else -> return null, bad request, unauthorized

    @PostMapping("/create")
    public ResponseEntity<?> createConversationWithParticipators(@RequestBody Conversation conversation){
        if (conversation.getParticipators().size() < 1){
            return new ResponseEntity<>(
                    EXISTING_ID_REQUIRED,
                    HttpStatus.BAD_REQUEST);
        }

        List<Profile> participators = conversation.getParticipators().stream()
                .map(Profile::getId)
                .map(profileService::getProfileById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // return 4** if any of profileService::getProfileById is null (it means that some ids do not exist in the database)
        if (conversation.getParticipators().size() != participators.size()){
            return new ResponseEntity<>(
                    EXISTING_ID_REQUIRED,
                    HttpStatus.BAD_REQUEST);
        }

        ServiceResponse<Conversation> conversationServiceResponse = conversationService.createConversation(participators);
        return new ResponseEntity<>(conversationServiceResponse, conversationServiceResponse.getHttpStatus());
    }

    // TODO: save to list of participators which have deleted this conversation.
    //  Check if request is sent form all participators already.
    //  if true -> remove conversation and all its messages from the database;
    //  else -> add user to Conversation's List<Profile> deleters;
    @DeleteMapping(value = "/delete/{conversationIdAsString}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteConversationById(@PathVariable String conversationIdAsString){
        UUID conversationId = null;
        try {
            conversationId = UUID.fromString(conversationIdAsString);

        } catch (IllegalArgumentException exception){
            return new ResponseEntity<>(
                    CORRECT_RANGE_REQUIRED,
                    HttpStatus.BAD_REQUEST);
        }

        ServiceResponse<?> serviceResponse = conversationService.getById(conversationId);

        if (serviceResponse.getObject() == null){
            return new ResponseEntity<>(
                    serviceResponse,
                    HttpStatus.BAD_REQUEST);
        }

        serviceResponse = conversationService.deleteConversationById(conversationId);
        return new ResponseEntity<>(
                serviceResponse,
                serviceResponse.getHttpStatus());
    }
}
