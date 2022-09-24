package pl.wasyluva.spring_messengerapi.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wasyluva.spring_messengerapi.data.service.ProfileService;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Profile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/profiles")
public class UserProfileController {
    // TODO: Return more self-descriptive HTTP status codes

    private final ProfileService profileService;

    public UserProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<List<Profile>> getAllUserProfiles(){
        return new ResponseEntity<>(profileService.getAllProfiles(), HttpStatus.OK);
    }

    @GetMapping(value = "/{userUuid}")
    public ResponseEntity<Profile> getUserProfileByUserId(@PathVariable String userUuid){
        try {
            UUID.fromString(userUuid);
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Profile profileById = profileService.getProfileById(UUID.fromString(userUuid));

        return profileById != null ?
                new ResponseEntity<>(profileById, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PatchMapping("/update")
    public ResponseEntity<Profile> updateUserProfile(@RequestBody Profile profile, HttpServletRequest request){
        Profile updatedProfile = profileService.updateProfile(profile);
        return updatedProfile != null ?
                new ResponseEntity<>(updatedProfile, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
