package pl.wasyluva.spring_messengerapi.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Profile;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    List<Profile> findAllByFirstNameStartsWithIgnoreCaseAndLastNameStartsWithIgnoreCase (String firstNameQuery, String lastNameQuery);
}
