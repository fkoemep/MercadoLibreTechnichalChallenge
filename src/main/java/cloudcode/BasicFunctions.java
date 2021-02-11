package cloudcode;

import cloudcode.objects.Location;

import java.util.List;

public class BasicFunctions {

    public static String GetMessage(List<String[]> messages){
        String message = "";

        return message;
    }

    public static Location GetLocation(List<Double> distances){
        Double x = 0.0;
        Double y = 1.1;

        Location position = new Location(x,y);
        return position;
    }
}