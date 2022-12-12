package pl.wasyluva.spring_messengerapi.data.service;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wasyluva.spring_messengerapi.data.repository.ConversationRepository;
import pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponse;
import pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponseMessages;
import pl.wasyluva.spring_messengerapi.domain.message.Conversation;
import pl.wasyluva.spring_messengerapi.domain.message.Message;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Profile;
import pl.wasyluva.spring_messengerapi.util.UuidUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponse.INCORRECT_ID;
import static pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponse.UNAUTHORIZED;
import static pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponseMessages.CANNOT_ADD_MESSAGE_TO_CONVERSATION;
import static pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponseMessages.CONVERSATION_CONFLICT;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {
    @Mock
    ConversationRepository conversationRepository;
    ConversationService conversationService;

    Profile testParticipator1 = new Profile("test1", "test1", new Date());
    Profile testParticipator2 = new Profile("test2", "test2", new Date());
    Message testMessageFrom1 = new Message(testParticipator1.getId(), new Message.TempMessage("testContent"));
    Conversation testConversation = new Conversation(Arrays.asList(testParticipator1, testParticipator2));

    @BeforeEach
    void setEach(){
        conversationService = new ConversationService(conversationRepository);
        testConversation.setId(UUID.randomUUID());
    }

    Profile getDummyProfileWithOnlyUuid(UUID uuid) throws Exception {
        Constructor<Profile> declaredConstructor = Profile.class.getDeclaredConstructor();
        declaredConstructor.setAccessible(true);
        Profile profile = declaredConstructor.newInstance();
        profile.setId(uuid);
        return profile;
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("Test if the getById() method")
    class GetByIdTest {
        @Test
        @DisplayName("returns INCORRECT_ID when the given UUID as String is an invalid UUID")
        void returnsIncorrectIdWhenTheGivenUuidAsStringIsAnInvalidUuid() {
            ServiceResponse<?> serviceResponse = conversationService.getById(testParticipator1.getId(), UuidUtils.INVALID_UUID_AS_STRING);

            assertThat(serviceResponse).isEqualTo(INCORRECT_ID);
            verify(conversationRepository, never()).findById(any());
        }

        @Test
        @DisplayName("return INCORRECT_ID when the Conversation with the given UUID does not exist")
        void returnIncorrectIdWhenTheConversationWithTheGivenUuidDoesNotExist() {
            when(conversationRepository.findById(any())).thenReturn(Optional.empty());

            ServiceResponse<?> serviceResponse = conversationService.getById(testParticipator1.getId(), testConversation.getId());

            assertThat(serviceResponse).isEqualTo(INCORRECT_ID);
        }

        @Test
        @DisplayName("returns UNAUTHORIZED when the requesting Profile is not a participator in the Conversation")
        void returnsUnauthorizedWhenTheRequestingProfileIsNotAParticipatorInTheConversation() {
            when(conversationRepository.findById(any())).thenReturn(Optional.ofNullable(testConversation));

            ServiceResponse<?> serviceResponse = conversationService.getById(UUID.randomUUID(), testConversation.getId());

            assertThat(serviceResponse).isEqualTo(UNAUTHORIZED);
        }

        // TODO: move to another nested class where every method uses this method source
        Stream<Arguments> participatorsSource() {
            return Stream.of(
                    arguments(testParticipator1),
                    arguments(testParticipator2)
            );
        }

        @ParameterizedTest
        @MethodSource("participatorsSource")
        @DisplayName("does not return UNAUTHORIZED when the requesting Profile is one of the participators in the Conversation")
        void doesNotReturnUnauthorizedWhenTheRequestingProfileIsOneOfTheParticipatorsInTheConversation(Profile participator) {
            when(conversationRepository.findById(any())).thenReturn(Optional.ofNullable(testConversation));

            ServiceResponse<?> serviceResponse = conversationService.getById(participator.getId(), testConversation.getId());

            assertThat(serviceResponse).isNotEqualTo(UNAUTHORIZED);
        }

        @Test
        @DisplayName("returns the Conversation when a happy path case occurs")
        void returnsTheConversationWhenAHappyPathCaseOccurs() {
            when(conversationRepository.findById(any())).thenReturn(Optional.ofNullable(testConversation));

            ServiceResponse<?> serviceResponseByUuid = conversationService.getById(testParticipator1.getId(), testConversation.getId());
            ServiceResponse<?> serviceResponseByString = conversationService.getById(testParticipator1.getId(), testConversation.getId().toString());

            assertThat(serviceResponseByUuid.getBody()).isEqualTo(testConversation);
            assertThat(serviceResponseByString.getBody()).isEqualTo(testConversation);
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("Test if the createConversation() method")
    class CreateConversationTest {
        Collection<Profile> participators = new ArrayList<>();

        @BeforeEach
        void resetTestConversation(){
            testConversation = new Conversation(Arrays.asList(testParticipator1, testParticipator2));
        }

        Profile onlyUuid1() throws Exception{
            return getDummyProfileWithOnlyUuid(testParticipator1.getId()); 
        }
        Profile onlyUuid2() throws Exception{
            return getDummyProfileWithOnlyUuid(testParticipator2.getId());
        }

        @Test
        @DisplayName("returns UNAUTHORIZED when the requesting Profile is not one of the participators")
        void returnsUnauthorizedWhenTheRequestingProfileIsNotOneOfTheParticipators() throws Exception {
            Collection<Profile> participators = new ArrayList<>(Arrays.asList(onlyUuid1(), onlyUuid2()));

            ServiceResponse<?> serviceResponse = conversationService.createConversation(UUID.randomUUID(), participators);

            assertThat(serviceResponse).isEqualTo(UNAUTHORIZED);
        }

        // TODO: move to another nested class where every method uses this method source
        Stream<Arguments> participatorsSource() {
            return Stream.of(
                    arguments(testParticipator1),
                    arguments(testParticipator2)
            );
        }

        @ParameterizedTest
        @MethodSource("participatorsSource")
        @DisplayName("does not return UNAUTHORIZED when the requesting Profile is one of the participators")
        void doesNotReturnUnauthorizedWhenTheRequestingProfileIsOneOfTheParticipators(Profile participator) throws Exception {
            Collection<Profile> participators = new ArrayList<>(Arrays.asList(onlyUuid1(), onlyUuid2()));

            ServiceResponse<?> serviceResponse = conversationService.createConversation(testParticipator1.getId(), participators);

            assertThat(serviceResponse).isNotEqualTo(UNAUTHORIZED);
        }

        @Test
        @DisplayName("rejects Profiles duplications in the participators list")
        void rejectsProfilesDuplicationsInTheParticipatorsList() throws Exception {
            testConversation.setParticipators(Collections.singletonList(testParticipator1));
            when(conversationRepository.findByParticipatorsIdIn(anyList())).thenReturn(Collections.singletonList(testConversation));
            Collection<Profile> participators = new ArrayList<>(Arrays.asList(onlyUuid1(), onlyUuid1(), onlyUuid1()));

            ServiceResponse<?> serviceResponse = conversationService.createConversation(testParticipator1.getId(), participators);

            assertThat(serviceResponse.getBody()).isEqualTo(CONVERSATION_CONFLICT);
        }

        @Test
        @DisplayName("returns INVALID_PARTICIPATORS when the participators list is empty")
        void returnsInvalidParticipatorsWhenTheParticipatorsListIsEmpty() {
            Collection<Profile> participators = new ArrayList<>();

            ServiceResponse<?> serviceResponse = conversationService.createConversation(testParticipator1.getId(), participators);

            assertThat(serviceResponse.getBody()).isEqualTo(ServiceResponseMessages.INVALID_CONVERSATION_PARTICIPATORS);
        }

        @Test
        @DisplayName("does not return INVALID_PARTICIPATORS when the participators list contains one Profile")
        void doesNotReturnInvalidParticipatorsWhenTheParticipatorsListContainsOneProfile() throws Exception{
            Collection<Profile> participators = new ArrayList<>(Collections.singletonList(onlyUuid1()));

            ServiceResponse<?> serviceResponse = conversationService.createConversation(testParticipator1.getId(), participators);

            assertThat(serviceResponse.getBody()).isNotEqualTo(ServiceResponseMessages.INVALID_CONVERSATION_PARTICIPATORS);
        }

        @Nested
        @TestInstance(TestInstance.Lifecycle.PER_CLASS)
        @DisplayName("returns the correct Conversation based on the given participators list")
        class ReturnsTheCorrectConversationBasedOnTheGivenParticipatorsListTestCase {
            ArrayList<Profile> sParticipator1;
            ArrayList<Profile> sParticipators1and2;
            ArrayList<Profile> sParticipators1and2andRandR;

            ArrayList<Profile> sParticipators1and2andR2andR2;
            Conversation sConversation1;
            Conversation sConversation2;
            Conversation sConversations1and2;
            Conversation sConversations1and2andRandR;
            Conversation sConversations1and2andR2andR2;

            @BeforeAll
            void beforeAll() throws Exception {
                sParticipator1 = new ArrayList<>(Collections.singletonList(onlyUuid1()));
                ArrayList<Profile> sParticipator2 = new ArrayList<>(Collections.singletonList(onlyUuid2()));
                sParticipators1and2 = new ArrayList<>(Arrays.asList(
                        onlyUuid1(),
                        onlyUuid2()));
                sParticipators1and2andRandR = new ArrayList<>(Arrays.asList(
                    onlyUuid1(),
                    onlyUuid2(),
                    getDummyProfileWithOnlyUuid(UUID.randomUUID()),
                    getDummyProfileWithOnlyUuid(UUID.randomUUID())));
                sParticipators1and2andR2andR2 = new ArrayList<>(Arrays.asList(
                    onlyUuid1(),
                    onlyUuid2(),
                    getDummyProfileWithOnlyUuid(UUID.randomUUID()),
                    getDummyProfileWithOnlyUuid(UUID.randomUUID())));
                sConversation1 = new Conversation(sParticipator1);
                sConversation2 = new Conversation(sParticipator2);
                sConversations1and2 = new Conversation(sParticipators1and2);
                sConversations1and2andRandR = new Conversation(sParticipators1and2andRandR);
                sConversations1and2andR2andR2 = new Conversation(sParticipators1and2andR2andR2);
            }

            @Test
            @DisplayName("when many Conversations has been found and none has the exact participators list")
            void whenManyConversationsHasBeenFoundAndNoneHasTheExactParticipatorsList() throws Exception{
                when(conversationRepository.findByParticipatorsIdIn(anyList())).thenReturn(Arrays.asList(
                        sConversation1,
                        sConversation2,
                        sConversations1and2andRandR,
                        sConversations1and2andR2andR2
                ));
                when(conversationRepository.save(any())).thenReturn(sConversations1and2);
                Collection<Profile> participators = new ArrayList<>(Arrays.asList(onlyUuid1(), onlyUuid2()));

                ServiceResponse<?> serviceResponse = conversationService.createConversation(testParticipator1.getId(), participators);

                assertThat(serviceResponse.getBody()).isInstanceOf(Conversation.class);
                ArgumentCaptor<Conversation> conversationArgumentCaptor = ArgumentCaptor.forClass(Conversation.class);
                verify(conversationRepository).save(conversationArgumentCaptor.capture());
                assertThat(conversationArgumentCaptor.getValue().getParticipators())
                        .containsExactlyInAnyOrderElementsOf(sConversations1and2.getParticipators());
            }

            @Test
            @DisplayName("when many Conversations has been found and one has the exact participators list")
            void whenManyConversationsHasBeenFoundAndOneHasTheExactParticipatorsList() throws Exception {
                when(conversationRepository.findByParticipatorsIdIn(anyList())).thenReturn(Arrays.asList(
                        sConversation1,
                        sConversation2,
                        sConversations1and2,
                        sConversations1and2andRandR,
                        sConversations1and2andR2andR2
                ));
                Collection<Profile> participators = new ArrayList<>(Arrays.asList(onlyUuid1(), onlyUuid2()));

                ServiceResponse<?> serviceResponse = conversationService.createConversation(testParticipator1.getId(), participators);

                assertThat(serviceResponse.getBody()).isEqualTo(CONVERSATION_CONFLICT);
            }
        }
    }

    @Nested
    @DisplayName("Test if the addMessageToConversationById() method")
    class AddMessageToConversationByIdTest {
        @Test
        @DisplayName("returns INCORRECT_ID when the getById() method returns INCORRECT_ID")
        void returnsIncorrectIdWhenTheGetByIdMethodReturnsIncorrectId() {
            when(conversationRepository.findById(any())).thenReturn(Optional.empty());

            ServiceResponse<?> serviceResponse = conversationService
                    .addMessageToConversationById(
                            testParticipator1.getId(),
                            UUID.randomUUID().toString(),
                            testMessageFrom1);

            assertThat(serviceResponse).isEqualTo(INCORRECT_ID);
        }

        @Test
        @DisplayName("returns INCORRECT_ID when the given String is not a valid UUID")
        void returnsIncorrectIdWhenTheGivenStringIsNotAValidUuid() {
            ServiceResponse<?> serviceResponse = conversationService
                    .addMessageToConversationById(
                            testParticipator1.getId(),
                            UuidUtils.INVALID_UUID_AS_STRING,
                            testMessageFrom1);

            assertThat(serviceResponse).isEqualTo(INCORRECT_ID);
            verify(conversationRepository, never()).findById(any());
        }

        @Nested
        @DisplayName("returns UNAUTHORIZED when the requesting user's Profile")
        class ReturnsUnauthorizedWhenTheRequestingUserSProfileTestCase {
            @Test
            @DisplayName("is not a participator in the Conversation but sends the message from a participator")
            void isNotAParticipatorInTheConversationButSendsTheMessageFromAParticipator() {
                when(conversationRepository.findById(any())).thenReturn(Optional.ofNullable(testConversation));

                ServiceResponse<?> serviceResponse = conversationService
                        .addMessageToConversationById(
                                UUID.randomUUID(),
                                testConversation.getId(),
                                testMessageFrom1);

                assertThat(serviceResponse).isEqualTo(UNAUTHORIZED);
                assertThat(testConversation.getParticipators()).contains(testParticipator1);
            }

            @Test
            @DisplayName("is not a participator in the Conversation and sends the message from himself")
            void isNotAParticipatorInTheConversationAndSendsTheMessageFromHimself() throws Exception {
                when(conversationRepository.findById(any())).thenReturn(Optional.ofNullable(testConversation));
                Profile sProfile = getDummyProfileWithOnlyUuid(UUID.randomUUID());
                Message sMessage = new Message(sProfile.getId(), new Message.TempMessage("sTest"));

                ServiceResponse<?> serviceResponse = conversationService
                        .addMessageToConversationById(
                                sProfile.getId(),
                                testConversation.getId(),
                                sMessage);

                assertThat(serviceResponse).isEqualTo(UNAUTHORIZED);
                assertThat(testConversation.getParticipators()).contains(testParticipator1).contains(testParticipator2);
            }
        }

        @Test
        @DisplayName("returns CANNOT_ADD_MESSAGE when the Message could not be added to the Conversation")
        void returnsCannotAddMessageWhenTheMessageCouldNotBeAddedToTheConversation() {
            testConversation.addMessage(testMessageFrom1);
            when(conversationRepository.findById(any())).thenReturn(Optional.ofNullable(testConversation));

            ServiceResponse<?> serviceResponse = conversationService
                    .addMessageToConversationById(
                            testParticipator1.getId(),
                            testConversation.getId(),
                            testMessageFrom1);

            assertThat(serviceResponse.getBody()).isEqualTo(CANNOT_ADD_MESSAGE_TO_CONVERSATION);
            assertThat(testConversation.getParticipators()).contains(testParticipator1).contains(testParticipator2);
        }

        @Test
        @DisplayName("returns the Conversation when the Message has been added")
        void returnsTheConversationWhenTheMessageHasBeenAdded() {
            when(conversationRepository.findById(any())).thenReturn(Optional.ofNullable(testConversation));
            when(conversationRepository.save(any())).thenReturn(testConversation);

            ServiceResponse<?> serviceResponse = conversationService
                    .addMessageToConversationById(
                            testParticipator1.getId(),
                            testConversation.getId(),
                            testMessageFrom1);

            assertThat(serviceResponse.getBody()).isEqualTo(testConversation);
        }

        @Test
        @DisplayName("adds the Message to the Conversation before saving it with the repository")
        void addsTheMessageToTheConversationBeforeSavingItWithTheRepository() {
            when(conversationRepository.findById(any())).thenReturn(Optional.ofNullable(testConversation));

            ServiceResponse<?> serviceResponse = conversationService
                    .addMessageToConversationById(
                            testParticipator1.getId(),
                            testConversation.getId(),
                            testMessageFrom1);

            ArgumentCaptor<Conversation> conversationArgumentCaptor = ArgumentCaptor.forClass(Conversation.class);
            verify(conversationRepository).save(conversationArgumentCaptor.capture());
            assertThat(conversationArgumentCaptor.getValue().getMessages()).contains(testMessageFrom1);
        }

        @Test
        @DisplayName("returns the Conversation with the added Message when the Conversation contains other Messages")
        void returnsTheConversationWithTheAddedMessageWhenTheConversationContainsOtherMessages() {
            when(conversationRepository.findById(any())).thenReturn(Optional.ofNullable(testConversation));

            ServiceResponse<?> serviceResponse = conversationService
                    .addMessageToConversationById(
                            testParticipator1.getId(),
                            testConversation.getId(),
                            testMessageFrom1);

            ArgumentCaptor<Conversation> conversationArgumentCaptor = ArgumentCaptor.forClass(Conversation.class);
            verify(conversationRepository).save(conversationArgumentCaptor.capture());
            assertThat(conversationArgumentCaptor.getValue().getMessages()).contains(testMessageFrom1);
        }

        @Test
        @DisplayName("adds the Message to the Conversation before saving it with the repository when others Messages")
        void addsTheMessageToTheConversationBeforeSavingItWithTheRepositoryWhenOthersMessages() {
            Message sMessage1 = new Message(testParticipator1.getId(), new Message.TempMessage("sTest"));
            Message sMessage2 = new Message(testParticipator2.getId(), new Message.TempMessage("sTest"));
            testConversation.addMessage(sMessage1);
            testConversation.addMessage(sMessage2);
            when(conversationRepository.findById(any())).thenReturn(Optional.ofNullable(testConversation));

            ServiceResponse<?> serviceResponse = conversationService
                    .addMessageToConversationById(
                            testParticipator1.getId(),
                            testConversation.getId(),
                            testMessageFrom1);

            ArgumentCaptor<Conversation> conversationArgumentCaptor = ArgumentCaptor.forClass(Conversation.class);
            verify(conversationRepository).save(conversationArgumentCaptor.capture());
            assertThat(conversationArgumentCaptor.getValue().getMessages()).contains(testMessageFrom1);
            assertThat(testConversation.getParticipators()).contains(testParticipator1).contains(testParticipator2);
        }
    }
}