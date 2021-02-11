package cloudcode.objects;

public class ResponseObject {
    private Location position;
    private String message;

    public ResponseObject(Location position, String message){
        this.position = position;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public Location getPosition() {
        return position;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPosition(Location position) {
        this.position = position;
    }
}
