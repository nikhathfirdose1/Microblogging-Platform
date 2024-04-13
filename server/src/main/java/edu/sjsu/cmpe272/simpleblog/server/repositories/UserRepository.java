package edu.sjsu.cmpe272.simpleblog.server.repositories;

import edu.sjsu.cmpe272.simpleblog.server.entities.MessageRequest;
import edu.sjsu.cmpe272.simpleblog.server.entities.UserRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserRequest, Integer>{
    UserRequest findByUser(String user);

}
