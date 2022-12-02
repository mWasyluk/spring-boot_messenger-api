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

import java.util.*;
import java.util.stream.Collectors;

import static pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponseMessages.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {
    private final ConversationRepository conversationRepository;

    public ServiceResponse<Conversation> getById(UUID conversationId){
        Optional<Conversation> optionalConversationById = conversationRepository.findById(conversationId);
        if (!optionalConversationById.isPresent()){
            log.debug("Conversation with UUID " + conversationId + " does not exist");
            return new ServiceResponse<>(null, HttpStatus.BAD_REQUEST, EXISTING_ID_REQUIRED);
        }
        return new ServiceResponse<>(
                optionalConversationById.get(),
                HttpStatus.OK,
                OK);
    }

    public ServiceResponse<Conversation> createConversation(Collection<Profile> participators){
        Set<Profile> participatorIdsWithoutDuplicates = new HashSet<>(participators);
        if (participatorIdsWithoutDuplicates.size() < 1){
            log.debug("Conversation without participants cannot be created");
            return new ServiceResponse<>(
                    null,
                    HttpStatus.BAD_REQUEST,
                    CORRECT_RANGE_REQUIRED);
        }

        List<UUID> participatorIds = participatorIdsWithoutDuplicates.stream()
                .map(Profile::getId)
                .collect(Collectors.toList());
        List<Conversation> byParticipators = conversationRepository.findByParticipatorsIdIn(participatorIds); // TODO: If the result can be turbo big?
        Conversation conversationWithExactParticipators = getConversationWithExactParticipators(byParticipators, participatorIds);
        if (conversationWithExactParticipators != null){
            return new ServiceResponse<>(
                    conversationWithExactParticipators,
                    HttpStatus.CONFLICT,
                    CONFLICT);
        }

        Conversation savedConversation = conversationRepository.save(new Conversation(new ArrayList<>(participatorIdsWithoutDuplicates)));
        return new ServiceResponse<>(
                savedConversation,
                HttpStatus.OK,
                OK);
    }

    /* The method filters the given as the first argument to Collection <Conversation>.
     * It looks for Collections containing exactly the same participant list as the second
     * argument to Collection <UUID> participatorIds.
     * If the result list of matching Conversation objects is greater than 1, the method post the error
     * log and continues its work without any more actions specific for the case. The log message contains
     * all IDs of the Conversation objects that contain the same participators.
     */
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

    public ServiceResponse<?> addMessageToConversationById(UUID conversationId, Message messageToAdd) {
        ServiceResponse<Conversation> conversationServiceResponseById = getById(conversationId);
        if (conversationServiceResponseById.getObject() == null){
            return new ServiceResponse<>(
                    null,
                    conversationServiceResponseById.getHttpStatus(),
                    conversationServiceResponseById.getMessage());
        }

        if (!conversationServiceResponseById.getObject().addMessage(messageToAdd)){
            return new ServiceResponse<>(
                    null,
                    HttpStatus.CONFLICT,
                    CONFLICT);
        }

        Conversation savedConversation = conversationRepository.save(conversationServiceResponseById.getObject());
        return new ServiceResponse<>(
                savedConversation,
                HttpStatus.OK,
                OK);
    }


    public ServiceResponse<?> deleteConversationById(UUID conversationId) {
        conversationRepository.deleteById(conversationId);
        return new ServiceResponse<>(
                null,
                HttpStatus.OK,
                OK);
    }

    // TODO: remove for release
    public ServiceResponse<List<Conversation>> getAll() {
        return new ServiceResponse<>(conversationRepository.findAll(), HttpStatus.OK, OK);
    }
}
