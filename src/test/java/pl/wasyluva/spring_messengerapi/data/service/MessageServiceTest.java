package pl.wasyluva.spring_messengerapi.data.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import pl.wasyluva.spring_messengerapi.data.repository.MessageRepository;
import pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponse;
import pl.wasyluva.spring_messengerapi.domain.message.Conversation;
import pl.wasyluva.spring_messengerapi.domain.message.Message;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {
    @Mock
    MessageRepository messageRepository;
    @Mock
    ConversationService conversationService;
    MessageService messageService;

    UUID testMessageAuthorUuid = UUID.randomUUID();
    Message testMessage;

    @BeforeEach
    void setUp() {
        messageService = new MessageService(messageRepository, conversationService);
        testMessage = new Message();
        testMessage.setSourceUserId(testMessageAuthorUuid);
    }

    @Nested
    @DisplayName("Test if the updateMessage() method")
    class UpdateMessageTest {
        @Test
        @DisplayName("returns incorrect_id when the message update does not contain an ID")
        void returnsIncorrectIdWhenTheMessageUpdateDoesNotContainAnId() {
            ServiceResponse<?> serviceResponse = messageService.updateMessage(testMessageAuthorUuid, testMessage);

            assertThat(serviceResponse).isEqualTo(ServiceResponse.INCORRECT_ID);
        }

        @Test
        @DisplayName("returns incorrect_id when the message update contains nonexistent ID")
        void returnsIncorrectIdWhenTheMessageUpdateContainsNonexistentId() {
            testMessage.setId(UUID.randomUUID());
            when(messageRepository.findById(any())).thenReturn(Optional.empty());

            ServiceResponse<?> serviceResponse = messageService.updateMessage(testMessageAuthorUuid, testMessage);

            assertThat(serviceResponse).isEqualTo(ServiceResponse.INCORRECT_ID);
        }

        @Test
        @DisplayName("returns unauthorized when the message update sent by a profile another then the author's")
        void returnsUnauthorizedWhenTheMessageUpdateSentByAProfileAnotherThenTheAuthors() {
            testMessage.setId(UUID.randomUUID());
            when(messageRepository.findById(any())).thenReturn(Optional.of(testMessage));

            ServiceResponse<?> serviceResponse = messageService.updateMessage(UUID.randomUUID(), testMessage);

            assertThat(serviceResponse).isEqualTo(ServiceResponse.UNAUTHORIZED);
        }

        @Test
        @DisplayName("returns updated message when the message update contains an ID and new data")
        void returnsUpdatedMessageWhenTheMessageUpdateContainsAnIdAndNewData() {
            UUID messageId = UUID.randomUUID();
            testMessage.setId(messageId);
            testMessage.setContent("test");
            testMessage.setSentDate(new Date());
            when(messageRepository.findById(any())).thenReturn(Optional.of(testMessage));
            Message messageUpdate = new Message();
            messageUpdate.setId(messageId);
            messageUpdate.setContent("update");
            Date deliveryDate = new Date();
            messageUpdate.setDeliveryDate(deliveryDate);
            Date readDate = new Date();
            messageUpdate.setReadDate(readDate);
            when(messageRepository.save(any())).thenReturn(messageUpdate);

            ServiceResponse<?> serviceResponse = messageService.updateMessage(testMessageAuthorUuid, messageUpdate);

            assertThat(serviceResponse.getBody()).isExactlyInstanceOf(Message.class);
            ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
            verify(messageRepository).save(messageArgumentCaptor.capture());
            assertThat(messageArgumentCaptor.getValue().getContent()).isEqualTo("update");
            assertThat(messageArgumentCaptor.getValue().getReadDate()).isEqualTo(readDate);
            assertThat(messageArgumentCaptor.getValue().getDeliveryDate()).isEqualTo(deliveryDate);
        }

        @Test
        @DisplayName("does not update dates that are already set")
        void doesNotUpdateDatesThatAreAlreadySet() {
            UUID messageId = UUID.randomUUID();
            testMessage.setId(messageId);
            Date sentDate = new Date();
            Date deliveryDate = new Date();
            Date readDate = new Date();
            testMessage.setSentDate(sentDate);
            testMessage.setDeliveryDate(deliveryDate);
            testMessage.setReadDate(readDate);
            when(messageRepository.findById(any())).thenReturn(Optional.of(testMessage));
            Message messageUpdate = new Message();
            messageUpdate.setId(messageId);
            messageUpdate.setSentDate(new Date());
            messageUpdate.setDeliveryDate(new Date());
            messageUpdate.setReadDate(new Date());
            when(messageRepository.save(any())).thenReturn(messageUpdate);

            ServiceResponse<?> serviceResponse = messageService.updateMessage(testMessageAuthorUuid, messageUpdate);

            assertThat(serviceResponse.getBody()).isExactlyInstanceOf(Message.class);
            ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
            verify(messageRepository).save(messageArgumentCaptor.capture());
            assertThat(messageArgumentCaptor.getValue().getSentDate()).isEqualTo(sentDate);
            assertThat(messageArgumentCaptor.getValue().getDeliveryDate()).isEqualTo(deliveryDate);
            assertThat(messageArgumentCaptor.getValue().getReadDate()).isEqualTo(readDate);
        }
    }

    @Nested
    @DisplayName("Test if the deleteMessage() method")
    class DeleteMessageTest {
        @BeforeEach
        void setUp() {
            testMessage.setId(UUID.randomUUID());
        }

        @Test
        @DisplayName("returns INCORRECT_ID when the message ID is an invalid UUID as a String")
        void returnsIncorrectIdWhenTheMessageIdIsAnInvalidUuidAsAString() {
            String invalidUuid = "8ae5fbf0-7881-a1eb-0242ac120002";

            ServiceResponse<?> serviceResponse = messageService.deleteMessage(testMessageAuthorUuid, invalidUuid);

            assertThat(serviceResponse).isEqualTo(ServiceResponse.INCORRECT_ID);
            verify(messageRepository, never()).findById(any());
        }

        @Test
        @DisplayName("returns INCORRECT_ID when the message with the given UUID does not exists")
        void returnsIncorrectIdWhenTheMessageWithTheGivenUuidDoesNotExists() {
            String validUuid = UUID.randomUUID().toString();
            when(messageRepository.findById(any())).thenReturn(Optional.empty());

            ServiceResponse<?> serviceResponse = messageService.deleteMessage(testMessageAuthorUuid, validUuid);

            assertThat(serviceResponse).isEqualTo(ServiceResponse.INCORRECT_ID);
        }

        @Test
        @DisplayName("returns UNAUTHORIZED when the message author's UUID is different than the requesting user's UUID")
        void returnsUnauthorizedWhenTheMessageAuthorSUuidIsDifferentThanTheRequestingUserSUuid() {
            when(messageRepository.findById(testMessage.getId())).thenReturn(Optional.of(testMessage));

            ServiceResponse<?> serviceResponse = messageService.deleteMessage(UUID.randomUUID(), testMessage.getId());

            assertThat(serviceResponse).isEqualTo(ServiceResponse.UNAUTHORIZED);
        }

        @Test
        @DisplayName("returns UNAUTHORIZED when the conversationService returns UNAUTHORIZED")
        void returnsUnauthorizedWhenTheConversationServiceReturnsUnauthorized() {
            Conversation conversation = new Conversation();
            conversation.setId(UUID.randomUUID());
            testMessage.setConversation(conversation);
            when(messageRepository.findById(testMessage.getId())).thenReturn(Optional.of(testMessage));
            doReturn(ServiceResponse.UNAUTHORIZED).when(conversationService).getById(any(UUID.class), any(UUID.class));

            ServiceResponse<?> serviceResponse = messageService.deleteMessage(testMessageAuthorUuid, testMessage.getId());

            assertThat(serviceResponse).isEqualTo(ServiceResponse.UNAUTHORIZED);
        }

        @Test
        @DisplayName("returns OK when the conversationService returns the message's Conversation")
        void returnsOkWhenTheConversationServiceReturnsTheMessageSConversation() {
            Conversation conversation = new Conversation();
            conversation.setId(UUID.randomUUID());
            testMessage.setConversation(conversation);
            when(messageRepository.findById(testMessage.getId())).thenReturn(Optional.of(testMessage));
            ServiceResponse<Conversation> conversationServiceResponse = new ServiceResponse<>(conversation, HttpStatus.OK);
            doReturn(conversationServiceResponse).when(conversationService).getById(any(UUID.class), any(UUID.class));

            ServiceResponse<?> serviceResponse = messageService.deleteMessage(testMessageAuthorUuid, testMessage.getId());

            assertThat(serviceResponse).isEqualTo(ServiceResponse.OK);
            verify(messageRepository).deleteById(any());
        }

        // TODO: integration test(???) - check if the persistent conversation does not contain the message after removing it
        @Test
        @DisplayName("removes the Message from the Conversation")
        void removesTheMessageFromTheConversation() {

        }
    }
}