package cloudcode.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.json.JsonParseException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RequestObject {

    private SatelliteMessage[] satelliteMessages;

    @JsonCreator
    public RequestObject(@JsonProperty("satellites") SatelliteMessage[] satelliteMessages){
        Set<String> nameSet = new HashSet<>();

        if(satelliteMessages != null && satelliteMessages.length == 3){
            Arrays.stream(satelliteMessages).forEach((satellite)-> {nameSet.add(satellite.getName());});
            if(nameSet.size() == 3){
                this.satelliteMessages = satelliteMessages;
            }
            else throw new JsonParseException();
        }
        else throw new JsonParseException();
    }

    public SatelliteMessage[] getSatelliteMessages() {
        return satelliteMessages;
    }

    public void setSatelliteMessages(SatelliteMessage[] satelliteMessages) {
        Set<String> nameSet = new HashSet<>();

        if(satelliteMessages != null && (satelliteMessages.length == 3)){
            Arrays.stream(satelliteMessages).forEach((satellite)-> {nameSet.add(satellite.getName());});
            if(nameSet.size() == 3){
                this.satelliteMessages = satelliteMessages;
            }
            else throw new JsonParseException();
        }
        else throw new JsonParseException();
    }
}
