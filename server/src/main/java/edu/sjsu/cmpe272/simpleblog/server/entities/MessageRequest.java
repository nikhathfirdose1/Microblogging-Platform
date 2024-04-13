package edu.sjsu.cmpe272.simpleblog.server.entities;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@Entity
@Data
public class MessageRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("message-id")
    private Integer messageId;
    private String date;
    private String author;
    @Column(length = 2000)
    private String message;
    @Column(length = 10000)
    private String attachment;
    @Lob
    @Column(length = 2000)
    private String signature;

}
