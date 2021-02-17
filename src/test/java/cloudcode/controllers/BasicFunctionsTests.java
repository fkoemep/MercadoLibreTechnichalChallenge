package cloudcode.controllers;

import cloudcode.BasicFunctions;
import cloudcode.entities.Location;
import cloudcode.exceptions.LocationProcessingException;
import cloudcode.exceptions.MessageProcessingException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BasicFunctionsTests {

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

    private final Double[] testDistancesInvalid = new Double[]{testDistanceInvalid.KENOBI.distance, testDistanceInvalid.SATO.distance, testDistanceInvalid.SKYWALKER.distance};

    private final String correctDecodedMessage = "";

    private final List<String[]> messageList = new ArrayList<>(){{
        add(new String[]{"aaaaaaaaaaaaa", "", "cccccccccccc", "   "});
        add(new String[]{"", "55555", ""});
        add(new String[]{"", "", "7777777", " ", "qqqqq"});
    }};

    private final List<String[]> messageListInvalid = new ArrayList<>(){{
        add(new String[]{"", "", ""});
        add(new String[]{"", "", ""});
        add(new String[]{"", "", ""});
    }};

    private final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource(){{setBasename("messages");}};

    private final double correctCalculatedLocationX = (-100.0/37.0)*(53.0 + 6.0 * Math.sqrt(3.0));
    private final double correctCalculatedLocationY = (400.0/37.0)*(-13.0 + 9.0 * Math.sqrt(3.0));

    @Test
    public void verifyCorrectLocationCalculation() throws Throwable {
        try{
            Location location = BasicFunctions.GetLocation(testDistances);
            assertThat(location, notNullValue());

            assertThat(location.getX(), closeTo(correctCalculatedLocationX, 0.000001));

            assertThat(location.getY(), closeTo(correctCalculatedLocationY, 0.000001));
        }
        catch (LocationProcessingException e){
            assertThat("a", equalTo("b"));
        }
    }

    @Test
    public void verifyIncorrectLocationsError() throws Throwable {
        try{
            BasicFunctions.GetLocation(testDistancesInvalid);
            assertThat("a", equalTo("b"));
        }
        catch (LocationProcessingException e){
            assertThat(e.getMessage(), equalTo(""));
        }
    }

    @Test
    public void verifyCorrectMessageCalculation() throws Throwable {
        try {
            String message = BasicFunctions.GetMessage(messageList);
            assertThat(message, notNullValue());
            assertThat(message, equalTo(correctDecodedMessage));
        }
        catch (MessageProcessingException e){
            assertThat("a", equalTo("b"));
        }
    }

    @Test
    public void verifyIncorrectMessagesError() throws Throwable {
        try{
            BasicFunctions.GetMessage(messageListInvalid);
            assertThat("a", equalTo("b"));
        }
        catch (MessageProcessingException e){
            assertThat(e.getMessage(), equalTo(messageSource.getMessage("INSUFFICIENT_DATA_MESSAGE", null, Locale.US)));
        }
    }
}