package pl.wasyluva.spring_messengerapi.data.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.wasyluva.spring_messengerapi.data.repository.UserProfileRepository;
import pl.wasyluva.spring_messengerapi.domain.userdetails.UserProfile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;

    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    // TODO: Return snips of profiles objects (DTO)
    public List<UserProfile> getAllUserProfiles(){
        return userProfileRepository.findAll();
    }


    // TODO: Add a requestingUser parameter to the method and check if he is on 'friend list'
    //  if true -> return a full object
    //  if false -> return a snip of the object (DTO)
    public UserProfile getUserProfileById(@NonNull UUID userId){
        return userProfileRepository.findById(userId).orElse(null);
    }

    // TODO: Add a requestingUser parameter to the method and check if he is the owner of the profile
    public UserProfile updateUserProfile(@NonNull UserProfile updatedUserProfile){
        if (updatedUserProfile.getId() == null){
            log.debug("UserProfile provided as updated has to have an ID");
            return null;
        }
        Optional<UserProfile> byId = userProfileRepository.findById(updatedUserProfile.getId());
        if (!byId.isPresent()) {
            log.debug("UserProfile with ID " + updatedUserProfile.getId() + " does not exist");
            return null;
        }
        UserProfile toPersistUserProfile = updateAllUserProfileFields(byId.get(), updatedUserProfile);

        log.debug("Profile updated");
        return userProfileRepository.save(toPersistUserProfile);
    }

    private UserProfile updateAllUserProfileFields(UserProfile oldState, UserProfile updatedState){
        if (updatedState.getAvatar() != null){
            oldState.setAvatar(updatedState.getAvatar());
        } if (updatedState.getFirstName() != null) {
            oldState.setFirstName(updatedState.getFirstName());
        } if (updatedState.getLastName() != null) {
            oldState.setLastName(updatedState.getLastName());
        } if (updatedState.getBirthDate() != null) {
            oldState.setBirthDate(updatedState.getBirthDate());
        }
        return oldState;
    }
}
