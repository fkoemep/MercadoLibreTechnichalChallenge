package cloudcode.services;

import cloudcode.BasicFunctions;
import cloudcode.entities.RequestObject;
import cloudcode.entities.ResponseObject;
import cloudcode.entities.SatelliteMessage;
import cloudcode.exceptions.LocationProcessingException;
import cloudcode.exceptions.MessageProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParseException;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SplitSatelliteMessagesProcessorService {

    private static final Logger logger = LoggerFactory.getLogger(SplitSatelliteMessagesProcessorService.class);
    private SplitSatelliteMessagesProcessorService splitSatelliteMessagesProcessorService;
    private final List<SatelliteMessage> satelliteMessages = Collections.synchronizedList(new ArrayList<>());
    private final MessageSource messageSource;
    private CompletableFuture<ResponseEntity> task;
    private CountDownLatch waitUntil3Messages;

    @Value("#{ T(java.lang.Long).parseLong('${splitSatelliteMessage.timeout}')}")
    private Long timeout;

    public SplitSatelliteMessagesProcessorService(MessageSource messageSource){
        this.messageSource = messageSource;
    }

    @Lazy
    @Autowired
    public void setSplitJoinerService(SplitSatelliteMessagesProcessorService splitSatelliteMessagesProcessorService) {
        this.splitSatelliteMessagesProcessorService = splitSatelliteMessagesProcessorService;
    }

    public CompletableFuture<ResponseEntity> addSatelliteMessage(SatelliteMessage message){
        synchronized (satelliteMessages){

            if(satelliteMessages.size() == 0){
                waitUntil3Messages = new CountDownLatch(1);
                task = splitSatelliteMessagesProcessorService.execute();
            }

/*If before adding a message there's 3 satelliteMessages already, it means that there're more than 3 simultaneous requests...something that
should not happen since we've set a concurrency limit for 3 simultaneous requests per Google Cloud Run container instance but just in case.*/
            if(satelliteMessages.size() == 3){
                return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(messageSource.getMessage("EXCESSIVE_MESSAGES_TRY_AGAIN_LATER", null, Locale.US)));
            }

            satelliteMessages.add(message);

            if(satelliteMessages.size() == 3){
                waitUntil3Messages.countDown();
            }

            return task;
        }
    }

    @Async
    public CompletableFuture<ResponseEntity> execute(){

        if(satelliteMessages.size() < 3){
            try{
                waitUntil3Messages.await(timeout, TimeUnit.SECONDS);
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        //We're synchronized with the message list to prevent a consecutive request from adding a message until we clear the list first
        synchronized (satelliteMessages){
            try{
                RequestObject req = new RequestObject(satelliteMessages.toArray(new SatelliteMessage[0]));


                List<String[]> messages = Arrays.stream(req.getSatelliteMessages()).map(SatelliteMessage::getMessage).collect(Collectors.toList());

                Double[] distances = Arrays.stream(req.getSatelliteMessages()).sorted(Comparator.comparing(SatelliteMessage::getName))
                        .map(SatelliteMessage::getDistance).toArray(Double[]::new);

                //Clearing the list and returning just in case Google Cloud Run decides to reuse the container instance.
                satelliteMessages.clear();
                return CompletableFuture.completedFuture(new ResponseEntity<>(
                        new ResponseObject(BasicFunctions.GetLocation(distances), BasicFunctions.GetMessage(messages)), HttpStatus.OK));
            }
            catch (JsonParseException e) {
                //Clearing the list and returning just in case Google Cloud Run decides to reuse the container instance.
                satelliteMessages.clear();
                return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(messageSource.getMessage("INVALID_JSON_MESSAGE", null, Locale.US)));
            }
            catch (LocationProcessingException | MessageProcessingException e){
                //Clearing the list and returning just in case Google Cloud Run decides to reuse the container instance.
                satelliteMessages.clear();
                return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(messageSource.getMessage("INSUFFICIENT_DATA_MESSAGE", null, Locale.US)));
            }
        }
    }
}