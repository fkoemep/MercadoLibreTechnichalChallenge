package cloudcode.entities;

public class SatelliteConstants {
    public enum satelliteName {KENOBI, SKYWALKER, SATO}

    public enum location {
        KENOBI(new Location(-500.00, -200.00)), SKYWALKER(new Location(100.00,-100.00)), SATO(new Location(500.00,100.00));

        private Location location;

        location(Location location) {
            this.location = location;
        }
        public Location getLocation() {
            return this.location;
        }
    }
}
