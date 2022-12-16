package pl.wasyluva.spring_messengerapi.web.http.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping("/")
    public String getHome(){
        return "Welcome at the main endpoint! (unsecured)";
    }

    @GetMapping("/test")
    public String getTest(){
        return "Welcome at the Test endpoint!";
    }
}
