package graph;

/**
 * Represents a node in the bicycle route graph
 * 
 * Each node stores:
 * - Geographic coordinates (latitude, longitude)
 * - Elevation in centimeters (computed from SRTM data)
 */

public class Node {
    private final int id;
    private final double latitude;
    private final double longitude;
    private int elevation;

    public Node(int id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = 0;
    }

    public int getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getElevation() {
        return elevation;
    }

    public void setElevation(int elevation) {
        this.elevation = elevation;
    }

    @Override
    public String toString() {
        return String.format("Node[id=%d, lat=%.6f, lon=%.6f, elev=%dcm]", 
                           id, latitude, longitude, elevation);
    }
}
