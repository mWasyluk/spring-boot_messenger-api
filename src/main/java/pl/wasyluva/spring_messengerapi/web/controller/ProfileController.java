package pl.wasyluva.spring_messengerapi.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wasyluva.spring_messengerapi.data.service.ProfileService;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Profile;
import pl.wasyluva.spring_messengerapi.web.controller.support.PrincipalService;

@RestController
@RequestMapping("/profiles")
public class ProfileController {
    // TODO: Return more self-descriptive HTTP status codes

    private final ProfileService profileService;
    private final PrincipalService principalService;

    public ProfileController(ProfileService profileService, PrincipalService principalService) {
        this.profileService = profileService;
        this.principalService = principalService;
    }

    @GetMapping
    public ResponseEntity<?> getAllUserProfiles(){
        return profileService.getAllProfiles().getResponseEntity();
    }

    @GetMapping("/{userUuid}")
    public ResponseEntity<?> getUserProfileByUserId(@PathVariable String userUuid){
        return profileService.getProfileById(userUuid)
                .getResponseEntity();
    }

    @PostMapping("/create")
    public ResponseEntity<?> createProfile(@RequestBody Profile newProfile){
        return profileService.createProfile(principalService.getPrincipalAccountId(), newProfile)
                .getResponseEntity();
    }

    @PatchMapping("/update")
    public ResponseEntity<?> updateUserProfile(@RequestBody Profile profile){
        return profileService.updateProfile(principalService.getPrincipalProfileId(), profile)
                .getResponseEntity();
    }
}
