package pl.wasyluva.spring_messengerapi.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wasyluva.spring_messengerapi.data.service.UserProfileService;
import pl.wasyluva.spring_messengerapi.domain.userdetails.UserProfile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/profiles")
public class UserProfileController {
    // TODO: Return more self-descriptive HTTP status codes

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public ResponseEntity<List<UserProfile>> getAllUserProfiles(){
        return new ResponseEntity<>(userProfileService.getAllUserProfiles(), HttpStatus.OK);
    }

    @GetMapping(value = "/{userUuid}")
    public ResponseEntity<UserProfile> getUserProfileByUserId(@PathVariable String userUuid){
        try {
            UUID.fromString(userUuid);
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        UserProfile userProfileById = userProfileService.getUserProfileById(UUID.fromString(userUuid));

        return userProfileById != null ?
                new ResponseEntity<>(userProfileById, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PatchMapping("/update")
    public ResponseEntity<UserProfile> updateUserProfile(@RequestBody UserProfile userProfile, HttpServletRequest request){
        UserProfile updatedUserProfile = userProfileService.updateUserProfile(userProfile);
        return updatedUserProfile != null ?
                new ResponseEntity<>(updatedUserProfile, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
