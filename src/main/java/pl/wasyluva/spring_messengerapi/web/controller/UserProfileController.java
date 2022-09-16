package pl.wasyluva.spring_messengerapi.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.wasyluva.spring_messengerapi.data.repository.UserProfileRepository;
import pl.wasyluva.spring_messengerapi.domain.userdetails.UserProfile;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserProfileController {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @GetMapping
    public ResponseEntity<List<UserProfile>> getAllUserProfiles(){
        return new ResponseEntity<>(userProfileRepository.findAll(), HttpStatus.OK);
    }
}
