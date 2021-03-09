package cloudcode.controllers;

import cloudcode.BasicFunctions;
import cloudcode.entities.ResponseObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.jetty.http.HttpMethod;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application.properties")


public class TopSecretSplitTests {

    private final String BASE_URL = "http://localhost:8080";
    private static final String BASE_URL_PROD = "https://melichallenge-kkhnr7cx4q-rj.a.run.app";
    private final String functionUrl = BASE_URL + "/topsecret_split/";

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
        KENOBI(new String[]{"", "este", "es", "un", "mensaje"}), SKYWALKER(new String[]{"este", "", "un", "mensaje"}), SATO(new String[]{"", "", "es", " ", "mensaje"});

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

    List<String[]> messageStringList = Arrays.stream(TopSecretTests.testMessagesArray.values()).map(TopSecretTests.testMessagesArray::getArray).collect(Collectors.toList());

    private final double correctCalculatedLocationX = (-100.0/37.0)*(53.0 + 6.0 * Math.sqrt(3.0));
    private final double correctCalculatedLocationY = (400.0/37.0)*(-13.0 + 9.0 * Math.sqrt(3.0));

    @Value("#{ T(java.lang.Long).parseLong('${splitSatelliteMessage.timeout}')}")
    private Long timeout;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final okhttp3.MediaType JSON = okhttp3.MediaType.parse("application/json; charset=utf-8");

