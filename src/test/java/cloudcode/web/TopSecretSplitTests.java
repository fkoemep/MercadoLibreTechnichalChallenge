package cloudcode.web;

import cloudcode.BasicFunctions;
import cloudcode.objects.ResponseObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application.properties")


public class TopSecretSplitTests {

    private final String BASE_URL = "http://localhost:8080";
//    private static final String BASE_URL_PROD = "http://localhost:8080";
    private final String functionUrl = BASE_URL + "/topsecret_split/";

    private final HttpClient client = HttpClient.newHttpClient();
    private final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource(){{setBasename("messages");}};

    @Value("#{ T(java.lang.Long).parseLong('${splitSatelliteMessage.timeout}')}")
    private Long timeout;

    @Test
    public void verifyCorrectJsonPOSTRequest() throws Throwable {
        Map<String, Object> message = new HashMap<>();
        message.put("message", new String[]{"a","",""});
        message.put("distance", 0.0);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);

        HttpRequest requestKenobi = HttpRequest.newBuilder().uri(URI.create(functionUrl + "kenobi")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBody)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSkywalker = HttpRequest.newBuilder().uri(URI.create(functionUrl + "skywalker")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBody)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSato = HttpRequest.newBuilder().uri(URI.create(functionUrl + "sato")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBody)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();

