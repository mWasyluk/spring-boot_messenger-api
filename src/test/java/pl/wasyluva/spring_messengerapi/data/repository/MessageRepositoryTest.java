package pl.wasyluva.spring_messengerapi.data.repository;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.wasyluva.spring_messengerapi.domain.message.Message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Slf4j
class MessageRepositoryTest {

    @Autowired
    private MessageRepository underTests;

    @Nested
    @DisplayName("Assert that the MessageRepository")
    class AssertThatMessageRepo {

        private Message testMessage;

        @BeforeEach
        void setUp() {
            UUID testUserId1 = UUID.randomUUID();
            UUID testUserId2 = UUID.randomUUID();
            String testMessageContent = "Test message";

            testMessage = new Message(testUserId1, testUserId2, new Message.TempMessage(testMessageContent));
        }

        @AfterEach
        void tearDown() {
            underTests.deleteAll();
        }

        @Test
        @DisplayName("is not null")
        public void isNotNull() {
            assertThat(underTests).isNotNull();
        }

        @Test
        @DisplayName("is insertable")
        public void isInsertable(){
            underTests.save(testMessage);

            assertThat(underTests.findAll().contains(testMessage)).isTrue();
        }

        @Test
        @DisplayName("is readable")
        public void returnsCorrectObjects(){
            underTests.save(testMessage);

            Optional<Message> any = underTests.findAll().stream().filter((item) -> item.equals(testMessage)).findAny();
            assertThat(any.isPresent()).isTrue();
        }

        @Test
        @DisplayName("is empty at the beginning")
        public void returnsNullIfEntityDoesNotExist(){
            Optional<Message> any = underTests.findAll().stream().findAny();
            assertThat(any.isPresent()).isFalse();
        }
    }
}