    @Test
    public void verifyCorrectJsonPOSTRequest() throws Throwable {
        Map<String, Object> messageKenobi = new HashMap<>();
        messageKenobi.put("message", testMessagesArray.KENOBI.getArray());
        messageKenobi.put("distance", testDistance.KENOBI.getDistance());

        Map<String, Object> messageSato = new HashMap<>();
        messageSato.put("message", testMessagesArray.SATO.getArray());
        messageSato.put("distance", testDistance.SATO.getDistance());

        Map<String, Object> messageSkywalker = new HashMap<>();
        messageSkywalker.put("message", testMessagesArray.SKYWALKER.getArray());
        messageSkywalker.put("distance", testDistance.SKYWALKER.getDistance());

        
        String requestBodyKenobi = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageKenobi);
        String requestBodySato = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageSato);
        String requestBodySkywalker = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageSkywalker);

        HttpRequest requestKenobi = HttpRequest.newBuilder().uri(URI.create(functionUrl + "kenobi")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodyKenobi)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSkywalker = HttpRequest.newBuilder().uri(URI.create(functionUrl + "skywalker")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodySkywalker)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSato = HttpRequest.newBuilder().uri(URI.create(functionUrl + "sato")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodySato)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();

        CompletableFuture<HttpResponse<String>> responseKenobi = client.sendAsync(requestKenobi, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        CompletableFuture<HttpResponse<String>> responseSkywalker = client.sendAsync(requestSkywalker, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        CompletableFuture<HttpResponse<String>> responseSato = client.sendAsync(requestSato, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpResponse<String> resultKenobi = responseKenobi.get();
        HttpResponse<String> resultSkywalker = responseSkywalker.get();
        HttpResponse<String> resultSato = responseSato.get();

        ResponseObject correctResult = new ResponseObject(BasicFunctions.GetLocation(testDistances), BasicFunctions.GetMessage(messageStringList));


        assertThat(resultKenobi.statusCode(), equalTo(HttpURLConnection.HTTP_OK));
        assertThat(resultSkywalker.statusCode(), equalTo(HttpURLConnection.HTTP_OK));
        assertThat(resultSato.statusCode(), equalTo(HttpURLConnection.HTTP_OK));

        assertThat(resultKenobi.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
        assertThat(resultSkywalker.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
        assertThat(resultSato.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
    }

    @Test
    public void verifyCorrectJsonGETRequest() throws Throwable {
        URIBuilder uriKenobi = new URIBuilder(URI.create(functionUrl + "kenobi"));
        uriKenobi.addParameter("distance", testDistance.KENOBI.getDistance().toString());
        uriKenobi.addParameter("message",String.join(",", testMessagesArray.KENOBI.getArray()));

        URIBuilder uriSkywalker = new URIBuilder(URI.create(functionUrl + "skywalker"));
        uriSkywalker.addParameter("distance", testDistance.SKYWALKER.getDistance().toString());
        uriSkywalker.addParameter("message",String.join(",", testMessagesArray.SKYWALKER.getArray()));

        URIBuilder uriSato = new URIBuilder(URI.create(functionUrl + "sato"));
        uriSato.addParameter("distance", testDistance.SATO.getDistance().toString());
        uriSato.addParameter("message",String.join(",", testMessagesArray.SATO.getArray()));


        HttpRequest requestKenobi = HttpRequest.newBuilder().uri(uriKenobi.build()).
                method(HttpMethod.GET.asString(), HttpRequest.BodyPublishers.noBody()).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSkywalker = HttpRequest.newBuilder().uri(uriSkywalker.build()).
                method(HttpMethod.GET.asString(), HttpRequest.BodyPublishers.noBody()).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSato = HttpRequest.newBuilder().uri(uriSato.build()).
                method(HttpMethod.GET.asString(), HttpRequest.BodyPublishers.noBody()).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();

        CompletableFuture<HttpResponse<String>> responseKenobi = client.sendAsync(requestKenobi, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        CompletableFuture<HttpResponse<String>> responseSkywalker = client.sendAsync(requestSkywalker, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        CompletableFuture<HttpResponse<String>> responseSato = client.sendAsync(requestSato, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpResponse<String> resultKenobi = responseKenobi.get();
        HttpResponse<String> resultSkywalker = responseSkywalker.get();
        HttpResponse<String> resultSato = responseSato.get();

        ResponseObject correctResult = new ResponseObject(BasicFunctions.GetLocation(testDistances), BasicFunctions.GetMessage(messageStringList));

        assertThat(resultKenobi.statusCode(), equalTo(HttpURLConnection.HTTP_OK));
        assertThat(resultSkywalker.statusCode(), equalTo(HttpURLConnection.HTTP_OK));
        assertThat(resultSato.statusCode(), equalTo(HttpURLConnection.HTTP_OK));

        assertThat(resultKenobi.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
        assertThat(resultSkywalker.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
        assertThat(resultSato.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
    }

    @Test
    public void verifyCorrectJsonMixedRequest() throws Throwable {
        Map<String, Object> messageKenobi = new HashMap<>();
        messageKenobi.put("message", testMessagesArray.KENOBI.getArray());
        messageKenobi.put("distance", testDistance.KENOBI.getDistance());

        Map<String, Object> messageSato = new HashMap<>();
        messageSato.put("message", testMessagesArray.SATO.getArray());
        messageSato.put("distance", testDistance.SATO.getDistance());

        URIBuilder uriSkywalker = new URIBuilder(URI.create(functionUrl + "skywalker"));
        uriSkywalker.addParameter("distance", testDistance.SKYWALKER.getDistance().toString());
        uriSkywalker.addParameter("message",String.join(",", testMessagesArray.SKYWALKER.getArray()));

        
        String requestBodyKenobi = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageKenobi);
        String requestBodySato = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageSato);

        HttpRequest requestKenobi = HttpRequest.newBuilder().uri(URI.create(functionUrl + "kenobi")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodyKenobi)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSkywalker = HttpRequest.newBuilder().uri(uriSkywalker.build()).
                method(HttpMethod.GET.asString(), HttpRequest.BodyPublishers.noBody()).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSato = HttpRequest.newBuilder().uri(URI.create(functionUrl + "sato")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodySato)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();

        CompletableFuture<HttpResponse<String>> responseKenobi = client.sendAsync(requestKenobi, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        CompletableFuture<HttpResponse<String>> responseSkywalker = client.sendAsync(requestSkywalker, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        CompletableFuture<HttpResponse<String>> responseSato = client.sendAsync(requestSato, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpResponse<String> resultKenobi = responseKenobi.get();
        HttpResponse<String> resultSkywalker = responseSkywalker.get();
        HttpResponse<String> resultSato = responseSato.get();

        ResponseObject correctResult = new ResponseObject(BasicFunctions.GetLocation(testDistances), BasicFunctions.GetMessage(messageStringList));

        assertThat(resultKenobi.statusCode(), equalTo(HttpURLConnection.HTTP_OK));
        assertThat(resultSkywalker.statusCode(), equalTo(HttpURLConnection.HTTP_OK));
        assertThat(resultSato.statusCode(), equalTo(HttpURLConnection.HTTP_OK));

        assertThat(resultKenobi.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
        assertThat(resultSkywalker.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
        assertThat(resultSato.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
    }

    @Test
    public void verifyIncorrectMethod() throws Throwable {
        Map<String, Object> messageKenobi = new HashMap<>();
        messageKenobi.put("message", testMessagesArray.KENOBI.getArray());
        messageKenobi.put("distance", testDistance.KENOBI.getDistance());

        
        String requestBodyKenobi = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageKenobi);

        HttpRequest requestKenobi = HttpRequest.newBuilder().uri(URI.create(functionUrl + "kenobi")).
                method(HttpMethod.PUT.asString(), HttpRequest.BodyPublishers.ofString(requestBodyKenobi)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();

        CompletableFuture<HttpResponse<String>> responseKenobi = client.sendAsync(requestKenobi, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpResponse<String> resultKenobi = responseKenobi.get();

        assertThat(resultKenobi.statusCode(), equalTo(HttpURLConnection.HTTP_BAD_METHOD));
    }

    @Test
    public void verifyIncorrectSatelliteNameJson() throws Throwable {
        Map<String, Object> messageKenobi = new HashMap<>();
        messageKenobi.put("message", testMessagesArray.KENOBI.getArray());
        messageKenobi.put("distance", testDistance.KENOBI.getDistance());

        Map<String, Object> messageSato = new HashMap<>();
        messageSato.put("message", testMessagesArray.SATO.getArray());
        messageSato.put("distance", testDistance.SATO.getDistance());

        Map<String, Object> messageSkywalker = new HashMap<>();
        messageSkywalker.put("message", testMessagesArray.SKYWALKER.getArray());
        messageSkywalker.put("distance", testDistance.SKYWALKER.getDistance());

        
        String requestBodyKenobi = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageKenobi);
        String requestBodySato = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageSato);
        String requestBodySkywalker = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageSkywalker);

        HttpRequest requestKenobi = HttpRequest.newBuilder().uri(URI.create(functionUrl + "kenobii")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodyKenobi)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSkywalker = HttpRequest.newBuilder().uri(URI.create(functionUrl + "skywalker")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodySkywalker)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSato = HttpRequest.newBuilder().uri(URI.create(functionUrl + "sato")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodySato)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();

        CompletableFuture<HttpResponse<String>> responseKenobi = client.sendAsync(requestKenobi, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        CompletableFuture<HttpResponse<String>> responseSkywalker = client.sendAsync(requestSkywalker, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        CompletableFuture<HttpResponse<String>> responseSato = client.sendAsync(requestSato, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpResponse<String> resultKenobi = responseKenobi.get();
        HttpResponse<String> resultSkywalker = responseSkywalker.get();
        HttpResponse<String> resultSato = responseSato.get();

        assertThat(resultKenobi.statusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));
        assertThat(resultSkywalker.statusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));
        assertThat(resultSato.statusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));

        assertThat(resultKenobi.body(), equalTo(messageSource.getMessage("INVALID_JSON_MESSAGE", null, Locale.US)));
        assertThat(resultSkywalker.body(), equalTo(messageSource.getMessage("INVALID_JSON_MESSAGE", null, Locale.US)));
        assertThat(resultSato.body(), equalTo(messageSource.getMessage("INVALID_JSON_MESSAGE", null, Locale.US)));
    }

    @Test
    public void verifyIncorrectRepeatedSatelliteNameJson() throws Throwable {
        Map<String, Object> messageKenobi = new HashMap<>();
        messageKenobi.put("message", testMessagesArray.KENOBI.getArray());
        messageKenobi.put("distance", testDistance.KENOBI.getDistance());

        Map<String, Object> messageSato = new HashMap<>();
        messageSato.put("message", testMessagesArray.SATO.getArray());
        messageSato.put("distance", testDistance.SATO.getDistance());

        
        String requestBodyKenobi = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageKenobi);
        String requestBodySato = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageSato);

        HttpRequest requestKenobi = HttpRequest.newBuilder().uri(URI.create(functionUrl + "kenobi")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodyKenobi)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSato = HttpRequest.newBuilder().uri(URI.create(functionUrl + "sato")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodySato)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();

        CompletableFuture<HttpResponse<String>> responseKenobi = client.sendAsync(requestKenobi, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        CompletableFuture<HttpResponse<String>> responseKenobi2 = client.sendAsync(requestKenobi, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        CompletableFuture<HttpResponse<String>> responseSato = client.sendAsync(requestSato, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpResponse<String> resultKenobi = responseKenobi.get();
        HttpResponse<String> resultKenobi2 = responseKenobi2.get();
        HttpResponse<String> resultSato = responseSato.get();

        assertThat(resultKenobi.statusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));
        assertThat(resultKenobi2.statusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));
        assertThat(resultSato.statusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));

        assertThat(resultKenobi.body(), equalTo(messageSource.getMessage("INVALID_JSON_MESSAGE", null, Locale.US)));
        assertThat(resultKenobi2.body(), equalTo(messageSource.getMessage("INVALID_JSON_MESSAGE", null, Locale.US)));
        assertThat(resultSato.body(), equalTo(messageSource.getMessage("INVALID_JSON_MESSAGE", null, Locale.US)));
    }


    @Test
    public void verifyIncorrectOnlyTwoMessagesJson() throws Throwable {

        Thread.sleep(10000);

        Map<String, Object> messageKenobi = new HashMap<>();
        messageKenobi.put("message", testMessagesArray.KENOBI.getArray());
        messageKenobi.put("distance", testDistance.KENOBI.getDistance());

        Map<String, Object> messageSato = new HashMap<>();
        messageSato.put("message", testMessagesArray.SATO.getArray());
        messageSato.put("distance", testDistance.SATO.getDistance());

        
        String requestBodyKenobi = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageKenobi);
        String requestBodySato = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageSato);

        HttpRequest requestKenobi = HttpRequest.newBuilder().uri(URI.create(functionUrl + "kenobi")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodyKenobi)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSato = HttpRequest.newBuilder().uri(URI.create(functionUrl + "sato")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodySato)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();


        StopWatch elapsedTimeKenobi = new StopWatch();
        elapsedTimeKenobi.start();

        CompletableFuture<HttpResponse<String>> responseKenobi = client.sendAsync(requestKenobi, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                                                                    .whenComplete((stringHttpResponse, throwable) -> elapsedTimeKenobi.stop());

        StopWatch elapsedTimeSato = new StopWatch();
        elapsedTimeSato.start();

        CompletableFuture<HttpResponse<String>> responseSato = client.sendAsync(requestSato, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                                                                    .whenComplete((stringHttpResponse, throwable) -> elapsedTimeSato.stop());


        HttpResponse<String> resultKenobi = responseKenobi.get();
        HttpResponse<String> resultSato = responseSato.get();

        assertThat(resultKenobi.statusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));
        assertThat(resultSato.statusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));

        assertThat(resultKenobi.body(), equalTo(messageSource.getMessage("INVALID_JSON_MESSAGE", null, Locale.US)));
        assertThat(resultSato.body(), equalTo(messageSource.getMessage("INVALID_JSON_MESSAGE", null, Locale.US)));

        assertThat(elapsedTimeKenobi.getTotalTimeSeconds(), greaterThanOrEqualTo(Double.valueOf(timeout)));
        assertThat(elapsedTimeSato.getTotalTimeSeconds(), greaterThanOrEqualTo(Double.valueOf(timeout)));

        Thread.sleep(10000);

    }

    @Test
    public void verifyIncorrectNoJson() throws Throwable {
        HttpRequest requestKenobi = HttpRequest.newBuilder().uri(URI.create(functionUrl + "kenobi")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(new HashMap<>())))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();

        CompletableFuture<HttpResponse<String>> responseKenobi = client.sendAsync(requestKenobi, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpResponse<String> resultKenobi = responseKenobi.get();

        assertThat(resultKenobi.statusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));

        assertThat(resultKenobi.body(), equalTo(messageSource.getMessage("INVALID_JSON_MESSAGE", null, Locale.US)));
    }

    @Test
    public void verifyIncorrectEmptyBody() throws Throwable {
        HttpRequest requestKenobi = HttpRequest.newBuilder().uri(URI.create(functionUrl + "kenobi")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.noBody()).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();

        CompletableFuture<HttpResponse<String>> responseKenobi = client.sendAsync(requestKenobi, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpResponse<String> resultKenobi = responseKenobi.get();

        assertThat(resultKenobi.statusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));
    }


    @Test
    public void verifyIncorrectObjectTypeJson() throws Throwable {
        Map<String, Object> messageKenobi = new HashMap<>();
        messageKenobi.put("message", 0);
        messageKenobi.put("distance", testDistance.KENOBI.getDistance());

        Map<String, Object> messageSato = new HashMap<>();
        messageSato.put("message", testMessagesArray.SATO.getArray());
        messageSato.put("distance", "A");

        Map<String, Object> messageSkywalker = new HashMap<>();
        messageSkywalker.put("message", testMessagesArray.SKYWALKER.getArray());
        messageSkywalker.put("distance", testDistance.SKYWALKER.getDistance());

        
        String requestBodyKenobi = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageKenobi);
        String requestBodySato = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageSato);
        String requestBodySkywalker = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageSkywalker);

        HttpRequest requestKenobi = HttpRequest.newBuilder().uri(URI.create(functionUrl + "kenobi")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodyKenobi)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSkywalker = HttpRequest.newBuilder().uri(URI.create(functionUrl + "skywalker")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodySkywalker)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSato = HttpRequest.newBuilder().uri(URI.create(functionUrl + "sato")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodySato)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();

        CompletableFuture<HttpResponse<String>> responseKenobi = client.sendAsync(requestKenobi, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        CompletableFuture<HttpResponse<String>> responseSkywalker = client.sendAsync(requestSkywalker, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        CompletableFuture<HttpResponse<String>> responseSato = client.sendAsync(requestSato, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpResponse<String> resultKenobi = responseKenobi.get();
        HttpResponse<String> resultSkywalker = responseSkywalker.get();
        HttpResponse<String> resultSato = responseSato.get();

        assertThat(resultKenobi.statusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));
        assertThat(resultSkywalker.statusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));
        assertThat(resultSato.statusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));

        assertThat(resultKenobi.body(), equalTo(messageSource.getMessage("INVALID_JSON_MESSAGE", null, Locale.US)));
        assertThat(resultSkywalker.body(), equalTo(messageSource.getMessage("INVALID_JSON_MESSAGE", null, Locale.US)));
        assertThat(resultSato.body(), equalTo(messageSource.getMessage("INVALID_JSON_MESSAGE", null, Locale.US)));
    }

    @Test
    public void verifyAsyncRequest() throws Throwable {
        Map<String, Object> messageKenobi = new HashMap<>();
        messageKenobi.put("message", testMessagesArray.KENOBI.getArray());
        messageKenobi.put("distance", testDistance.KENOBI.getDistance());

        Map<String, Object> messageSato = new HashMap<>();
        messageSato.put("message", testMessagesArray.SATO.getArray());
        messageSato.put("distance", testDistance.SATO.getDistance());

        Map<String, Object> messageSkywalker = new HashMap<>();
        messageSkywalker.put("message", testMessagesArray.SKYWALKER.getArray());
        messageSkywalker.put("distance", testDistance.SKYWALKER.getDistance());

        
        String requestBodyKenobi = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageKenobi);
        String requestBodySato = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageSato);
        String requestBodySkywalker = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageSkywalker);

        HttpRequest requestKenobi = HttpRequest.newBuilder().uri(URI.create(functionUrl + "kenobi")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodyKenobi)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSkywalker = HttpRequest.newBuilder().uri(URI.create(functionUrl + "skywalker")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodySkywalker)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSato = HttpRequest.newBuilder().uri(URI.create(functionUrl + "sato")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodySato)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();


        StopWatch elapsedTimeKenobi = new StopWatch();
        elapsedTimeKenobi.start();

        CompletableFuture<HttpResponse<String>> responseKenobi = client.sendAsync(requestKenobi, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .whenComplete((stringHttpResponse, throwable) -> elapsedTimeKenobi.stop());

        StopWatch elapsedTimeSkywalker = new StopWatch();
        elapsedTimeSkywalker.start();

        CompletableFuture<HttpResponse<String>> responseSkywalker = client.sendAsync(requestSkywalker, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .whenComplete((stringHttpResponse, throwable) -> elapsedTimeSkywalker.stop());


        Thread.sleep(Double.valueOf(timeout * 1000 * 0.85).longValue());

        CompletableFuture<HttpResponse<String>> responseSato = client.sendAsync(requestSato, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpResponse<String> resultKenobi = responseKenobi.get();
        HttpResponse<String> resultSkywalker = responseSkywalker.get();
        HttpResponse<String> resultSato = responseSato.get();

        ResponseObject correctResult = new ResponseObject(BasicFunctions.GetLocation(testDistances), BasicFunctions.GetMessage(messageStringList));

        assertThat(resultKenobi.statusCode(), equalTo(HttpURLConnection.HTTP_OK));
        assertThat(resultSkywalker.statusCode(), equalTo(HttpURLConnection.HTTP_OK));
        assertThat(resultSato.statusCode(), equalTo(HttpURLConnection.HTTP_OK));

        assertThat(elapsedTimeKenobi.getTotalTimeSeconds(), lessThanOrEqualTo(Double.valueOf(timeout)));
        assertThat(elapsedTimeSkywalker.getTotalTimeSeconds(), lessThanOrEqualTo(Double.valueOf(timeout)));

        assertThat(resultKenobi.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
        assertThat(resultSkywalker.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
        assertThat(resultSato.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
    }


    @Test
    public void verifyFourConcurrentRequests() throws Throwable {

        Thread.sleep(10000);

        Map<String, Object> messageKenobi = new HashMap<>();
        messageKenobi.put("message", testMessagesArray.KENOBI.getArray());
        messageKenobi.put("distance", testDistance.KENOBI.getDistance());

        Map<String, Object> messageSato = new HashMap<>();
        messageSato.put("message", testMessagesArray.SATO.getArray());
        messageSato.put("distance", testDistance.SATO.getDistance());

        Map<String, Object> messageSkywalker = new HashMap<>();
        messageSkywalker.put("message", testMessagesArray.SKYWALKER.getArray());
        messageSkywalker.put("distance", testDistance.SKYWALKER.getDistance());

        
        String requestBodyKenobi = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageKenobi);
        String requestBodySato = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageSato);
        String requestBodySkywalker = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageSkywalker);

        HttpRequest requestKenobi = HttpRequest.newBuilder().uri(URI.create(functionUrl + "kenobi")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodyKenobi)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSkywalker = HttpRequest.newBuilder().uri(URI.create(functionUrl + "skywalker")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodySkywalker)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSato = HttpRequest.newBuilder().uri(URI.create(functionUrl + "sato")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodySato)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();


        StopWatch elapsedTimeKenobi = new StopWatch();
        elapsedTimeKenobi.start();

        CompletableFuture<HttpResponse<String>> responseKenobi = client.sendAsync(requestKenobi, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .whenComplete((stringHttpResponse, throwable) -> elapsedTimeKenobi.stop());

        StopWatch elapsedTimeSkywalker = new StopWatch();
        elapsedTimeSkywalker.start();

        CompletableFuture<HttpResponse<String>> responseSkywalker = client.sendAsync(requestSkywalker, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .whenComplete((stringHttpResponse, throwable) -> elapsedTimeSkywalker.stop());

        StopWatch elapsedTimeSato = new StopWatch();
        elapsedTimeSato.start();

        CompletableFuture<HttpResponse<String>> responseSato = client.sendAsync(requestSato, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .whenComplete((stringHttpResponse, throwable) -> elapsedTimeSato.stop());


        //Extra request

        CountDownLatch waitForSkywalker = new CountDownLatch(1);

        OkHttpClient okHttpClient = new OkHttpClient.Builder().callTimeout(120, TimeUnit.SECONDS).readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS).connectTimeout(120, TimeUnit.SECONDS).build();

        Request requestSkywalker2 = new Request.Builder().url(functionUrl + "skywalker").method(HttpMethod.POST.asString(), RequestBody.create(requestBodySkywalker, JSON)).build();

        StopWatch elapsedTimeSkywalker2 = new StopWatch();
        elapsedTimeSkywalker2.start();

        okHttpClient.newCall(requestSkywalker2).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                waitForSkywalker.countDown();
                assertThat("a", equalTo("b"));
                elapsedTimeSkywalker2.stop();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                elapsedTimeSkywalker2.stop();
                waitForSkywalker.countDown();
                assertThat(response.code(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));
                assertThat(response.body().string(), equalTo(messageSource.getMessage("INVALID_JSON_MESSAGE", null, Locale.US)));
                response.close();
                assertThat(elapsedTimeSkywalker2.getTotalTimeSeconds(), greaterThanOrEqualTo(Double.valueOf(timeout)));
            }
        });

        HttpResponse<String> resultKenobi = responseKenobi.get();
        HttpResponse<String> resultSkywalker = responseSkywalker.get();
        HttpResponse<String> resultSato = responseSato.get();

        ResponseObject correctResult = new ResponseObject(BasicFunctions.GetLocation(testDistances), BasicFunctions.GetMessage(messageStringList));

        assertThat(resultKenobi.statusCode(), equalTo(HttpURLConnection.HTTP_OK));
        assertThat(resultSkywalker.statusCode(), equalTo(HttpURLConnection.HTTP_OK));
        assertThat(resultSato.statusCode(), equalTo(HttpURLConnection.HTTP_OK));

        assertThat(elapsedTimeKenobi.getTotalTimeSeconds(), lessThanOrEqualTo(timeout * 1000 * 0.2));
        assertThat(elapsedTimeSkywalker.getTotalTimeSeconds(), lessThanOrEqualTo(timeout * 1000 * 0.2));
        assertThat(elapsedTimeSato.getTotalTimeSeconds(), lessThanOrEqualTo(timeout * 1000 * 0.2));

        assertThat(resultKenobi.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
        assertThat(resultSkywalker.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
        assertThat(resultSato.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
        waitForSkywalker.await();

        Thread.sleep(10000);
    }

    @Test
    public void verifyFiveConcurrentRequests() throws Throwable {
        Thread.sleep(10000);

        Map<String, Object> messageKenobi = new HashMap<>();
        messageKenobi.put("message", testMessagesArray.KENOBI.getArray());
        messageKenobi.put("distance", testDistance.KENOBI.getDistance());

        Map<String, Object> messageSato = new HashMap<>();
        messageSato.put("message", testMessagesArray.SATO.getArray());
        messageSato.put("distance", testDistance.SATO.getDistance());

        Map<String, Object> messageSkywalker = new HashMap<>();
        messageSkywalker.put("message", testMessagesArray.SKYWALKER.getArray());
        messageSkywalker.put("distance", testDistance.SKYWALKER.getDistance());

        
        String requestBodyKenobi = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageKenobi);
        String requestBodySato = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageSato);
        String requestBodySkywalker = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageSkywalker);

        HttpRequest requestKenobi = HttpRequest.newBuilder().uri(URI.create(functionUrl + "kenobi")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodyKenobi)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSkywalker = HttpRequest.newBuilder().uri(URI.create(functionUrl + "skywalker")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodySkywalker)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSato = HttpRequest.newBuilder().uri(URI.create(functionUrl + "sato")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodySato)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();


        StopWatch elapsedTimeKenobi = new StopWatch();
        elapsedTimeKenobi.start();

        CompletableFuture<HttpResponse<String>> responseKenobi = client.sendAsync(requestKenobi, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .whenComplete((stringHttpResponse, throwable) -> elapsedTimeKenobi.stop());

        StopWatch elapsedTimeSkywalker = new StopWatch();
        elapsedTimeSkywalker.start();

        CompletableFuture<HttpResponse<String>> responseSkywalker = client.sendAsync(requestSkywalker, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .whenComplete((stringHttpResponse, throwable) -> elapsedTimeSkywalker.stop());

        StopWatch elapsedTimeSato = new StopWatch();
        elapsedTimeSato.start();

        CompletableFuture<HttpResponse<String>> responseSato = client.sendAsync(requestSato, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .whenComplete((stringHttpResponse, throwable) -> elapsedTimeSato.stop());

        //Extra request

        CountDownLatch waitForSkywalker = new CountDownLatch(1);

        OkHttpClient okHttpClient = new OkHttpClient.Builder().callTimeout(120, TimeUnit.SECONDS).readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS).connectTimeout(120, TimeUnit.SECONDS).build();

        Request requestSkywalker2 = new Request.Builder().url(functionUrl + "skywalker").method(HttpMethod.POST.asString(), RequestBody.create(requestBodySkywalker, JSON)).build();

        StopWatch elapsedTimeSkywalker2 = new StopWatch();
        elapsedTimeSkywalker2.start();

        okHttpClient.newCall(requestSkywalker2).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                waitForSkywalker.countDown();
                assertThat("a", equalTo("b"));
                elapsedTimeSkywalker2.stop();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                elapsedTimeSkywalker2.stop();
                waitForSkywalker.countDown();
                assertThat(response.code(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));
                assertThat(response.body().string(), equalTo(messageSource.getMessage("INVALID_JSON_MESSAGE", null, Locale.US)));
                response.close();
                assertThat(elapsedTimeSkywalker2.getTotalTimeSeconds(), greaterThanOrEqualTo(Double.valueOf(timeout)));
            }
        });


        CountDownLatch waitForSato = new CountDownLatch(1);

        //Extra request
        Request requestSato2 = new Request.Builder().url(functionUrl + "sato").method(HttpMethod.POST.asString(), RequestBody.create(requestBodySato, JSON)).build();

        StopWatch elapsedTimeSato2 = new StopWatch();
        elapsedTimeSato2.start();

        okHttpClient.newCall(requestSato2).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                waitForSato.countDown();
                assertThat("a", equalTo("b"));
                elapsedTimeSato2.stop();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                elapsedTimeSato2.stop();
                waitForSato.countDown();
                assertThat(response.code(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));
                assertThat(response.body().string(), equalTo(messageSource.getMessage("INVALID_JSON_MESSAGE", null, Locale.US)));
                response.close();
                assertThat(elapsedTimeSato2.getTotalTimeSeconds(), greaterThanOrEqualTo(Double.valueOf(timeout)));
            }
        });

        HttpResponse<String> resultKenobi = responseKenobi.get();
        HttpResponse<String> resultSkywalker = responseSkywalker.get();
        HttpResponse<String> resultSato = responseSato.get();

        ResponseObject correctResult = new ResponseObject(BasicFunctions.GetLocation(testDistances), BasicFunctions.GetMessage(messageStringList));

        assertThat(resultKenobi.statusCode(), equalTo(HttpURLConnection.HTTP_OK));
        assertThat(resultSkywalker.statusCode(), equalTo(HttpURLConnection.HTTP_OK));
        assertThat(resultSato.statusCode(), equalTo(HttpURLConnection.HTTP_OK));

        assertThat(elapsedTimeKenobi.getTotalTimeSeconds(), lessThanOrEqualTo(timeout * 1000 * 0.2));
        assertThat(elapsedTimeSkywalker.getTotalTimeSeconds(), lessThanOrEqualTo(timeout * 1000 * 0.2));
        assertThat(elapsedTimeSato.getTotalTimeSeconds(), lessThanOrEqualTo(timeout * 1000 * 0.2));

        assertThat(resultKenobi.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
        assertThat(resultSkywalker.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
        assertThat(resultSato.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
        waitForSkywalker.await();
        waitForSato.await();

        Thread.sleep(10000);
    }


    @Test
    public void verifySixConcurrentRequests() throws Throwable {

        Thread.sleep(10000);

        Map<String, Object> messageKenobi = new HashMap<>();
        messageKenobi.put("message", testMessagesArray.KENOBI.getArray());
        messageKenobi.put("distance", testDistance.KENOBI.getDistance());

        Map<String, Object> messageSato = new HashMap<>();
        messageSato.put("message", testMessagesArray.SATO.getArray());
        messageSato.put("distance", testDistance.SATO.getDistance());

        Map<String, Object> messageSkywalker = new HashMap<>();
        messageSkywalker.put("message", testMessagesArray.SKYWALKER.getArray());
        messageSkywalker.put("distance", testDistance.SKYWALKER.getDistance());

        
        String requestBodyKenobi = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageKenobi);
        String requestBodySato = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageSato);
        String requestBodySkywalker = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageSkywalker);

        HttpRequest requestKenobi = HttpRequest.newBuilder().uri(URI.create(functionUrl + "kenobi")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodyKenobi)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSkywalker = HttpRequest.newBuilder().uri(URI.create(functionUrl + "skywalker")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodySkywalker)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSato = HttpRequest.newBuilder().uri(URI.create(functionUrl + "sato")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodySato)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();


        StopWatch elapsedTimeKenobi = new StopWatch();
        elapsedTimeKenobi.start();

        CompletableFuture<HttpResponse<String>> responseKenobi = client.sendAsync(requestKenobi, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .whenComplete((stringHttpResponse, throwable) -> elapsedTimeKenobi.stop());

        StopWatch elapsedTimeSkywalker = new StopWatch();
        elapsedTimeSkywalker.start();

        CompletableFuture<HttpResponse<String>> responseSkywalker = client.sendAsync(requestSkywalker, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .whenComplete((stringHttpResponse, throwable) -> elapsedTimeSkywalker.stop());

        StopWatch elapsedTimeSato = new StopWatch();
        elapsedTimeSato.start();

        CompletableFuture<HttpResponse<String>> responseSato = client.sendAsync(requestSato, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .whenComplete((stringHttpResponse, throwable) -> elapsedTimeSato.stop());

        ResponseObject correctResult = new ResponseObject(BasicFunctions.GetLocation(testDistances), BasicFunctions.GetMessage(messageStringList));

        //Extra requests

        CountDownLatch waitForKenobi = new CountDownLatch(1);

        OkHttpClient okHttpClient = new OkHttpClient.Builder().callTimeout(120, TimeUnit.SECONDS).readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS).connectTimeout(120, TimeUnit.SECONDS).build();

        Request requestKenobi2 = new Request.Builder().url(functionUrl + "kenobi").method(HttpMethod.POST.asString(), RequestBody.create(requestBodyKenobi, JSON)).build();

        StopWatch elapsedTimeKenobi2 = new StopWatch();
        elapsedTimeKenobi2.start();

        okHttpClient.newCall(requestKenobi2).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                waitForKenobi.countDown();
                assertThat("a", equalTo("b"));
                elapsedTimeKenobi2.stop();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                waitForKenobi.countDown();
                elapsedTimeKenobi2.stop();
                assertThat(response.code(), equalTo(HttpURLConnection.HTTP_OK));
                assertThat(response.body().string(), equalTo(objectMapper.writeValueAsString(correctResult)));
                response.close();
                assertThat(elapsedTimeKenobi2.getTotalTimeSeconds(), lessThanOrEqualTo(timeout * 1000 * 0.2));
            }
        });


        CountDownLatch waitForSkywalker = new CountDownLatch(1);

        Request requestSkywalker2 = new Request.Builder().url(functionUrl + "skywalker").method(HttpMethod.POST.asString(), RequestBody.create(requestBodySkywalker, JSON)).build();

        StopWatch elapsedTimeSkywalker2 = new StopWatch();
        elapsedTimeSkywalker2.start();

        okHttpClient.newCall(requestSkywalker2).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                waitForSkywalker.countDown();
                assertThat("a", equalTo("b"));
                elapsedTimeSkywalker2.stop();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                elapsedTimeSkywalker2.stop();
                waitForSkywalker.countDown();
                assertThat(response.code(), equalTo(HttpURLConnection.HTTP_OK));
                assertThat(response.body().string(), equalTo(objectMapper.writeValueAsString(correctResult)));
                response.close();
                assertThat(elapsedTimeSkywalker2.getTotalTimeSeconds(), lessThanOrEqualTo(timeout * 1000 * 0.2));
            }
        });

        //Extra request

        CountDownLatch waitForSato = new CountDownLatch(1);

        Request requestSato2 = new Request.Builder().url(functionUrl + "sato").method(HttpMethod.POST.asString(), RequestBody.create(requestBodySato, JSON)).build();

        StopWatch elapsedTimeSato2 = new StopWatch();
        elapsedTimeSato2.start();

        okHttpClient.newCall(requestSato2).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                waitForSato.countDown();
                assertThat("a", equalTo("b"));
                elapsedTimeSato2.stop();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                waitForSato.countDown();
                elapsedTimeSato2.stop();
                assertThat(response.code(), equalTo(HttpURLConnection.HTTP_OK));
                assertThat(response.body().string(), equalTo(objectMapper.writeValueAsString(correctResult)));
                response.close();
                assertThat(elapsedTimeSato2.getTotalTimeSeconds(), lessThanOrEqualTo(timeout * 1000 * 0.2));
            }
        });


        HttpResponse<String> resultKenobi = responseKenobi.get();
        HttpResponse<String> resultSkywalker = responseSkywalker.get();
        HttpResponse<String> resultSato = responseSato.get();


        assertThat(resultKenobi.statusCode(), equalTo(HttpURLConnection.HTTP_OK));
        assertThat(resultSkywalker.statusCode(), equalTo(HttpURLConnection.HTTP_OK));
        assertThat(resultSato.statusCode(), equalTo(HttpURLConnection.HTTP_OK));

        assertThat(elapsedTimeKenobi.getTotalTimeSeconds(), lessThanOrEqualTo(timeout * 1000 * 0.2));
        assertThat(elapsedTimeSkywalker.getTotalTimeSeconds(), lessThanOrEqualTo(timeout * 1000 * 0.2));
        assertThat(elapsedTimeSato.getTotalTimeSeconds(), lessThanOrEqualTo(timeout * 1000 * 0.2));

        assertThat(resultKenobi.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
        assertThat(resultSkywalker.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
        assertThat(resultSato.body(), equalTo(objectMapper.writeValueAsString(correctResult)));

        waitForKenobi.await();
        waitForSkywalker.await();
        waitForSato.await();

        Thread.sleep(10000);
    }

    @Test
    public void verifyIncorrectNoIntersectionJson() throws Throwable {
        Map<String, Object> messageKenobi = new HashMap<>();
        messageKenobi.put("message", testMessagesArray.KENOBI.getArray());
        messageKenobi.put("distance", testDistanceInvalid.KENOBI.getDistance());

        Map<String, Object> messageSato = new HashMap<>();
        messageSato.put("message", testMessagesArray.SATO.getArray());
        messageSato.put("distance", testDistanceInvalid.SATO.getDistance());

        Map<String, Object> messageSkywalker = new HashMap<>();
        messageSkywalker.put("message", testMessagesArray.SKYWALKER.getArray());
        messageSkywalker.put("distance", testDistanceInvalid.SKYWALKER.getDistance());

        
        String requestBodyKenobi = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageKenobi);
        String requestBodySato = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageSato);
        String requestBodySkywalker = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageSkywalker);

        HttpRequest requestKenobi = HttpRequest.newBuilder().uri(URI.create(functionUrl + "kenobi")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodyKenobi)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSkywalker = HttpRequest.newBuilder().uri(URI.create(functionUrl + "skywalker")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodySkywalker)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSato = HttpRequest.newBuilder().uri(URI.create(functionUrl + "sato")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodySato)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();

        CompletableFuture<HttpResponse<String>> responseKenobi = client.sendAsync(requestKenobi, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        CompletableFuture<HttpResponse<String>> responseSkywalker = client.sendAsync(requestSkywalker, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        CompletableFuture<HttpResponse<String>> responseSato = client.sendAsync(requestSato, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpResponse<String> resultKenobi = responseKenobi.get();
        HttpResponse<String> resultSkywalker = responseSkywalker.get();
        HttpResponse<String> resultSato = responseSato.get();

        assertThat(resultKenobi.statusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));
        assertThat(resultSkywalker.statusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));
        assertThat(resultSato.statusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));

        assertThat(resultKenobi.body(), equalTo(messageSource.getMessage("INSUFFICIENT_DATA_MESSAGE", null, Locale.US)));
        assertThat(resultSkywalker.body(), equalTo(messageSource.getMessage("INSUFFICIENT_DATA_MESSAGE", null, Locale.US)));
        assertThat(resultSato.body(), equalTo(messageSource.getMessage("INSUFFICIENT_DATA_MESSAGE", null, Locale.US)));
    }

    @Test
    public void verifyIncorrectNoDecodedMessageJson() throws Throwable {
        Map<String, Object> messageKenobi = new HashMap<>();
        messageKenobi.put("message", testMessagesArrayInvalid.KENOBI.getArray());
        messageKenobi.put("distance", testDistance.KENOBI.getDistance());

        Map<String, Object> messageSato = new HashMap<>();
        messageSato.put("message", testMessagesArrayInvalid.SATO.getArray());
        messageSato.put("distance", testDistance.SATO.getDistance());

        Map<String, Object> messageSkywalker = new HashMap<>();
        messageSkywalker.put("message", testMessagesArrayInvalid.SKYWALKER.getArray());
        messageSkywalker.put("distance", testDistance.SKYWALKER.getDistance());

        
        String requestBodyKenobi = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageKenobi);
        String requestBodySato = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageSato);
        String requestBodySkywalker = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageSkywalker);

        HttpRequest requestKenobi = HttpRequest.newBuilder().uri(URI.create(functionUrl + "kenobi")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodyKenobi)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSkywalker = HttpRequest.newBuilder().uri(URI.create(functionUrl + "skywalker")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodySkywalker)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSato = HttpRequest.newBuilder().uri(URI.create(functionUrl + "sato")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBodySato)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();

        CompletableFuture<HttpResponse<String>> responseKenobi = client.sendAsync(requestKenobi, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        CompletableFuture<HttpResponse<String>> responseSkywalker = client.sendAsync(requestSkywalker, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        CompletableFuture<HttpResponse<String>> responseSato = client.sendAsync(requestSato, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpResponse<String> resultKenobi = responseKenobi.get();
        HttpResponse<String> resultSkywalker = responseSkywalker.get();
        HttpResponse<String> resultSato = responseSato.get();

        assertThat(resultKenobi.statusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));
        assertThat(resultSkywalker.statusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));
        assertThat(resultSato.statusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));

        assertThat(resultKenobi.body(), equalTo(messageSource.getMessage("INSUFFICIENT_DATA_MESSAGE", null, Locale.US)));
        assertThat(resultSkywalker.body(), equalTo(messageSource.getMessage("INSUFFICIENT_DATA_MESSAGE", null, Locale.US)));
        assertThat(resultSato.body(), equalTo(messageSource.getMessage("INSUFFICIENT_DATA_MESSAGE", null, Locale.US)));
    }
}