package pl.wasyluva.spring_messengerapi.data.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.wasyluva.spring_messengerapi.data.repository.ConversationRepository;
import pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponse;
import pl.wasyluva.spring_messengerapi.domain.message.Conversation;
import pl.wasyluva.spring_messengerapi.domain.message.Message;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Profile;
import pl.wasyluva.spring_messengerapi.util.UuidUtils;

import java.util.*;
import java.util.stream.Collectors;

import static pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponseMessages.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {
    private final ConversationRepository conversationRepository;

    public ServiceResponse<?> getById(UUID requestingProfileUuid, UUID conversationUuid){
        Optional<Conversation> optionalConversationById = conversationRepository.findById(conversationUuid);
        if (!optionalConversationById.isPresent()){
            log.debug("Conversation with UUID " + conversationUuid + " does not exist");
            return new ServiceResponse<>(CONVERSATION_DOES_NOT_EXIST, HttpStatus.NOT_FOUND);
        }
        if (optionalConversationById.get().getParticipators().stream()
                .noneMatch(profile -> profile.getId().equals(requestingProfileUuid))){
            return new ServiceResponse<>(UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        return new ServiceResponse<>(
                optionalConversationById.get(),
                HttpStatus.OK);
    }

    public ServiceResponse<?> getById(UUID requestingProfileUuid, String conversationUuidString){
        if (!UuidUtils.isStringCorrectUuid(conversationUuidString)) {
            return new ServiceResponse<>(EXISTING_ID_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        return getById(requestingProfileUuid, UUID.fromString(conversationUuidString));
    }

    public ServiceResponse<?> createConversation(UUID requestingProfileUuid, Collection<Profile> participators){
        Set<Profile> participatorIdsWithoutDuplicates = new HashSet<>(participators);

        // check if the requesting user is a participator
        if (participatorIdsWithoutDuplicates.stream()
                .map(Profile::getId)
                .noneMatch(id -> id.equals(requestingProfileUuid))){
            return new ServiceResponse<>(
                    UNAUTHORIZED,
                    HttpStatus.UNAUTHORIZED);
        }

        // check if the Conversation contains any participator
        if (participatorIdsWithoutDuplicates.size() < 1){
            log.debug("Conversation without participants cannot be created");
            return new ServiceResponse<>(
                    BAD_CONVERSATION_PARTICIPATORS,
                    HttpStatus.BAD_REQUEST);
        }

        List<UUID> participatorIds = participatorIdsWithoutDuplicates.stream()
                .map(Profile::getId)
                .collect(Collectors.toList());
        List<Conversation> byParticipators = conversationRepository.findByParticipatorsIdIn(participatorIds); // TODO: If the result can be turbo big?
        Conversation conversationWithExactParticipators = getConversationWithExactParticipators(byParticipators, participatorIds);

        // check if Conversation with the exact participators exists
        if (conversationWithExactParticipators != null){
            return new ServiceResponse<>(
                    CONVERSATION_CONFLICT,
                    HttpStatus.CONFLICT);
        }

        // save and return the Conversation
        Conversation savedConversation = conversationRepository.save(new Conversation(new ArrayList<>(participatorIdsWithoutDuplicates)));
        return new ServiceResponse<>(
                savedConversation,
                HttpStatus.OK);
    }

    /** The method filters the given as the first argument Collection <Conversation>.
     * It looks for Collections containing exactly the same participators list as the second
     * argument to Collection <UUID> participatorIds.
     * If the result list of matching Conversation objects is greater than 1, the method post the error
     * log and continues its work without any more actions specific for the case. The log message contains
     * all IDs of the Conversation objects that contain the same participators.
     **/
    private Conversation getConversationWithExactParticipators(Collection<Conversation> conversations, Collection<UUID> participatorIds) {
        Set<Conversation> conversationsWithoutDuplicates = new HashSet<>(conversations);
        Set<UUID> participatorIdsWithoutDuplicates = new HashSet<>(participatorIds);

        List<Conversation> matchingConversations = conversationsWithoutDuplicates.stream()
                .filter(conversation -> conversation.getParticipators().stream()
                        .allMatch(profile -> participatorIdsWithoutDuplicates.contains(profile.getId())))
                .filter(conversation -> conversation.getParticipators().size() == participatorIdsWithoutDuplicates.size())
                .collect(Collectors.toList());

        if (matchingConversations.size() > 1){
            String conversationsIdsForError = matchingConversations.stream()
                    .map(Conversation::getId)
                    .map(Objects::toString)
                    .collect(Collectors.joining(" and "));
            // TODO for v.2.0: send to Kafka's topic + build an application that sends me a notification
            //  every time an error is reported
            log.error("The Data Base is containing multiply conversations for same participators. Conversations IDs:\n"
                    + conversationsIdsForError);
        }

        if (matchingConversations.size() >= 1) {
            return matchingConversations.get(0);
        } else {
            return null; }
    }

    public ServiceResponse<?> addMessageToConversationById(UUID requestingProfileUuid, UUID conversationId, Message messageToAdd) {
        ServiceResponse<?> conversationServiceResponseById = getById(requestingProfileUuid, conversationId);

        // check if the response contains a Conversation object
        if (!(conversationServiceResponseById.getBody() instanceof Conversation)){
            return new ServiceResponse<>(
                    conversationServiceResponseById.getBody(),
                    conversationServiceResponseById.getStatusCode());
        }

        Conversation conversation = (Conversation) conversationServiceResponseById.getBody();

        // check if the requesting user is a participator
        if (conversation.getParticipators().stream()
                .map(Profile::getId)
                .noneMatch(id -> id.equals(requestingProfileUuid))){
            return new ServiceResponse<>(
                    UNAUTHORIZED,
                    HttpStatus.UNAUTHORIZED);
        }

        // check if the Message can be added to the Conversation
        if (!conversation.addMessage(messageToAdd)){
            return new ServiceResponse<>(
                    CANNOT_ADD_MESSAGE_TO_CONVERSATION,
                    HttpStatus.BAD_REQUEST);
        }

        // save and return the Message
        Conversation savedConversation = conversationRepository.save(conversation);
        return new ServiceResponse<>(
                savedConversation,
                HttpStatus.OK);
    }

    public ServiceResponse<?> addMessageToConversationById(UUID requestingProfileUuid, String conversationStringUuid, Message messageToPersist) {
        if (!UuidUtils.isStringCorrectUuid(conversationStringUuid)){
            return new ServiceResponse<>(EXISTING_ID_REQUIRED, HttpStatus.NOT_FOUND);
        }
        return addMessageToConversationById(requestingProfileUuid, UUID.fromString(conversationStringUuid), messageToPersist);
    }


    public ServiceResponse<?> deleteConversationById(UUID requestingProfileUuid, UUID conversationId) {
        ServiceResponse<?> byId = getById(requestingProfileUuid, conversationId);

        // check if the response contains a Conversation object
        if (!(byId.getBody() instanceof Conversation)){
            return byId;
        }

        // delete the Conversation
        conversationRepository.deleteById(conversationId);
        return new ServiceResponse<>(OK, HttpStatus.OK);
    }

    public ServiceResponse<?> deleteConversationById(UUID requestingProfileUuid, String conversationUuidString) {
        // check if the UUID is correct
        if (!UuidUtils.isStringCorrectUuid(conversationUuidString)){
            return new ServiceResponse<>(EXISTING_ID_REQUIRED, HttpStatus.NOT_FOUND);
        }

        // redirect to method with a different signature
        return deleteConversationById(requestingProfileUuid, UUID.fromString(conversationUuidString));
    }

    // TODO: remove for release
    public ServiceResponse<List<Conversation>> getAll() {
        return new ServiceResponse<>(conversationRepository.findAll(), HttpStatus.OK);
    }

}
