package edu.sjsu.cmpe272.simpleblog.server.controller;
import edu.sjsu.cmpe272.simpleblog.server.entities.MessageRequest;
import edu.sjsu.cmpe272.simpleblog.server.entities.UserRequest;
import edu.sjsu.cmpe272.simpleblog.server.repositories.MessageRepository;
import edu.sjsu.cmpe272.simpleblog.server.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class Welcome {

   @Autowired
    UserRepository userRepository;

   @Autowired
    MessageRepository messageRepository;

    @GetMapping("/")
    ResponseEntity<String> getWelcome() {
        return ResponseEntity.ok("Welcome!");
    }

    @PostMapping("/messages/create")
    public ResponseEntity<Map<String, Object>> createMessage(@RequestBody MessageRequest request) {
        try {
            // Calculate message hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] messageHash = digest.digest(request.getMessage().getBytes());

            // Retrieve user's public key from the repository
            UserRequest user = userRepository.findByUser(request.getAuthor());
            if (user == null || user.getPublicKey() == null || user.getPublicKey().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found or public key is empty"));
            }
            String pkString = user.getPublicKey();

            // Decode public key string and generate public key object
            byte[] pkBytes = Base64.getDecoder().decode(pkString);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(pkBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(spec);

            // Verify the signature
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(publicKey);
            verifier.update(messageHash);
            boolean isVerified = verifier.verify(Base64.getDecoder().decode(request.getSignature()));

            // Save message if signature is verified
            if (isVerified) {
                messageRepository.save(request);
                return ResponseEntity.ok(Map.of("message-id", request.getMessageId()));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "signature didn't match"));
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }


    @PostMapping("/user/create")
    public ResponseEntity<Map<String, String>> createUser(@RequestBody UserRequest request) {
        if (userRepository.findByUser(request.getUser()) != null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User already exists"));
        } else {
            userRepository.save(request);
            return ResponseEntity.ok(Map.of("message", "welcome"));
        }

    }


    @PostMapping("/messages/list")
    public ResponseEntity<List<MessageRequest>> listMessages(@RequestBody Map<String, Object> request) {
        int startingId = (int) request.getOrDefault("starting", -1);
        int count = (int) request.getOrDefault("count", 10);

        List<MessageRequest> messages = fetchMessages(count, startingId);

        return ResponseEntity.ok(messages);
    }

    @GetMapping("/user/{username}/public-key")
    public ResponseEntity<?> getUserPublicKey(@PathVariable String username) {
        String publicKeyString = String.valueOf(userRepository.findByUser(username));  //db.get(username);

        if (publicKeyString != null) {
            try {

                byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyString);
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PublicKey publicKey = keyFactory.generatePublic(keySpec);

                // Convert the public key to PEM format
                String pemPublicKey = convertToPEM(publicKey);

                return ResponseEntity.ok(pemPublicKey);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Error encoding public key"));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        }
    }

    private String convertToPEM(PublicKey publicKey) {
        String base64EncodedKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        StringBuilder pemBuilder = new StringBuilder("""
                -----BEGIN PUBLIC KEY-----
                """);
        int length = base64EncodedKey.length();
        int startIndex = 0;
        while (startIndex < length) {
            int endIndex = Math.min(startIndex + 64, length);
            pemBuilder.append(base64EncodedKey, startIndex, endIndex).append("\n");
            startIndex = endIndex;
        }
        pemBuilder.append("""
                -----END PUBLIC KEY-----
                """);
        return pemBuilder.toString();
    }


    private List<MessageRequest> fetchMessages(int count, int startingId) {


//        if(startingId == -1) {
//            return messages.stream()
//                    .skip(Math.max(0, messages.size() - count))
//                    .limit(count)
//                    .collect(Collectors.toList());
//        }else{
//            return messages.stream()
//                    .skip(Math.max(0, messages.size() - startingId))
//                    .limit(count)
//                    .collect(Collectors.toList());
//        }

        if (count > 20) {
            throw new IllegalArgumentException("Count cannot exceed 20");
        }

        List<MessageRequest> fetchedMessages = messageRepository.findAll();

        List<MessageRequest> filteredMessages;

        if (startingId == -1) {
            filteredMessages = fetchedMessages.stream()
                    .limit(count)
                    .collect(Collectors.toList());
            Collections.reverse(filteredMessages);
        } else {
            int startIndex = 0;
            for (int i = 0; i < fetchedMessages.size(); i++) {
                if (fetchedMessages.get(i).getMessageId() == startingId) {
                    startIndex = i;
                    break;
                }
            }

            filteredMessages = fetchedMessages.stream()
                    .skip(startIndex)
                    .limit(count)
                    .collect(Collectors.toList());
        }

        return filteredMessages;

    }
}