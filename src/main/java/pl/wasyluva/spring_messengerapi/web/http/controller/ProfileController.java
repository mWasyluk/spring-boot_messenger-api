package pl.wasyluva.spring_messengerapi.web.http.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wasyluva.spring_messengerapi.data.service.ProfileService;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Profile;
import pl.wasyluva.spring_messengerapi.web.http.support.PrincipalService;

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

    @GetMapping("/all")
    public ResponseEntity<?> getAllUserProfiles(){
        return profileService.getAllProfiles().getResponseEntity();
    }

    @GetMapping
    public ResponseEntity<?> getPrincipalProfile(){
        return profileService.getProfileByAccountId(principalService.getPrincipalAccountId())
                .getResponseEntity();
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

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteProfile(){
        return profileService.deleteProfile(principalService.getPrincipalAccountId())
                .getResponseEntity();
    }
}
