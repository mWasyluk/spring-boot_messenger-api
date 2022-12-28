package pl.wasyluva.spring_messengerapi.web.http.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wasyluva.spring_messengerapi.data.service.ProfileService;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Profile;
import pl.wasyluva.spring_messengerapi.web.http.support.PrincipalService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/profiles")
public class ProfileController {
    // TODO: Return more self-descriptive HTTP status codes

    private final ProfileService profileService;
    private final PrincipalService principalService;

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

    @GetMapping("/search")
    public ResponseEntity<?> searchProfilesByName(@RequestParam(name = "q") String query){
        return profileService.getProfilesByNameQuery(query)
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
