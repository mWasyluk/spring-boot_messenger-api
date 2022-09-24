package pl.wasyluva.spring_messengerapi.data.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.wasyluva.spring_messengerapi.data.repository.ProfileRepository;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Profile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ProfileService {
    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    // TODO: Return snips of profiles objects (DTO)
    public List<Profile> getAllProfiles(){
        return profileRepository.findAll();
    }


    // TODO: Add a requestingUser parameter to the method and check if he is on 'friend list'
    //  if true -> return a full object
    //  if false -> return a snip of the object (DTO)
    public Profile getProfileById(@NonNull UUID userId){
        return profileRepository.findById(userId).orElse(null);
    }

    // TODO: Add a requestingUser parameter to the method and check if he is the owner of the profile
    public Profile updateProfile(@NonNull Profile updatedProfile){
        if (updatedProfile.getId() == null){
            log.debug("UserProfile provided as updated has to have an ID");
            return null;
        }
        Optional<Profile> byId = profileRepository.findById(updatedProfile.getId());
        if (!byId.isPresent()) {
            log.debug("UserProfile with ID " + updatedProfile.getId() + " does not exist");
            return null;
        }
        Profile toPersistProfile = updateAllProfileFields(byId.get(), updatedProfile);

        log.debug("Profile updated");
        return profileRepository.save(toPersistProfile);
    }

    private Profile updateAllProfileFields(Profile oldState, Profile updatedState){
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
