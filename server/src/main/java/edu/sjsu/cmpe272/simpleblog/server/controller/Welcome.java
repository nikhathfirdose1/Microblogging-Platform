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

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
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

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] messageHash = digest.digest(request.getMessage().getBytes());

            UserRequest user = userRepository.findByUser(request.getAuthor());
            if (user == null || user.getPublicKey() == null || user.getPublicKey().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found or public key is empty"));
            }
            String pkString = user.getPublicKey();

            byte[] pkBytes = Base64.getDecoder().decode(pkString);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(pkBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(spec);

            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(publicKey);
            verifier.update(messageHash);
            boolean isVerified = verifier.verify(Base64.getDecoder().decode(request.getSignature()));

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
        UserRequest user = userRepository.findByUser(username);
        if (user != null && user.getPublicKey() != null && !user.getPublicKey().isEmpty()) {
            try {
                String publicKeyString = user.getPublicKey();
                byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyString);
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PublicKey publicKey = keyFactory.generatePublic(keySpec);

                String pemPublicKey = convertToPEM(publicKey);

                return ResponseEntity.ok(pemPublicKey);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Error encoding public key"));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Invalid Base64 encoding in public key"));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found or public key is empty"));
        }
    }

    private String convertToPEM(PublicKey publicKey) {
        String base64EncodedKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        StringBuilder pemBuilder = new StringBuilder("""
                -----BEGIN PUBLIC KEY----- \n
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
        List<MessageRequest> fetchedMessages = messageRepository.findAll();

        List<MessageRequest> filteredMessages;
        int remainingCount = count;
        int offset = 0;

        while (remainingCount > 0) {
            int batchSize = Math.min(20, remainingCount);
            List<MessageRequest> batchMessages = messageRepository.findNextMessages(startingId);

            fetchedMessages.addAll(batchMessages);

            remainingCount -= batchSize;
            offset += batchSize;

            if (batchMessages.size() < batchSize) {
                break;
            }
        }


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
//private List<MessageRequest> fetchMessages(int count, int startingId) {
//    List<MessageRequest> fetchedMessages = new ArrayList<>();
//    int remainingCount = count;
//    int offset = 0;
//
//    while (remainingCount > 0) {
//        int batchSize = Math.min(20, remainingCount);
//        List<MessageRequest> batchMessages = messageRepository.findNextMessages(startingId);
//
//        fetchedMessages.addAll(batchMessages);
//
//        remainingCount -= batchSize;
//        offset += batchSize;
//
//        if (batchMessages.size() < batchSize) {
//            break;
//        }
//    }
//
//    if (startingId == -1) {
//        Collections.reverse(fetchedMessages);
//    }
//else {
//        int startIndex = 0;
//        for (int i = 0; i < fetchedMessages.size(); i++) {
//            if (fetchedMessages.get(i).getMessageId() == startingId) {
//                startIndex = i;
//                break;
//            }
//        }
//        fetchedMessages = fetchedMessages.stream()
//                    .skip(startIndex)
//                    .limit(count)
//                    .collect(Collectors.toList());
//    }
//
//    return fetchedMessages;
//}



}