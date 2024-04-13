package edu.sjsu.cmpe272.simpleblog.server.entities;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class UserRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Integer id;

    @Column(unique = true, name="user_name")
    private String user;

    @Column(length = 2000)
    @JsonProperty("public-key")
    private String publicKey;
}
