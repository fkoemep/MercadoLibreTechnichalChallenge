package cloudcode;

import cloudcode.entities.Location;
import cloudcode.entities.SatelliteConstants;
import cloudcode.exceptions.LocationProcessingException;
import cloudcode.exceptions.MessageProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.IntStream;

public class BasicFunctions {

    private static final Logger logger = LoggerFactory.getLogger(BasicFunctions.class);
    private static final double EPSILON = 0.000001;

    public static String[] GetMessage(List<String[]> messages) throws MessageProcessingException {

        //Getting the message lenght, we ignore safety checks since we've previously validated the data
        Integer maxLenght = messages.stream().map(strings -> IntStream.range(0, strings.length - 1).map(i -> strings.length - 1 - i)
                            .filter(i -> !strings[i].trim().isEmpty()).findFirst().orElse(-1)).max(Integer::compare).get();

        List<String> message = new ArrayList<String>();
        for(int i = maxLenght - 1; i >= 0; i--){
            Set<String> temp = new HashSet<String>();
            temp.add(messages.get(0)[messages.get(0).length - 1 - i].trim());
            temp.add(messages.get(1)[messages.get(1).length - 1 - i].trim());
            temp.add(messages.get(2)[messages.get(2).length - 1 - i].trim());

            //Two or more different messages in the same position, we add a blank space
            if (temp.size() == 3){
                message.add("");
            }
            else{
                temp.remove("");
                message.add(temp.iterator().next());
            }
        }

        if(message.size() == 0){
            throw new MessageProcessingException("");
        }

        return Arrays.copyOf(message.toArray(), message.toArray().length, String[].class);
    }

    public static Location GetLocation(Double[] distances) throws LocationProcessingException {

        Location position = calculateThreeCircleIntersection(distances[0], distances[1], distances[2]);

        if (position == null) {
            throw new LocationProcessingException("");
        }

        return position;
    }

    //Do not change the parameter positions unless you prove mathematically that doing so does not change the result. The array in the caller function requires this order.
    private static Location calculateThreeCircleIntersection(double rKenobi, double rSato, double rSkywalker) {
        double xKenobi = SatelliteConstants.location.KENOBI.getLocation().getX();
        double yKenobi = SatelliteConstants.location.KENOBI.getLocation().getY();
        double xSato = SatelliteConstants.location.SATO.getLocation().getX();
        double ySato = SatelliteConstants.location.SATO.getLocation().getY();
        double xSkywalker = SatelliteConstants.location.SKYWALKER.getLocation().getX();
        double ySkywalker = SatelliteConstants.location.SKYWALKER.getLocation().getY();

        double a, dx, dy, d, h, rx, ry;
        double point2_x, point2_y;

        /* dx and dy are the vertical and horizontal distances between
         * the circle centers.
         */
        dx = xSato - xKenobi;
        dy = ySato - yKenobi;

        /* Determine the straight-line distance between the centers. */
        d = Math.sqrt((dy * dy) + (dx * dx));

        /* Check for solvability. */
        if (d > (rKenobi + rSato)) {
            /* no solution. circles do not intersect. */
            return null;
        }
        if (d < Math.abs(rKenobi - rSato)) {
            /* no solution. one circle is contained in the other */
            return null;
        }

        /* 'point 2' is the point where the line through the circle
         * intersection points crosses the line between the circle
         * centers.
         */

        /* Determine the distance from point 0 to point 2. */
        a = ((rKenobi * rKenobi) - (rSato * rSato) + (d * d)) / (2.0 * d);

        /* Determine the coordinates of point 2. */
        point2_x = xKenobi + (dx * a / d);
        point2_y = yKenobi + (dy * a / d);

        /* Determine the distance from point 2 to either of the
         * intersection points.
         */
        h = Math.sqrt((rKenobi * rKenobi) - (a * a));

        /* Now determine the offsets of the intersection points from
         * point 2.
         */
        rx = -dy * (h / d);
        ry = dx * (h / d);

        /* Determine the absolute intersection points. */
        double intersectionPoint1_x = point2_x + rx;
        double intersectionPoint2_x = point2_x - rx;
        double intersectionPoint1_y = point2_y + ry;
        double intersectionPoint2_y = point2_y - ry;

        logger.info("INTERSECTION Circle1 AND Circle2: " + "(" + intersectionPoint1_x + "," + intersectionPoint1_y + ")" + " AND (" + intersectionPoint2_x + "," + intersectionPoint2_y + ")");

        /* Lets determine if circle 3 intersects at either of the above intersection points. */
        dx = intersectionPoint1_x - xSkywalker;
        dy = intersectionPoint1_y - ySkywalker;
        double d1 = Math.sqrt((dy * dy) + (dx * dx));

        dx = intersectionPoint2_x - xSkywalker;
        dy = intersectionPoint2_y - ySkywalker;
        double d2 = Math.sqrt((dy * dy) + (dx * dx));

        if (Math.abs(d1 - rSkywalker) < EPSILON) {
            return new Location(intersectionPoint1_x, intersectionPoint1_y);
//            logger.info("INTERSECTION Circle1 AND Circle2 AND Circle3:", "(" + intersectionPoint1_x + "," + intersectionPoint1_y + ")");
        } else if (Math.abs(d2 - rSkywalker) < EPSILON) {
            return new Location(intersectionPoint2_x, intersectionPoint2_y);
//            logger.info("INTERSECTION Circle1 AND Circle2 AND Circle3:", "(" + intersectionPoint2_x + "," + intersectionPoint2_y + ")"); //here was an error
        } else {
            return null;
//            logger.info("INTERSECTION Circle1 AND Circle2 AND Circle3:", "NONE");
        }
    }

}