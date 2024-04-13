package edu.sjsu.cmpe272.simpleblog.server.repositories;
import edu.sjsu.cmpe272.simpleblog.server.entities.MessageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageRequest, Integer> {


}
