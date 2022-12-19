package pl.wasyluva.spring_messengerapi.data.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wasyluva.spring_messengerapi.domain.message.Message;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findAllByConversationIdOrderBySentDateDesc(UUID conversationId, Pageable pageable);
}
