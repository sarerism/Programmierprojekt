package routing;

import graph.Graph;
import graph.Node;

/**
 * Finds the nearest graph node to a given geographic coordinate.
 */
public class NodeFinder {
    
    private final Graph graph;
    
    /**
     * Creates a NodeFinder for the given graph.
     * 
     * @param graph The graph to search
     */
    public NodeFinder(Graph graph) {
        this.graph = graph;
    }
    
    /**
     * Finds the nearest node to the given coordinates.
     * Uses naive O(n) linear search with Euclidean distance.
     * 
     * @param latitude Target latitude
     * @param longitude Target longitude
     * @return ID of nearest node
     */
    public int findNearestNode(double latitude, double longitude) {
        int nearestNodeId = -1;
        double minDistance = Double.MAX_VALUE;
        
        Node[] nodes = graph.getNodes();
        
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            
            double distance = calculateDistance(
                latitude, longitude,
                node.getLatitude(), node.getLongitude()
            );
            
            if (distance < minDistance) {
                minDistance = distance;
                nearestNodeId = i;
            }
        }
        
        return nearestNodeId;
    }
    
    /**
     * Finds nearest node and returns its coordinates.
     * 
     * @param latitude Target latitude
     * @param longitude Target longitude
     * @return Array [lat, lon] of nearest node
     */
    public double[] findNearestNodeCoordinates(double latitude, double longitude) {
        int nodeId = findNearestNode(latitude, longitude);
        
        if (nodeId == -1) {
            return new double[]{0.0, 0.0};
        }
        
        Node node = graph.getNode(nodeId);
        return new double[]{node.getLatitude(), node.getLongitude()};
    }
    
    /**
     * Calculates Euclidean distance between two points.
     * This is an approximation suitable for finding nearest nodes.
     * 
     * @param lat1 First latitude
     * @param lon1 First longitude
     * @param lat2 Second latitude
     * @param lon2 Second longitude
     * @return Euclidean distance (not in any specific unit, just for comparison)
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;
        return Math.sqrt(dLat * dLat + dLon * dLon);
    }
}
