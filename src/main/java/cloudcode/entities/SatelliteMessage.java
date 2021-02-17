package cloudcode.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.boot.json.JsonParseException;

public class SatelliteMessage {

    private Double distance;
    private String[] message;
    private String name;
    private Location satelliteLocation;

    @JsonCreator
    public SatelliteMessage(@JsonProperty("message") String[] message, @JsonProperty("distance") Double distance, @JsonProperty("name") String name){

        if(message != null && distance != null && name != null && message.length > 0 && EnumUtils.isValidEnum(SatelliteConstants.satelliteName.class,name.toUpperCase())){
            this.name = name.toLowerCase();
            this.distance = distance;
            this.message = message;
            this.satelliteLocation = SatelliteConstants.location.valueOf(name.toUpperCase()).getLocation();
        }
        else throw new JsonParseException();
    }

    public void setMessage(String[] message) {
        if(message != null && message.length > 0){
            this.message = message;
        }
        else throw new JsonParseException();
    }

    public void setDistance(Double distance) {
        if(distance != null){
            this.distance = distance;
        }
        else throw new JsonParseException();
    }

    public void setName(String name) {
        if(name != null && EnumUtils.isValidEnum(SatelliteConstants.satelliteName.class,name.toUpperCase())){
            this.name = name.toLowerCase();
            this.satelliteLocation = SatelliteConstants.location.valueOf(name.toUpperCase()).getLocation();
        }
        else throw new JsonParseException();
    }

    public String getName() {
        return name;
    }

    public Location getSatelliteLocation() {
        return satelliteLocation;
    }

    public String[] getMessage() {
        return message;
    }

    public Double getDistance() {
        return distance;
    }

}
