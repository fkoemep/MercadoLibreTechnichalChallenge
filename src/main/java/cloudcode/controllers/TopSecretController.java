package cloudcode.controllers;

import cloudcode.BasicFunctions;
import cloudcode.entities.RequestObject;
import cloudcode.entities.ResponseObject;
import cloudcode.entities.SatelliteMessage;
import cloudcode.exceptions.LocationProcessingException;
import cloudcode.exceptions.MessageProcessingException;
import cloudcode.services.SplitSatelliteMessagesProcessorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public final class TopSecretController {
  private static final Logger logger = LoggerFactory.getLogger(TopSecretController.class);
  private MessageSource messageSource;
  private SplitSatelliteMessagesProcessorService splitSatelliteMessagesProcessorService;
  private static final ObjectMapper objectMapper = new ObjectMapper();

  public TopSecretController(SplitSatelliteMessagesProcessorService splitSatelliteMessagesProcessorService, MessageSource messageSource){
    this.splitSatelliteMessagesProcessorService = splitSatelliteMessagesProcessorService;
    this.messageSource = messageSource;
  }

  @PostMapping(value = "/topsecret", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity topSecretSplitPOST(@RequestBody Map satelliteMessages){
    try{
      RequestObject result = objectMapper.convertValue(satelliteMessages, RequestObject.class);

      if(result == null){
        synchronized (this){
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(messageSource.getMessage("INVALID_JSON_MESSAGE", null, Locale.US));
        }
      }

      List<String[]> messages = Arrays.stream(result.getSatelliteMessages()).map(SatelliteMessage::getMessage).collect(Collectors.toList());

      Double[] distances = Arrays.stream(result.getSatelliteMessages()).sorted(Comparator.comparing(SatelliteMessage::getName))
                          .map(SatelliteMessage::getDistance).toArray(Double[]::new);

      synchronized (this){
        return new ResponseEntity<>(new ResponseObject(BasicFunctions.GetLocation(distances), BasicFunctions.GetMessage(messages)), HttpStatus.OK);
      }
    }

    catch (IllegalArgumentException e){
      synchronized (this){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(messageSource.getMessage("INVALID_JSON_MESSAGE", null, Locale.US));
      }
    }
    catch (LocationProcessingException | MessageProcessingException e){
      synchronized (this) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(messageSource.getMessage("INSUFFICIENT_DATA_MESSAGE", null, Locale.US));
      }
    }
  }

}
