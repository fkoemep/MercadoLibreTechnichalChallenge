package cloudcode.controllers;

import cloudcode.services.SplitSatelliteMessagesProcessorService;
import cloudcode.entities.SatelliteMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
public final class TopSecretSplitController {
  private static final Logger logger = LoggerFactory.getLogger(TopSecretSplitController.class);
  private MessageSource messageSource;
  private SplitSatelliteMessagesProcessorService splitSatelliteMessagesProcessorService;
  private static final ObjectMapper objectMapper = new ObjectMapper();


  public TopSecretSplitController(SplitSatelliteMessagesProcessorService splitSatelliteMessagesProcessorService, MessageSource messageSource){
    this.splitSatelliteMessagesProcessorService = splitSatelliteMessagesProcessorService;
    this.messageSource = messageSource;
  }

  @PostMapping(value = "/topsecret_split/{name}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity topSecretSplitPOST(@RequestBody Map message, @PathVariable String name){
    message.put("name", name);

    try{
      SatelliteMessage satelliteMessage = objectMapper.convertValue(message, SatelliteMessage.class);
      CompletableFuture<ResponseEntity> response = splitSatelliteMessagesProcessorService.addSatelliteMessage(satelliteMessage);
      ResponseEntity result = response.get();

      synchronized (this){
        return result;
      }
    }

    catch (IllegalArgumentException | InterruptedException | ExecutionException e){
      synchronized (this){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(messageSource.getMessage("INVALID_JSON_MESSAGE", null, Locale.US));
      }
    }
  }

  @GetMapping(value = "/topsecret_split/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity topSecretSplitGET(@PathVariable String name, @RequestParam Double distance, @RequestParam String[] message){
    Map<String, Object> objectToParse = new HashMap<>();
    objectToParse.put("name", name);
    objectToParse.put("distance", distance);
    objectToParse.put("message", message);

    try{
      SatelliteMessage satelliteMessage = objectMapper.convertValue(objectToParse, SatelliteMessage.class);
      CompletableFuture<ResponseEntity> response = splitSatelliteMessagesProcessorService.addSatelliteMessage(satelliteMessage);
      ResponseEntity result = response.get();

      synchronized (this){
        return result;
      }
    }

    catch (IllegalArgumentException | InterruptedException | ExecutionException e){
      synchronized (this){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(messageSource.getMessage("INVALID_JSON_MESSAGE", null, Locale.US));
      }
    }
  }


}
