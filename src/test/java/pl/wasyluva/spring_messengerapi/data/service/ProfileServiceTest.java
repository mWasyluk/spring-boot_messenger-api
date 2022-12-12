package pl.wasyluva.spring_messengerapi.data.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wasyluva.spring_messengerapi.data.repository.AccountRepository;
import pl.wasyluva.spring_messengerapi.data.repository.ProfileRepository;
import pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponse;
import pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponseMessages;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Account;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Profile;
import pl.wasyluva.spring_messengerapi.domain.userdetails.ProfileAvatar;
import pl.wasyluva.spring_messengerapi.util.UuidUtils;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponse.INCORRECT_ID;
import static pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponse.UNAUTHORIZED;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private AccountRepository accountRepository;
    private ProfileService profileService;

    static String testFirstName = "Test";
    static String testLastName = "Testy";
    static Date testBirthdate = new Date();
    Profile testProfile;

    @BeforeEach
    void setUp() {
        profileService = new ProfileService(profileRepository, accountRepository);
        testProfile = new Profile(testFirstName, testLastName, testBirthdate);
    }

    @Nested
    @DisplayName("Test if the getProfileById() method")
    class GetProfileByIdMethodTest {
        @Test
        @DisplayName("returns the Profile when it exists with the given UUID")
        void returnsTheProfileWhenItExistsWithTheGivenId() {
            when(profileRepository.findById(testProfile.getId())).thenReturn(Optional.of(testProfile));

            ServiceResponse<?> serviceResponse = profileService.getProfileById(testProfile.getId());

            assertThat(serviceResponse.getBody()).isEqualTo(testProfile);
        }

        @Test
        @DisplayName("returns INCORRECT_ID when the profile with the given UUID does not exist")
        void returnsNull() {
            when(profileRepository.findById(testProfile.getId())).thenReturn(Optional.empty());

            ServiceResponse<?> serviceResponse = profileService.getProfileById(testProfile.getId());

            assertThat(serviceResponse).isEqualTo(INCORRECT_ID);
        }

        @Test
        @DisplayName("returns the Profile when it exists with the given UUID as a String")
        void returnsTheProfileWhenItExistsWithTheGivenUuidAsAString() {
            when(profileRepository.findById(testProfile.getId())).thenReturn(Optional.of(testProfile));

            ServiceResponse<?> serviceResponse = profileService.getProfileById(testProfile.getId().toString());

            assertThat(serviceResponse.getBody()).isEqualTo(testProfile);
        }


        @Test
        @DisplayName("returns INCORRECT_ID when the Profile with the given UUID as a String does not exist")
        void returnsIncorrectIdWhenTheProfileWithTheGivenUuidAsAStringDoesNotExist() {
            ServiceResponse<?> serviceResponse = profileService.getProfileById(UuidUtils.INVALID_UUID_AS_STRING);

            assertThat(serviceResponse).isEqualTo(INCORRECT_ID);
            verify(profileRepository, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("Test if the createProfile() method")
    class CreateProfileMethodTest {
        @Test
        @DisplayName("returns INCORRECT_ID when the Account with the given UUID does not exist")
        void returnsIncorrectIdWhenTheAccountWithTheGivenUuidDoesNotExist(){
            when(accountRepository.findById(any())).thenReturn(Optional.empty());

            ServiceResponse<?> serviceResponse = profileService.createProfile(UUID.randomUUID(), testProfile);

            assertThat(serviceResponse).isEqualTo(INCORRECT_ID);
            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("returns ALREADY_EXISTS when the Account with the given UUID already has a Profile assigned")
        void returnsAlreadyExistsWhenTheAccountWithTheGivenUuidAlreadyHasAProfileAssigned() {
            Account account = new Account();
            account.setProfile(testProfile);
            when(accountRepository.findById(any())).thenReturn(Optional.of(account));

            ServiceResponse<?> serviceResponse = profileService.createProfile(UUID.randomUUID(), testProfile);

            assertThat(serviceResponse.getBody()).isEqualTo(ServiceResponseMessages.ACCOUNT_PROFILE_ALREADY_EXISTS);
            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("updates the Account with the given Profile")
        void updatesTheAccountWithTheGivenProfile() {
            Account account = new Account();
            account.setProfile(null);
            when(accountRepository.findById(any())).thenReturn(Optional.of(account));
            when(accountRepository.save(account)).thenReturn(account);

            ServiceResponse<?> serviceResponse = profileService.createProfile(UUID.randomUUID(), testProfile);

            ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
            verify(accountRepository).save(accountArgumentCaptor.capture());
            assertThat(accountArgumentCaptor.getValue().getProfile()).isEqualTo(testProfile);
        }

        @Test
        @DisplayName("saves and returns the Profile when it has been assigned to the Account")
        void savesAndReturnsTheProfileWhenItHasBeenAssignedToTheAccount() {
            Account account = new Account();
            account.setProfile(null);
            when(accountRepository.findById(any())).thenReturn(Optional.of(account));
            when(accountRepository.save(account)).thenReturn(account);

            ServiceResponse<?> serviceResponse = profileService.createProfile(UUID.randomUUID(), testProfile);

            assertThat(serviceResponse.getBody()).isInstanceOf(Profile.class);
            verify(accountRepository).save(any());
        }
    }

    @Nested
    @DisplayName("Test if the updateProfile() method")
    class UpdateProfileTest {
        @Test
        @DisplayName("returns INCORRECT_ID when the Profile update does not contain a UUID")
        void returnsIncorrectIdWhenTheProfileUpdateDoesNotContainAUuid() {
            testProfile.setId(null);

            ServiceResponse<?> serviceResponse = profileService.updateProfile(testProfile.getId(), testProfile);

            assertThat(serviceResponse).isEqualTo(INCORRECT_ID);
            verify(profileRepository, never()).findById(any());
        }

        @Test
        @DisplayName("returns INCORRECT_ID when the Profile with the given UUID does not exist")
        void returnsIncorrectIdWhenTheProfileWithTheGivenUuidDoesNotExist() {
            when(profileRepository.findById(any())).thenReturn(Optional.empty());

            ServiceResponse<?> serviceResponse = profileService.updateProfile(testProfile.getId(), testProfile);

            assertThat(serviceResponse).isEqualTo(INCORRECT_ID);
        }

        @Test
        @DisplayName("returns UNAUTHORIZED when the given Profile's UUID is different from the Profile's UUID of the requesting user")
        void returnsUnauthorizedWhenTheGivenProfileSUuidIsDifferentFromTheProfileSUuidOfTheRequestingUser() {
            when(profileRepository.findById(any())).thenReturn(Optional.of(testProfile));

            ServiceResponse<?> serviceResponse = profileService.updateProfile(UUID.randomUUID(), testProfile);

            assertThat(serviceResponse).isEqualTo(UNAUTHORIZED);
        }

        @Test
        @DisplayName("updates the Profile")
        void updatesTheProfile() {
            when(profileRepository.findById(any())).thenReturn(Optional.of(testProfile));
            // data preparation
            ProfileAvatar sAvatar = new ProfileAvatar("http://test.avatar");
            String sFirstName = "sTest";
            String sLastName = "sTest";
            Date sBirthdate = new Date();
            // test object set up
            Profile sProfile = new Profile(sFirstName, sLastName, sBirthdate);
            sProfile.setId(testProfile.getId());
            sProfile.setAvatar(sAvatar);

            ServiceResponse<?> serviceResponse = profileService.updateProfile(testProfile.getId(), sProfile);

            ArgumentCaptor<Profile> profileArgumentCaptor = ArgumentCaptor.forClass(Profile.class);
            verify(profileRepository).save(profileArgumentCaptor.capture());
            assertThat(profileArgumentCaptor.getValue().getAvatar()).isEqualTo(sAvatar);
            assertThat(profileArgumentCaptor.getValue().getFirstName()).isEqualTo(sFirstName);
            assertThat(profileArgumentCaptor.getValue().getLastName()).isEqualTo(sLastName);
            assertThat(profileArgumentCaptor.getValue().getBirthDate()).isEqualTo(sBirthdate);
        }

        @Test
        @DisplayName("does not update the Profile with the given null values")
        void doesNotUpdateTheProfileWithTheGivenNullValues() {
            when(profileRepository.findById(any())).thenReturn(Optional.of(testProfile));
            // data preparation
            ProfileAvatar sAvatar = null;
            String sFirstName = null;
            String sLastName = null;
            Date sBirthdate = null;
            // test object set up
            Profile sProfile = new Profile(sFirstName, sLastName, sBirthdate);
            sProfile.setId(testProfile.getId());
            sProfile.setAvatar(sAvatar);

            ServiceResponse<?> serviceResponse = profileService.updateProfile(testProfile.getId(), sProfile);

            ArgumentCaptor<Profile> profileArgumentCaptor = ArgumentCaptor.forClass(Profile.class);
            verify(profileRepository).save(profileArgumentCaptor.capture());
            assertThat(profileArgumentCaptor.getValue().getAvatar()).isEqualTo(testProfile.getAvatar());
            assertThat(profileArgumentCaptor.getValue().getFirstName()).isEqualTo(testProfile.getFirstName());
            assertThat(profileArgumentCaptor.getValue().getLastName()).isEqualTo(testProfile.getLastName());
            assertThat(profileArgumentCaptor.getValue().getBirthDate()).isEqualTo(testProfile.getBirthDate());
        }

        @Test
        @DisplayName("saves and returns the Profile")
        void savesAndReturnsTheProfile() {
            when(profileRepository.findById(any())).thenReturn(Optional.of(testProfile));
            when(profileRepository.save(any())).thenReturn(testProfile);

            ServiceResponse<?> serviceResponse = profileService.updateProfile(testProfile.getId(), testProfile);

            assertThat(serviceResponse.getBody()).isEqualTo(testProfile);
        }
    }
}