        CompletableFuture<HttpResponse<String>> responseKenobi = client.sendAsync(requestKenobi, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        CompletableFuture<HttpResponse<String>> responseSkywalker = client.sendAsync(requestSkywalker, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        CompletableFuture<HttpResponse<String>> responseSato = client.sendAsync(requestSato, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpResponse<String> resultKenobi = responseKenobi.get();
        HttpResponse<String> resultSkywalker = responseSkywalker.get();
        HttpResponse<String> resultSato = responseSato.get();

        ResponseObject correctResult = new ResponseObject(BasicFunctions.GetLocation(new ArrayList<>()), BasicFunctions.GetMessage(new ArrayList<>()));


        assertThat(resultKenobi.statusCode(), equalTo(HttpURLConnection.HTTP_OK));
        assertThat(resultSkywalker.statusCode(), equalTo(HttpURLConnection.HTTP_OK));
        assertThat(resultSato.statusCode(), equalTo(HttpURLConnection.HTTP_OK));

        assertThat(resultKenobi.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
        assertThat(resultSkywalker.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
        assertThat(resultSato.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
    }

    @Test
    public void verifyCorrectJsonGETRequest() throws Throwable {
        Map<String, Object> message = new HashMap<>();
        message.put("message", new String[]{"a","",""});
        message.put("distance", 0.0);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);

        HttpRequest requestKenobi = HttpRequest.newBuilder().uri(URI.create(functionUrl + "kenobi")).
                method(HttpMethod.GET.asString(), HttpRequest.BodyPublishers.ofString(requestBody)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSkywalker = HttpRequest.newBuilder().uri(URI.create(functionUrl + "skywalker")).
                method(HttpMethod.GET.asString(), HttpRequest.BodyPublishers.ofString(requestBody)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSato = HttpRequest.newBuilder().uri(URI.create(functionUrl + "sato")).
                method(HttpMethod.GET.asString(), HttpRequest.BodyPublishers.ofString(requestBody)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();

        CompletableFuture<HttpResponse<String>> responseKenobi = client.sendAsync(requestKenobi, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        CompletableFuture<HttpResponse<String>> responseSkywalker = client.sendAsync(requestSkywalker, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        CompletableFuture<HttpResponse<String>> responseSato = client.sendAsync(requestSato, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpResponse<String> resultKenobi = responseKenobi.get();
        HttpResponse<String> resultSkywalker = responseSkywalker.get();
        HttpResponse<String> resultSato = responseSato.get();

        ResponseObject correctResult = new ResponseObject(BasicFunctions.GetLocation(new ArrayList<>()), BasicFunctions.GetMessage(new ArrayList<>()));

        assertThat(resultKenobi.statusCode(), equalTo(HttpURLConnection.HTTP_OK));
        assertThat(resultSkywalker.statusCode(), equalTo(HttpURLConnection.HTTP_OK));
        assertThat(resultSato.statusCode(), equalTo(HttpURLConnection.HTTP_OK));

        assertThat(resultKenobi.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
        assertThat(resultSkywalker.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
        assertThat(resultSato.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
    }

    @Test
    public void verifyCorrectJsonMixedRequest() throws Throwable {
        Map<String, Object> message = new HashMap<>();
        message.put("message", new String[]{"a","",""});
        message.put("distance", 0.0);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);

        HttpRequest requestKenobi = HttpRequest.newBuilder().uri(URI.create(functionUrl + "kenobi")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBody)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSkywalker = HttpRequest.newBuilder().uri(URI.create(functionUrl + "skywalker")).
                method(HttpMethod.GET.asString(), HttpRequest.BodyPublishers.ofString(requestBody)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSato = HttpRequest.newBuilder().uri(URI.create(functionUrl + "sato")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBody)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();

        CompletableFuture<HttpResponse<String>> responseKenobi = client.sendAsync(requestKenobi, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        CompletableFuture<HttpResponse<String>> responseSkywalker = client.sendAsync(requestSkywalker, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        CompletableFuture<HttpResponse<String>> responseSato = client.sendAsync(requestSato, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpResponse<String> resultKenobi = responseKenobi.get();
        HttpResponse<String> resultSkywalker = responseSkywalker.get();
        HttpResponse<String> resultSato = responseSato.get();

        ResponseObject correctResult = new ResponseObject(BasicFunctions.GetLocation(new ArrayList<>()), BasicFunctions.GetMessage(new ArrayList<>()));


        assertThat(resultKenobi.statusCode(), equalTo(HttpURLConnection.HTTP_OK));
        assertThat(resultSkywalker.statusCode(), equalTo(HttpURLConnection.HTTP_OK));
        assertThat(resultSato.statusCode(), equalTo(HttpURLConnection.HTTP_OK));

        assertThat(resultKenobi.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
        assertThat(resultSkywalker.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
        assertThat(resultSato.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
    }

    @Test
    public void verifyIncorrectMethod() throws Throwable {
        Map<String, Object> message = new HashMap<>();
        message.put("message", new String[]{"a","",""});
        message.put("distance", 0.0);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);

        HttpRequest requestKenobi = HttpRequest.newBuilder().uri(URI.create(functionUrl + "kenobi")).
                method(HttpMethod.PUT.asString(), HttpRequest.BodyPublishers.ofString(requestBody)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();

        CompletableFuture<HttpResponse<String>> responseKenobi = client.sendAsync(requestKenobi, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpResponse<String> resultKenobi = responseKenobi.get();

        assertThat(resultKenobi.statusCode(), equalTo(HttpURLConnection.HTTP_BAD_METHOD));
    }

    @Test
    public void verifyIncorrectSatelliteNameJson() throws Throwable {
        Map<String, Object> message = new HashMap<>();
        message.put("message", new String[]{"a","",""});
        message.put("distance", 0.0);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);

        HttpRequest requestKenobi = HttpRequest.newBuilder().uri(URI.create(functionUrl + "kenobii")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBody)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSkywalker = HttpRequest.newBuilder().uri(URI.create(functionUrl + "skywalker")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBody)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSato = HttpRequest.newBuilder().uri(URI.create(functionUrl + "sato")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBody)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();

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
        Map<String, Object> message = new HashMap<>();
        message.put("message", new String[]{"a","",""});
        message.put("distance", 0.0);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);

        HttpRequest requestKenobi = HttpRequest.newBuilder().uri(URI.create(functionUrl + "kenobi")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBody)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSato = HttpRequest.newBuilder().uri(URI.create(functionUrl + "sato")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBody)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();

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
        Map<String, Object> message = new HashMap<>();
        message.put("message", new String[]{"a","",""});
        message.put("distance", 0.0);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);

        HttpRequest requestKenobi = HttpRequest.newBuilder().uri(URI.create(functionUrl + "kenobi")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBody)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSato = HttpRequest.newBuilder().uri(URI.create(functionUrl + "sato")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBody)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();


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
        Map<String, Object> message = new HashMap<>();
        message.put("message", 0);
        message.put("distance", "a");

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);

        HttpRequest requestKenobi = HttpRequest.newBuilder().uri(URI.create(functionUrl + "kenobi")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBody)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSkywalker = HttpRequest.newBuilder().uri(URI.create(functionUrl + "skywalker")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBody)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSato = HttpRequest.newBuilder().uri(URI.create(functionUrl + "sato")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBody)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();

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
        Map<String, Object> message = new HashMap<>();
        message.put("message", new String[]{"a","",""});
        message.put("distance", 0.0);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);

        HttpRequest requestKenobi = HttpRequest.newBuilder().uri(URI.create(functionUrl + "kenobi")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBody)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSkywalker = HttpRequest.newBuilder().uri(URI.create(functionUrl + "skywalker")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBody)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
        HttpRequest requestSato = HttpRequest.newBuilder().uri(URI.create(functionUrl + "sato")).
                method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBody)).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();


        StopWatch elapsedTimeKenobi = new StopWatch();
        elapsedTimeKenobi.start();

        CompletableFuture<HttpResponse<String>> responseKenobi = client.sendAsync(requestKenobi, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .whenComplete((stringHttpResponse, throwable) -> elapsedTimeKenobi.stop());

        StopWatch elapsedTimeSkywalker = new StopWatch();
        elapsedTimeSkywalker.start();

        CompletableFuture<HttpResponse<String>> responseSkywalker = client.sendAsync(requestSkywalker, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .whenComplete((stringHttpResponse, throwable) -> elapsedTimeSkywalker.stop());


        Thread.sleep(Double.valueOf(timeout * 1000 * 0.85).longValue());

        CompletableFuture<HttpResponse<String>> responseSato = client.sendAsync(requestSato, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .whenComplete((stringHttpResponse, throwable) -> elapsedTimeSkywalker.stop());


        HttpResponse<String> resultKenobi = responseKenobi.get();
        HttpResponse<String> resultSkywalker = responseSkywalker.get();
        HttpResponse<String> resultSato = responseSato.get();

        ResponseObject correctResult = new ResponseObject(BasicFunctions.GetLocation(new ArrayList<>()), BasicFunctions.GetMessage(new ArrayList<>()));

        assertThat(resultKenobi.statusCode(), equalTo(HttpURLConnection.HTTP_OK));
        assertThat(resultSkywalker.statusCode(), equalTo(HttpURLConnection.HTTP_OK));
        assertThat(resultSato.statusCode(), equalTo(HttpURLConnection.HTTP_OK));

        assertThat(elapsedTimeKenobi.getTotalTimeSeconds(), lessThanOrEqualTo(Double.valueOf(timeout)));
        assertThat(elapsedTimeSkywalker.getTotalTimeSeconds(), lessThanOrEqualTo(Double.valueOf(timeout)));

        assertThat(resultKenobi.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
        assertThat(resultSkywalker.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
        assertThat(resultSato.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
    }


//concurrency
}