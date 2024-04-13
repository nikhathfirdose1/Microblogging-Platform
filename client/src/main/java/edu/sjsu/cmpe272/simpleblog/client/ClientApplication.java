package edu.sjsu.cmpe272.simpleblog.client;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.time.LocalDateTime;


import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SpringBootApplication
@Command
public class ClientApplication implements CommandLineRunner, ExitCodeGenerator {


    @Data
    @AllArgsConstructor
    public static class IdandKey {
        public String userId;
        public PrivateKey privateKey;

    }

    @Data
    public static class MessageRequestClient {
        @JsonProperty("message-id")
        private Integer messageId;
        private String date;
        private String author;
        private String message;
        private String attachment;
        private String signature;
    }

    @Data
    @AllArgsConstructor
    public static class UserRequestClient {
        private Integer id;
        private String user;
        private String publicKey;
    }


    @Autowired
    CommandLine.IFactory iFactory;

    @Autowired
    private ConfigurableApplicationContext context;

    private final RestTemplate restTemplate = new RestTemplate();

    private String url = "http://localhost:8080";



    @Command(name = "post", description = "Post a message to the server")
    public int post(@Parameters String message, @Parameters(defaultValue = "null") String attachment) {


        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("date", LocalDateTime.now().toString());
            requestBody.put("message", message);
            if (attachment != null && !attachment.equals("null")) {
                File attachmentFile = new File(attachment);
                if (!attachmentFile.exists() || attachmentFile.isDirectory()) {
                    System.out.println("Attachment file not found or is a directory.");
                    return 1;
                }

                try {
                    byte[] fileBytes = Files.readAllBytes(attachmentFile.toPath());
                    String base64String = Base64.getEncoder().encodeToString(fileBytes);
                    requestBody.put("attachment", base64String);
                } catch (IOException e) {
                    throw new RuntimeException("Error reading attachment file.", e);
                }
            }
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] digestBytes = digest.digest(message.getBytes());
            IdandKey privateKeyContent = readFile("mb.ini");
            requestBody.put("author", privateKeyContent.userId);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKeyContent.privateKey);
            signature.update(digestBytes);

            byte[] signatureBytes = signature.sign();
            String base64Signature = Base64.getEncoder().encodeToString(signatureBytes);
            requestBody.put("signature", base64Signature);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody);
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(url+"/messages/create", requestEntity, Map.class);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;

    }

    @Command
    int create(@Parameters String id) {
        if (!id.matches("^[a-z0-9]+$")) {
            System.out.println("Invalid user ID. User ID must contain only lowercase letters (a-z) and numbers (0-9).");
            return 1;
        }

        String userId = id;
        System.out.println("Id created for " + id);
        KeyPair keyPair = generateKeyPair();
        if (keyPair == null) {
            return 1;
        }
        saveToIniFile(userId, keyPair.getPrivate());
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("user", userId);
        responseBody.put("public-key", Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));

        Map response = restTemplate.postForObject(url+"/user/create", responseBody, Map.class);
        return 2;
    }

    @Command(name = "list", description = "List messages from the server")
    public int list(
            @CommandLine.Option(names = {"--starting"}, description = "Starting message ID", defaultValue = "-1") int startingId,
            @CommandLine.Option(names = {"--count"}, description = "Number of messages to retrieve", defaultValue = "10") int count,
            @CommandLine.Option(names = {"-a", "--save-attachment"}, description = "Save attachment to file") boolean saveAttachment) {
        try {

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("starting", startingId);
            requestBody.put("count", count);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody);

            ResponseEntity<List<MessageRequestClient>> responseEntity = restTemplate.exchange(
                    url + "/messages/list",
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<List<MessageRequestClient>>() {
                    });

                    List <MessageRequestClient> messages = responseEntity.getBody();


            if (messages != null) {
                for (MessageRequestClient message : messages) {

                    if (saveAttachment) {
                        String attachment = message.getAttachment();
                        if (attachment != null && !attachment.isEmpty()) {
                            byte[] decodedAttachment = Base64.getDecoder().decode(attachment);
                            String fileName = message.getMessageId() + ".out";
                            FileOutputStream fos = new FileOutputStream(fileName);
                            fos.write(decodedAttachment);
                        }
                    }

                    System.out.println(formatMessage(message));
                }

            } else {
                System.out.println("No messages found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 2;
    }

    private String formatMessage(MessageRequestClient message) {
        StringBuilder formattedMessage = new StringBuilder();
        formattedMessage.append(message.getMessageId()).append(": ")
                .append(message.getDate()).append(" ")
                .append(message.getAuthor()).append(" says \"")
                .append(message.getMessage()).append("\"");

        if (message.getAttachment() != null && !message.getAttachment().isEmpty()) {
            formattedMessage.append(" 📎 ");
        }

        formattedMessage.append("\n");
        return formattedMessage.toString();
    }


    public static void main(String[] args) {

        SpringApplication.run(ClientApplication.class, args);
    }

    int exitCode;

    @Override
    public void run(String... args) throws Exception {
        exitCode = new CommandLine(this, iFactory).execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void saveToIniFile(String userId, PrivateKey privateKey) {
        try (FileWriter fileOut = new FileWriter("mb.ini");
             BufferedWriter objectOut = new BufferedWriter(fileOut)) {
            objectOut.write(userId + "\n");
            objectOut.write(Base64.getEncoder().encodeToString(privateKey.getEncoded()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static IdandKey readFile(String file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String userId = br.readLine();
            byte[] privateKeyBytes = Base64.getDecoder().decode(br.readLine());
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            return new IdandKey(userId, privateKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}