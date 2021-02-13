package cloudcode.web;

import cloudcode.BasicFunctions;
import cloudcode.objects.ResponseObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TopSecretTests {
    private final String BASE_URL = "http://localhost:8080";
    private static final String BASE_URL_PROD = "https://melichallenge-kkhnr7cx4q-rj.a.run.app";
    private final String functionUrl = BASE_URL_PROD + "/topsecret/";

    private final HttpClient client = HttpClient.newHttpClient();

    private final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource(){{setBasename("messages");}};

    public enum testDistance {
        KENOBI(400.0), SKYWALKER(300.0), SATO(100.0 * Math.sqrt((1.0/37.0) * (1853.0-96.0*Math.sqrt(3.0))));

        private Double distance;

        testDistance(Double distance) {
            this.distance = distance;
        }
        public Double getDistance() {
            return this.distance;
        }
    }

    public enum testDistanceInvalid {
        KENOBI(100.0), SKYWALKER(100.0), SATO(100.0);

        private Double distance;

        testDistanceInvalid(Double distance) {
            this.distance = distance;
        }
        public Double getDistance() {
            return this.distance;
        }
    }

    private final Double[] testDistances = new Double[]{testDistance.KENOBI.distance, testDistance.SATO.distance, testDistance.SKYWALKER.distance};

    public enum testMessagesArray {
        KENOBI(new String[]{"", "", ""}), SKYWALKER(new String[]{"", "", ""}), SATO(new String[]{"", "", ""});

        private String[] array;

        testMessagesArray(String[] array) {
            this.array = array;
        }
        public String[] getArray() {
            return this.array;
        }
    }

    public enum testMessagesArrayInvalid {
        KENOBI(new String[]{"", "", ""}), SKYWALKER(new String[]{"", "", ""}), SATO(new String[]{"", "", ""});

        private String[] array;

        testMessagesArrayInvalid(String[] array) {
            this.array = array;
        }
        public String[] getArray() {
            return this.array;
        }
    }

    List<String[]> messageStringList = Arrays.stream(testMessagesArray.values()).map(testMessagesArray::getArray).collect(Collectors.toList());


    @Test
    public void verifyCorrectJson() throws Throwable {
        List<Map<String, Object>> messageList = new ArrayList<>();

        Map<String, Object> messageKenobi = new HashMap<>();
        messageKenobi.put("name", "kenobi");
        messageKenobi.put("message", testMessagesArray.KENOBI.getArray());
        messageKenobi.put("distance", testDistance.KENOBI.getDistance());
        messageList.add(messageKenobi);

        Map<String, Object> messageSkywalker = new HashMap<>();
        messageSkywalker.put("name", "skywalker");
        messageSkywalker.put("message", testMessagesArray.SKYWALKER.getArray());
        messageSkywalker.put("distance", testDistance.SKYWALKER.getDistance());
        messageList.add(messageSkywalker);

        Map<String, Object> messageSato = new HashMap<>();
        messageSato.put("name", "sato");
        messageSato.put("message", testMessagesArray.SATO.getArray());
        messageSato.put("distance", testDistance.SATO.getDistance());
        messageList.add(messageSato);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(Map.of("satellites",messageList));

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(functionUrl))
                .method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        ResponseObject correctResult = new ResponseObject(BasicFunctions.GetLocation(testDistances), BasicFunctions.GetMessage(messageStringList));

        assertThat(response.statusCode(), equalTo(HttpURLConnection.HTTP_OK));
        assertThat(response.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
    }

    @Test
    public void verifyCorrectJsonConcurrentRequests() throws Throwable {
        List<Map<String, Object>> messageList = new ArrayList<>();

        Map<String, Object> messageKenobi = new HashMap<>();
        messageKenobi.put("name", "kenobi");
        messageKenobi.put("message", testMessagesArray.KENOBI.getArray());
        messageKenobi.put("distance", testDistance.KENOBI.getDistance());
        messageList.add(messageKenobi);

        Map<String, Object> messageSkywalker = new HashMap<>();
        messageSkywalker.put("name", "skywalker");
        messageSkywalker.put("message", testMessagesArray.SKYWALKER.getArray());
        messageSkywalker.put("distance", testDistance.SKYWALKER.getDistance());
        messageList.add(messageSkywalker);

        Map<String, Object> messageSato = new HashMap<>();
        messageSato.put("name", "sato");
        messageSato.put("message", testMessagesArray.SATO.getArray());
        messageSato.put("distance", testDistance.SATO.getDistance());
        messageList.add(messageSato);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(Map.of("satellites",messageList));

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(functionUrl))
                .method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();

        CompletableFuture<HttpResponse<String>> response1 = client.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        CompletableFuture<HttpResponse<String>> response2 = client.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        ResponseObject correctResult = new ResponseObject(BasicFunctions.GetLocation(testDistances), BasicFunctions.GetMessage(messageStringList));

        HttpResponse<String> result1 = response1.get();
        HttpResponse<String> result2 = response2.get();

        assertThat(result1.statusCode(), equalTo(HttpURLConnection.HTTP_OK));
        assertThat(result1.body(), equalTo(objectMapper.writeValueAsString(correctResult)));

        assertThat(result2.statusCode(), equalTo(HttpURLConnection.HTTP_OK));
        assertThat(result2.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
    }

    @Test
    public void verifyIncorrectJson() throws Throwable {
            List<Map<String, Object>> messageList = new ArrayList<>();

        Map<String, Object> messageKenobi = new HashMap<>();
        messageKenobi.put("name", "kenobi");
        messageKenobi.put("message", testMessagesArray.KENOBI.getArray());
        messageKenobi.put("distance", testDistance.KENOBI.getDistance());
        messageList.add(messageKenobi);

        Map<String, Object> messageSkywalker = new HashMap<>();
        messageSkywalker.put("name", "skywalker");
        messageSkywalker.put("message", testMessagesArray.SKYWALKER.getArray());
        messageSkywalker.put("distance", testDistance.SKYWALKER.getDistance());
        messageList.add(messageSkywalker);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(Map.of("satellites",messageList));

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(functionUrl))
                .method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertThat(response.statusCode(), equalTo(HttpURLConnection.HTTP_NOT_FOUND));
        assertThat(response.body(), equalTo(messageSource.getMessage("INVALID_JSON_MESSAGE", null, Locale.US)));
    }

    @Test
    public void verifyIncorrectRequestMethod() throws Throwable {
        List<Map<String, Object>> messageList = new ArrayList<>();

        Map<String, Object> messageKenobi = new HashMap<>();
        messageKenobi.put("name", "kenobi");
        messageKenobi.put("message", testMessagesArray.KENOBI.getArray());
        messageKenobi.put("distance", testDistance.KENOBI.getDistance());
        messageList.add(messageKenobi);

        Map<String, Object> messageSkywalker = new HashMap<>();
        messageSkywalker.put("name", "skywalker");
        messageSkywalker.put("message", testMessagesArray.SKYWALKER.getArray());
        messageSkywalker.put("distance", testDistance.SKYWALKER.getDistance());
        messageList.add(messageSkywalker);

        Map<String, Object> messageSato = new HashMap<>();
        messageSato.put("name", "sato");
        messageSato.put("message", testMessagesArray.SATO.getArray());
        messageSato.put("distance", testDistance.SATO.getDistance());
        messageList.add(messageSato);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(Map.of("satellites",messageList));

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(functionUrl))
                .method(HttpMethod.PUT.asString(), HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertThat(response.statusCode(), equalTo(HttpURLConnection.HTTP_BAD_METHOD));
    }


    @Test
    public void verifyIncorrectNoIntersectionJson() throws Throwable {
        List<Map<String, Object>> messageList = new ArrayList<>();

        Map<String, Object> messageKenobi = new HashMap<>();
        messageKenobi.put("name", "kenobi");
        messageKenobi.put("message", testMessagesArray.KENOBI.getArray());
        messageKenobi.put("distance", testDistanceInvalid.KENOBI.getDistance());
        messageList.add(messageKenobi);

        Map<String, Object> messageSkywalker = new HashMap<>();
        messageSkywalker.put("name", "skywalker");
        messageSkywalker.put("message", testMessagesArray.SKYWALKER.getArray());
        messageSkywalker.put("distance", testDistanceInvalid.SKYWALKER.getDistance());
        messageList.add(messageSkywalker);

        Map<String, Object> messageSato = new HashMap<>();
        messageSato.put("name", "sato");
        messageSato.put("message", testMessagesArray.SATO.getArray());
        messageSato.put("distance", testDistanceInvalid.SATO.getDistance());
        messageList.add(messageSato);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(Map.of("satellites",messageList));

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(functionUrl))
                .method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertThat(response.statusCode(), equalTo(HttpURLConnection.HTTP_NOT_FOUND));
        assertThat(response.body(), equalTo(messageSource.getMessage("INSUFFICIENT_DATA_MESSAGE", null, Locale.US)));
    }

    @Test
    public void verifyIncorrectNoDecodedMessageJson() throws Throwable {
        List<Map<String, Object>> messageList = new ArrayList<>();

        Map<String, Object> messageKenobi = new HashMap<>();
        messageKenobi.put("name", "kenobi");
        messageKenobi.put("message", testMessagesArrayInvalid.KENOBI.getArray());
        messageKenobi.put("distance", testDistance.KENOBI.getDistance());
        messageList.add(messageKenobi);

        Map<String, Object> messageSkywalker = new HashMap<>();
        messageSkywalker.put("name", "skywalker");
        messageSkywalker.put("message", testMessagesArrayInvalid.SKYWALKER.getArray());
        messageSkywalker.put("distance", testDistance.SKYWALKER.getDistance());
        messageList.add(messageSkywalker);

        Map<String, Object> messageSato = new HashMap<>();
        messageSato.put("name", "sato");
        messageSato.put("message", testMessagesArrayInvalid.SATO.getArray());
        messageSato.put("distance", testDistance.SATO.getDistance());
        messageList.add(messageSato);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(Map.of("satellites",messageList));

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(functionUrl))
                .method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertThat(response.statusCode(), equalTo(HttpURLConnection.HTTP_NOT_FOUND));
        assertThat(response.body(), equalTo(messageSource.getMessage("INSUFFICIENT_DATA_MESSAGE", null, Locale.US)));
    }
}