package pl.wasyluva.spring_messengerapi.data.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.wasyluva.spring_messengerapi.data.repository.ConversationRepository;
import pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponse;
import pl.wasyluva.spring_messengerapi.domain.message.Conversation;
import pl.wasyluva.spring_messengerapi.domain.message.Message;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Profile;
import pl.wasyluva.spring_messengerapi.util.DebugLogger;
import pl.wasyluva.spring_messengerapi.util.UuidUtils;

import java.util.*;
import java.util.stream.Collectors;

import static pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponseMessages.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {
    private final ConversationRepository conversationRepository;

    public ServiceResponse<?> getById(@NonNull UUID requestingProfileUuid, @NonNull UUID conversationUuid){
        Optional<Conversation> optionalConversationById = conversationRepository.findById(conversationUuid);
        if (!optionalConversationById.isPresent()){
            DebugLogger.logObjectNotFound(conversationUuid.toString());
            return ServiceResponse.INCORRECT_ID;
        }

        if (optionalConversationById.get().getParticipators().stream()
                .noneMatch(profile -> profile.getId().equals(requestingProfileUuid))){
            DebugLogger.logUnauthorizedProfile(requestingProfileUuid.toString());
            return ServiceResponse.UNAUTHORIZED;
        }

        return new ServiceResponse<>(
                optionalConversationById.get(),
                HttpStatus.OK);
    }

    public ServiceResponse<?> getById(@NonNull UUID requestingProfileUuid, @NonNull String conversationUuidString){
        if (!UuidUtils.isStringCorrectUuid(conversationUuidString)) {
            DebugLogger.logInvalidUuidAsString(conversationUuidString);
            return ServiceResponse.INCORRECT_ID;
        }
        return getById(requestingProfileUuid, UUID.fromString(conversationUuidString));
    }

    public ServiceResponse<?> getAllConversationsByParticipator(@NonNull UUID requestingProfileUuid){
        List<Conversation> allByParticipatorId = conversationRepository.findAllByParticipatorsId(requestingProfileUuid);

        return new ServiceResponse<>(allByParticipatorId, HttpStatus.OK);
    }

    public ServiceResponse<?> createConversation(@NonNull UUID requestingProfileUuid, @NonNull Collection<Profile> participators){
        Set<Profile> participatorIdsWithoutDuplicates = new HashSet<>(participators);

        // check if the Conversation contains any participator
        if (participatorIdsWithoutDuplicates.size() < 1){
            log.debug("Conversation without participants cannot be created");
            return new ServiceResponse<>(
                    INVALID_CONVERSATION_PARTICIPATORS,
                    HttpStatus.BAD_REQUEST);
        }

        // check if the requesting user is a participator
        if (participatorIdsWithoutDuplicates.stream()
                .map(Profile::getId)
                .noneMatch(id -> id.equals(requestingProfileUuid))){
            return ServiceResponse.UNAUTHORIZED;
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
                HttpStatus.CREATED);
    }

    /** The method filters the given as the first argument Collection <Conversation>.
     * It looks for Collections containing exactly the same participators list as the second
     * argument to Collection <UUID> participatorIds.
     * If the result list of matching Conversation objects is greater than 1, the method post the error
     * log and continues its work without any more actions specific for the case. The log message contains
     * all IDs of the Conversation objects that contain the same participators.
     **/
    private Conversation getConversationWithExactParticipators(@NonNull Collection<Conversation> conversations, @NonNull Collection<UUID> participatorIds) {
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

    public ServiceResponse<?> addMessageToConversationById(@NonNull UUID requestingProfileUuid, @NonNull UUID conversationId, @NonNull Message messageToAdd) {
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
            return ServiceResponse.UNAUTHORIZED;
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
                HttpStatus.CREATED);
    }

    public ServiceResponse<?> addMessageToConversationById(@NonNull UUID requestingProfileUuid, @NonNull String conversationStringUuid, @NonNull Message messageToPersist) {
        if (!UuidUtils.isStringCorrectUuid(conversationStringUuid)){
            return ServiceResponse.INCORRECT_ID;
        }
        return addMessageToConversationById(requestingProfileUuid, UUID.fromString(conversationStringUuid), messageToPersist);
    }

    // TODO: save requesting Profile to list of participators which have deleted this conversation.
    //  Check if the request is already sent form all participators.
    //  if true -> remove conversation;
    //  else -> add Profile the to Conversation's list of Profiles that have requested deletion;
    //  Tests then...
    public ServiceResponse<?> deleteConversationById(@NonNull UUID requestingProfileUuid, @NonNull UUID conversationId) {
        ServiceResponse<?> byId = getById(requestingProfileUuid, conversationId);

        // check if the response contains a Conversation object
        if (!(byId.getBody() instanceof Conversation)){
            return byId;
        }

        // delete the Conversation
        conversationRepository.deleteById(conversationId);
        return new ServiceResponse<>(OK, HttpStatus.OK);
    }

    public ServiceResponse<?> deleteConversationById(@NonNull UUID requestingProfileUuid, @NonNull String conversationUuidString) {
        // check if the UUID is correct
        if (!UuidUtils.isStringCorrectUuid(conversationUuidString)){
            return ServiceResponse.INCORRECT_ID;
        }

        // redirect to method with a different signature
        return deleteConversationById(requestingProfileUuid, UUID.fromString(conversationUuidString));
    }

    // TODO: remove for release
    public ServiceResponse<List<Conversation>> getAll() {
        return new ServiceResponse<>(conversationRepository.findAll(), HttpStatus.OK);
    }
}
