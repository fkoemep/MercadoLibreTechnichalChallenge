//package cloudcode.helloworld.web;
//
//import java.io.IOException;
//import java.util.concurrent.TimeUnit;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//
///** Defines a controller to handle HTTP requests */
//@RestController
//public final class HelloWorldController {
//
//  private static String project;
//  private static final Logger logger = LoggerFactory.getLogger(HelloWorldController.class);
//
//  /**
//   * Create an endpoint for the landing page
//   *
//   * @return the index view template
//   /*/
//  @GetMapping("/topsecret_split")
//  public String helloWorld(Model model) {
//    // If the custom environment variable GOOGLE_CLOUD_PROJECT is not set
//    // check the Cloud Run metadata server for the Project Id.
//    project = System.getenv("GOOGLE_CLOUD_PROJECT");
//    if (project == null) {
//      project = getProjectId();
//    }
//
//    // Get Cloud Run environment variables.
//    String revision = System.getenv("K_REVISION") == null ? "???" : System.getenv("K_REVISION");
//    String service = System.getenv("K_SERVICE") == null ? "???" : System.getenv("K_SERVICE");
//
//    // Set variables in html template.
//    model.addAttribute("revision", revision);
//    model.addAttribute("service", service);
//    model.addAttribute("project", project);
//    return "index";
//  }
//
//  @PostMapping("/topsecret_split")
//  public String helloWorld2(Model model) {
//    // If the custom environment variable GOOGLE_CLOUD_PROJECT is not set
//    // check the Cloud Run metadata server for the Project Id.
//    project = System.getenv("GOOGLE_CLOUD_PROJECT");
//    if (project == null) {
//      project = getProjectId();
//    }
//
//    // Get Cloud Run environment variables.
//    String revision = System.getenv("K_REVISION") == null ? "???" : System.getenv("K_REVISION");
//    String service = System.getenv("K_SERVICE") == null ? "???" : System.getenv("K_SERVICE");
//
//    // Set variables in html template.
//    model.addAttribute("revision", revision);
//    model.addAttribute("service", service);
//    model.addAttribute("project", project);
//    return "index";
//  }
//
//  /**
//   * Get the Project Id from GCP metadata server
//   *
//   * @return GCP Project Id or null
//   */
//  public static String getProjectId() {
//    OkHttpClient ok =
//        new OkHttpClient.Builder()
//            .readTimeout(500, TimeUnit.MILLISECONDS)
//            .writeTimeout(500, TimeUnit.MILLISECONDS)
//            .build();
//
//    String metadataUrl = "http://metadata.google.internal/computeMetadata/v1/project/project-id";
//    Request request =
//        new Request.Builder().url(metadataUrl).addHeader("Metadata-Flavor", "Google").get().build();
//
//    String project = null;
//    try {
//      Response response = ok.newCall(request).execute();
//      project = response.body().string();
//    } catch (IOException e) {
//      logger.error("Error retrieving Project Id");
//    }
//    return project;
//  }
//}

package cloudcode.web;

import cloudcode.BasicFunctions;
import cloudcode.SplitSatelliteMessagesProcessorService;
import cloudcode.objects.RequestObject;
import cloudcode.objects.ResponseObject;
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

@RestController
public final class TopSecretController {
  private static final Logger logger = LoggerFactory.getLogger(TopSecretController.class);
  private MessageSource messageSource;
  private SplitSatelliteMessagesProcessorService splitSatelliteMessagesProcessorService;

  public TopSecretController(SplitSatelliteMessagesProcessorService splitSatelliteMessagesProcessorService, MessageSource messageSource){
    this.splitSatelliteMessagesProcessorService = splitSatelliteMessagesProcessorService;
    this.messageSource = messageSource;
  }

  @PostMapping(value = "/topsecret", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity topSecretSplitPOST(@RequestBody Map satelliteMessages){
    try{
      ObjectMapper objectMapper = new ObjectMapper();
      RequestObject result = objectMapper.convertValue(satelliteMessages, RequestObject.class);

      if(result == null){
        synchronized (this){
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(messageSource.getMessage("INVALID_JSON_MESSAGE", null, Locale.US));
        }
      }

      List<String[]> messages = new ArrayList<>();
      List<Double> distances = new ArrayList<>();

      Arrays.stream(result.getSatelliteMessages()).forEach(satelliteMessage -> {messages.add(satelliteMessage.getMessage()); distances.add(satelliteMessage.getDistance());});

      synchronized (this){
        return new ResponseEntity<>(new ResponseObject(BasicFunctions.GetLocation(distances), BasicFunctions.GetMessage(messages)), HttpStatus.OK);
      }
    }

    catch (IllegalArgumentException e){
      synchronized (this){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(messageSource.getMessage("INVALID_JSON_MESSAGE", null, Locale.US));
      }
    }
  }

}
