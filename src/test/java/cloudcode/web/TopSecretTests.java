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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TopSecretTests {
    private final String BASE_URL = "http://localhost:8080";
//    private static final String BASE_URL_PROD = "http://localhost:8080";
    private final String functionUrl = BASE_URL + "/topsecret/";

    private final HttpClient client = HttpClient.newHttpClient();

    private final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource(){{setBasename("messages");}};

    @Test
    public void verifyCorrectJson() throws Throwable {
        List<Map<String, Object>> messageList = new ArrayList<>();

        Map<String, Object> messageKenobi = new HashMap<>();
        messageKenobi.put("name", "kenobi");
        messageKenobi.put("message", new String[]{"a","",""});
        messageKenobi.put("distance", 0.0);
        messageList.add(messageKenobi);

        Map<String, Object> messageSkywalker = new HashMap<>();
        messageSkywalker.put("name", "skywalker");
        messageSkywalker.put("message", new String[]{"","b",""});
        messageSkywalker.put("distance", 0.1);
        messageList.add(messageSkywalker);

        Map<String, Object> messageSato = new HashMap<>();
        messageSato.put("name", "sato");
        messageSato.put("message", new String[]{"","","c"});
        messageSato.put("distance", 0.1);
        messageList.add(messageSato);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(Map.of("satellites",messageList));

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(functionUrl))
                .method(HttpMethod.POST.asString(), HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        ResponseObject correctResult = new ResponseObject(BasicFunctions.GetLocation(new ArrayList<>()), BasicFunctions.GetMessage(new ArrayList<>()));

        assertThat(response.statusCode(), equalTo(HttpURLConnection.HTTP_OK));
        assertThat(response.body(), equalTo(objectMapper.writeValueAsString(correctResult)));
    }

    @Test
    public void verifyIncorrectJson() throws Throwable {
            List<Map<String, Object>> messageList = new ArrayList<>();

            Map<String, Object> messageKenobi = new HashMap<>();
            messageKenobi.put("name", "kenobi");
            messageKenobi.put("message", new String[]{"a","",""});
            messageKenobi.put("distance", 0.0);
            messageList.add(messageKenobi);

            Map<String, Object> messageSkywalker = new HashMap<>();
            messageSkywalker.put("name", "skywalker");
            messageSkywalker.put("message", new String[]{"","b",""});
            messageSkywalker.put("distance", 0.1);
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
        messageKenobi.put("message", new String[]{"a","",""});
        messageKenobi.put("distance", 0.0);
        messageList.add(messageKenobi);

        Map<String, Object> messageSkywalker = new HashMap<>();
        messageSkywalker.put("name", "skywalker");
        messageSkywalker.put("message", new String[]{"","b",""});
        messageSkywalker.put("distance", 0.1);
        messageList.add(messageSkywalker);

        Map<String, Object> messageSato = new HashMap<>();
        messageSato.put("name", "sato");
        messageSato.put("message", new String[]{"","","c"});
        messageSato.put("distance", 0.1);
        messageList.add(messageSato);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(Map.of("satellites",messageList));

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(functionUrl))
                .method(HttpMethod.GET.asString(), HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertThat(response.statusCode(), equalTo(HttpURLConnection.HTTP_BAD_METHOD));
    }
}