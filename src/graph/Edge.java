package graph;

/**
 * Represents an edge in the bicycle route graph
 * 
 * Each edge stores:
 * - Target node ID
 * - Distance in centi
 * - Absolute elevation change in centi
 */

public class Edge { 
    private final int targetNodeId;
    private final int distance;
    private final int elevationGain; 

    public Edge(int targetNodeId, int distance, int elevationGain) {
        this.targetNodeId = targetNodeId;
        this.distance = distance;
        this.elevationGain = elevationGain;
    }

    public int getTargetNodeId() {
        return targetNodeId;
    }

    public int getDistance() {
        return distance;
    }

    public int getElevationGain() {
        return elevationGain;
    }

    /**
     * Calculates the weighted cost of this edge.
     * 
     * @param weight Value between 0 and 1
     *               1.0 = optimize for distance only
     *               0.0 = optimize for elevation only
     * @return Weighted cost in centimeters
     */

    public int getCost(double weight) {
        return (int) Math.round(weight * distance + (1.0 - weight) * elevationGain);
    }

    @Override
    public String toString() {
        return String.format("Edge[target=%d, dist=%dcm, elevGain=%dcm]", 
                           targetNodeId, distance, elevationGain);
    }
}
