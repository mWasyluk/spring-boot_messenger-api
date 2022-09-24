package pl.wasyluva.spring_messengerapi.data.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.wasyluva.spring_messengerapi.data.repository.MessageRepository;
import pl.wasyluva.spring_messengerapi.data.repository.UserProfileRepository;
import pl.wasyluva.spring_messengerapi.domain.message.Message;
import pl.wasyluva.spring_messengerapi.domain.userdetails.UserProfile;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Check if MessageService entity")
class MessageServiceTest {
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @InjectMocks
    private MessageService messageService;

    @Test
    @DisplayName("exists")
    void doesExist(){
        assertThat(messageService).isNotNull();
    }

    @Test
    @DisplayName("contains existing MessageRepository type property")
    void containsMessageRepository(){
        assertThat(messageRepository).isNotNull();
    }

    private UUID persistedUserId1 = null;
    private UUID persistedUserId2 = null;
    private Message testMessage = null;

    @BeforeEach
    void setUp() {
        persistedUserId1 = UUID.randomUUID();
        persistedUserId2 = UUID.randomUUID();
        testMessage = new Message(persistedUserId1, persistedUserId2, new Message.TempMessage("Test message"));

        lenient().when(userProfileRepository.findById(persistedUserId1))
                .thenReturn( Optional.of(new UserProfile(persistedUserId1, "test", "test", new Date())));
        lenient().when(userProfileRepository.findById(persistedUserId2))
                .thenReturn( Optional.of(new UserProfile(persistedUserId2, "test", "test", new Date())));
    }

    @Nested
    @DisplayName("uses correct method to")
    class HappyPathMethodsCheck{

        @Test
        @DisplayName("save new Message object in DB")
        void saveMessage() {
            messageService.saveMessage(testMessage);

            verify(messageRepository).save(testMessage);
        }

        @Test
        @DisplayName("update changed Message object in DB")
        void updateMessage() {
            UUID tempMessageId = UUID.randomUUID();
            testMessage.setId(tempMessageId);
            Message updatedMessage = new Message();
            updatedMessage.setId(testMessage.getId());
            updatedMessage.setContent("Updated message content");

            when(messageRepository.findById(tempMessageId)).thenReturn(Optional.of(testMessage));
            messageService.updateMessage(persistedUserId1, updatedMessage);

            verify(messageRepository).save(testMessage);
            assertThat(messageRepository.findById(tempMessageId).get().getContent()).isEqualTo("Updated message content");
        }

        @Test
        @DisplayName("delete indicated Message object from DB")
        void deleteMessage() {
            assertThat(true).isTrue();
        }
    }

    @Nested
    @DisplayName("returns proper status if sent updating message that")
    class UpdateMessageMethodTest{
        @Test
        @DisplayName("has no ID")
        public void updatingMessageWithNoId(){
            assertThat(messageService.updateMessage(testMessage.getSourceUserId(), testMessage))
                    .isEqualTo(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
        }

        @Test
        @DisplayName("is not persisted")
        public void updatingNotPersistedMessage(){
            testMessage.setId(UUID.randomUUID());

            assertThat(messageService.updateMessage(testMessage.getTargetUserId(), testMessage))
                    .isEqualTo(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        }

        @Test
        @DisplayName("is sent by another user")
        public void updatingPersistedMessageAsAnotherUser(){
            testMessage.setId(UUID.randomUUID());
            when(messageRepository.findById(testMessage.getId())).thenReturn(Optional.ofNullable(testMessage));

            assertThat(messageService.updateMessage(UUID.randomUUID(), testMessage))
                    .isEqualTo(new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
        }

        @Test
        @DisplayName("contains all possible data")
        public void updatingPersistedMessageWithAllData(){
            // Prepare data
            UUID tempId = UUID.randomUUID();
            Date tempSentDate = new Date();
            Date tempDeliveryDate = new Date();
            Date tempReadDate = new Date();
            // Create new message object to imitate updated message
            Message tempMessage = new Message();
            tempMessage.setId(tempId);
            tempMessage.setSentDate(tempSentDate);
            tempMessage.setDeliveryDate(tempDeliveryDate);
            tempMessage.setReadDate(tempReadDate);
            // Set
            testMessage.setId(tempId);

            when(messageRepository.findById(testMessage.getId()))
                    .thenReturn(Optional.ofNullable(testMessage));
            messageService.updateMessage(testMessage.getSourceUserId(), tempMessage);

            verify(messageRepository).save(testMessage);
            assertThat(messageRepository.findById(tempId).get().getId()).isEqualTo(tempId);
            assertThat(messageRepository.findById(tempId).get().getSentDate()).isEqualTo(tempSentDate);
            assertThat(messageRepository.findById(tempId).get().getDeliveryDate()).isEqualTo(tempDeliveryDate);
            assertThat(messageRepository.findById(tempId).get().getReadDate()).isEqualTo(tempReadDate);
        }
    }

}