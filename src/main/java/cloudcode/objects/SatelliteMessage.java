package cloudcode.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.boot.json.JsonParseException;

public class SatelliteMessage {

    private Double distance;
    private String[] message;
    private String name;
    private Location satelliteLocation;

    private enum location {
        KENOBI(new Location(-500.00, -200.00)), SKYWALKER(new Location(100.00,-100.00)), SATO(new Location(500.00,100.00));

        private Location location;

        location(Location location) {
            this.location = location;
        }
        public Location getLocation() {
            return this.location;
        }
    }

    private enum satelliteName {KENOBI, SKYWALKER, SATO}


    @JsonCreator
    public SatelliteMessage(@JsonProperty("message") String[] message, @JsonProperty("distance") Double distance, @JsonProperty("name") String name){

        if(message != null && distance != null && name != null && message.length > 0 && EnumUtils.isValidEnum(satelliteName.class,name.toUpperCase())){
            this.name = name;
            this.distance = distance;
            this.message = message;
            this.satelliteLocation = location.valueOf(name.toUpperCase()).getLocation();
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
        if(name != null && EnumUtils.isValidEnum(satelliteName.class,name.toUpperCase())){
            this.name = name;
            this.satelliteLocation = location.valueOf(name.toUpperCase()).getLocation();
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
