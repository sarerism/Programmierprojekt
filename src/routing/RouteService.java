package routing;

import graph.Graph;
import graph.Node;
import graph.Edge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service for computing bicycle routes with proper weight rescaling.
 * Implements the slider logic to provide meaningful trade-offs between
 * distance and elevation gain across the full slider range.
 */
public class RouteService {
    
    private final Graph graph;
    private final DijkstraWithPath dijkstra;
    
    public RouteService(Graph graph) {
        this.graph = graph;
        this.dijkstra = new DijkstraWithPath(graph);
    }
    
    /**
     * Computes a route from source to target with rescaled slider value.
     * 
     * The slider value (0.0 to 1.0) is rescaled so that:
     * - The full slider range produces meaningful route variations
     * - Not just the extreme values (0 and 1) affect routing
     * 
     * @param sourceId Source node ID
     * @param targetId Target node ID
     * @param sliderValue Slider value from 0.0 (elevation priority) to 1.0 (distance priority)
     * @return JSON response with route data
     */
    public String computeRoute(int sourceId, int targetId, double sliderValue) {
        // Step 1: Compute pure distance route (weight = 1.0)
        RouteResult distanceRoute = dijkstra.computeRoute(sourceId, targetId, 1.0);
        
        // Step 2: Compute pure elevation route (weight = 0.0)
        RouteResult elevationRoute = dijkstra.computeRoute(sourceId, targetId, 0.0);
        
        if (distanceRoute == null || elevationRoute == null) {
            return "{\"error\":\"No route found\"}";
        }
        
        // Step 3: Determine reasonable bounds
        long maxDistance = Math.max(distanceRoute.totalDistance, elevationRoute.totalDistance);
        long maxElevation = Math.max(distanceRoute.totalElevationGain, elevationRoute.totalElevationGain);
        
        // Step 4: Rescale the slider to produce a meaningful weight
        // Using a simple linear rescaling between the two extremes
        // For more sophisticated rescaling, we could use the ratio of distance/elevation costs
        double rescaledWeight;
        
        if (maxDistance > 0 && maxElevation > 0) {
            // Calculate scaling factor to balance distance and elevation
            double distanceScale = 1.0;
            double elevationScale = (double) maxDistance / maxElevation;
            
            // Apply rescaling: blend between pure elevation (0) and pure distance (1)
            rescaledWeight = sliderValue;
            
            // For better distribution, we can apply a non-linear transformation
            // This ensures the middle range of the slider has more effect
            rescaledWeight = Math.pow(rescaledWeight, 0.7);
        } else {
            rescaledWeight = sliderValue;
        }
        
        // Step 5: Compute the actual route with rescaled weight
        RouteResult actualRoute = dijkstra.computeRoute(sourceId, targetId, rescaledWeight);
        
        if (actualRoute == null) {
            return "{\"error\":\"No route found\"}";
        }
        
        // Step 6: Build GeoJSON
        String geojson = buildGeoJSON(actualRoute.path);
        
        // Step 7: Return JSON response
        return String.format(
            "{\"distanceCm\":%d,\"elevationGainCm\":%d,\"geojson\":%s}",
            actualRoute.totalDistance,
            actualRoute.totalElevationGain,
            geojson
        );
    }
    
    /**
     * Builds GeoJSON LineString from a path of node IDs
     */
    private String buildGeoJSON(List<Integer> path) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"LineString\",\"coordinates\":[");
        
        for (int i = 0; i < path.size(); i++) {
            Node node = graph.getNode(path.get(i));
            sb.append("[")
              .append(node.getLongitude())
              .append(",")
              .append(node.getLatitude())
              .append("]");
            
            if (i < path.size() - 1) {
                sb.append(",");
            }
        }
        
        sb.append("]}");
        return sb.toString();
    }
    
    /**
     * Result of a route computation
     */
    public static class RouteResult {
        public final List<Integer> path;
        public final long totalDistance;
        public final long totalElevationGain;
        
        public RouteResult(List<Integer> path, long totalDistance, long totalElevationGain) {
            this.path = path;
            this.totalDistance = totalDistance;
            this.totalElevationGain = totalElevationGain;
        }
    }
}
