package pl.wasyluva.spring_messengerapi.data.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import pl.wasyluva.spring_messengerapi.data.repository.AccountRepository;
import pl.wasyluva.spring_messengerapi.data.repository.ProfileRepository;
import pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponse;
import pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponseMessages;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Account;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Profile;
import pl.wasyluva.spring_messengerapi.util.DebugLogger;
import pl.wasyluva.spring_messengerapi.util.UuidUtils;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final AccountRepository accountRepository;

    // TODO: Return snips of profiles objects (DTO)
    public ServiceResponse<?> getAllProfiles(){
        return new ServiceResponse<>(profileRepository.findAll(), HttpStatus.OK);
    }

    public ServiceResponse<?> getProfileByAccountId(@NonNull UUID accountUuid){
        Optional<Account> optionalAccount = accountRepository.findById(accountUuid);
        if (!optionalAccount.isPresent()){
            DebugLogger.logObjectNotFound(accountUuid.toString());
            return ServiceResponse.INCORRECT_ID;
        }

        if (optionalAccount.get().getProfile() == null){
            DebugLogger.logObjectNotFoundByName("Profile");
            return ServiceResponse.NOT_FOUND;
        }

        return new ServiceResponse<>(optionalAccount.get().getProfile(), HttpStatus.OK);
    }

    // TODO: Add a requestingUser parameter to the method and check if he is on 'friend list'
    //  if true -> return a full object
    //  if false -> return a snip of the object (DTO)
    public ServiceResponse<?> getProfileById(@NonNull UUID profileUuid){
        Optional<Profile> byId = profileRepository.findById(profileUuid);
        if (!byId.isPresent()) {
            return ServiceResponse.INCORRECT_ID;
        }
        return new ServiceResponse<>(byId.get(), HttpStatus.OK);
    }

    public ServiceResponse<?> getProfileById(@NonNull String profileStringUuid){
        if (!UuidUtils.isStringCorrectUuid(profileStringUuid)){
            return ServiceResponse.INCORRECT_ID;
        }
        return getProfileById(UUID.fromString(profileStringUuid));
    }

    public ServiceResponse<?> createProfile(@NonNull UUID accountId, @NonNull Profile profile){
        Optional<Account> byId = accountRepository.findById(accountId);
        if (!byId.isPresent()){
            log.debug("Account with ID " + accountId + " does not exist");
            return ServiceResponse.INCORRECT_ID;
        }

        if (byId.get().getProfile() != null){
            log.debug("Account with ID " + accountId + " already has Profile assigned");
            return new ServiceResponse<>(ServiceResponseMessages.ACCOUNT_PROFILE_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        Account account = byId.get();
        account.setProfile(profile);

        log.debug("Account with ID " + accountId + " has now Profile with ID " + profile.getId() + " assigned");
        return new ServiceResponse<>(accountRepository.save(account).getProfile(), HttpStatus.CREATED);
    }

    // TODO: Add a requestingUser parameter to the method and check if he is the owner of the profile
    public ServiceResponse<?> updateProfile(@NonNull UUID requestingProfileUuid, @NonNull Profile updatedProfile){
        if (updatedProfile.getId() == null){
            log.debug("UserProfile provided as updated has to have an ID");
            return ServiceResponse.INCORRECT_ID;
        }
        Optional<Profile> byId = profileRepository.findById(updatedProfile.getId());
        if (!byId.isPresent()) {
            log.debug("UserProfile with ID " + updatedProfile.getId() + " does not exist");
            return ServiceResponse.INCORRECT_ID;
        }

        if (!updatedProfile.getId().equals(requestingProfileUuid)){
            log.debug("Profile's UUID of the requesting user is different from the UUID of the Profile that has been found");
            return ServiceResponse.UNAUTHORIZED;
        }

        Profile toPersistProfile = updateAllProfileFields(byId.get(), updatedProfile);

        log.debug("Profile updated");
        return new ServiceResponse<>(profileRepository.save(toPersistProfile), HttpStatus.OK);
    }

    private Profile updateAllProfileFields(@NonNull Profile oldState, @NonNull Profile updatedState){
        if (updatedState.getAvatar() != null){
            oldState.setAvatar(updatedState.getAvatar());
        } if (updatedState.getFirstName() != null) {
            oldState.setFirstName(updatedState.getFirstName());
        } if (updatedState.getLastName() != null) {
            oldState.setLastName(updatedState.getLastName());
        } if (updatedState.getBirthDate() != null) {
            oldState.setBirthDate(updatedState.getBirthDateAsString());
        }
        return oldState;
    }

    public ServiceResponse<?> deleteProfile(UUID principalAccountId) {
        Optional<Account> accountOptional = this.accountRepository.findById(principalAccountId);
        if (!accountOptional.isPresent()){
            return ServiceResponse.NOT_FOUND;
        }
        accountOptional.get().setProfile(null);
        this.accountRepository.save(accountOptional.get());

        return ServiceResponse.OK;
    }
}
