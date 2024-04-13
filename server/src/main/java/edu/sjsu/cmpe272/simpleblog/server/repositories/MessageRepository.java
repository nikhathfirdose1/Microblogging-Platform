package edu.sjsu.cmpe272.simpleblog.server.repositories;
import edu.sjsu.cmpe272.simpleblog.server.entities.MessageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageRequest, Integer> {

    @Query("SELECT m FROM MessageRequest m WHERE m.messageId > :startingId ORDER BY m.messageId ASC")
    List<MessageRequest> findNextMessages(@Param("startingId") int startingId);


}
