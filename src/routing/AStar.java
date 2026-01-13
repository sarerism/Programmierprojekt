package routing;

import graph.Edge;
import graph.Graph;
import graph.Node;
import java.util.Arrays;
import java.util.PriorityQueue;

/**
 * Implements A* (A-Star) Search algorithm
 * 
 * An extension of Dijkstra that uses a heuristic function to guide the search
 * towards the target, significantly reducing the search space.
 * 
 * f(n) = g(n) + h(n)
 * - g(n): Actual cost from source to n
 * - h(n): Heuristic estimate from n to target (Haversine distance)
 */
public class AStar {
    
    private final Graph graph;
    private final Node[] nodes;
    
    // Arrays to store search state
    private long[] gScores; // g(n): cost from source
    private long[] fScores; // f(n): g(n) + h(n)
    private boolean[] visited;
    
    // Statistics for benchmarking
    private int settledNodesCount = 0;
    
    /**
     * Entry in the priority queue for A* algorithm
     * Ordered by f-score (estimated total cost)
     */
    private static class AStarEntry implements Comparable<AStarEntry> {
        final int nodeId;
        final long fScore; // Estimated total cost
        
        AStarEntry(int nodeId, long fScore) {
            this.nodeId = nodeId;
            this.fScore = fScore;
        }
        
        @Override
        public int compareTo(AStarEntry other) {
            return Long.compare(this.fScore, other.fScore);
        }
    }
    
    public AStar(Graph graph) {
        this.graph = graph;
        this.nodes = graph.getNodes();
        int nodeCount = graph.getNodeCount();
        
        this.gScores = new long[nodeCount];
        this.fScores = new long[nodeCount];
        this.visited = new boolean[nodeCount];
    }
    
    /**
     * Computes shortest path using A* search
     * 
     * @param sourceId Source node ID
     * @param targetId Target node ID
     * @return Shortest distance in cm (or cost units), or -1 if unreachable
     */
    public long search(int sourceId, int targetId) {
        // Reset state
        Arrays.fill(gScores, Long.MAX_VALUE);
        Arrays.fill(fScores, Long.MAX_VALUE);
        Arrays.fill(visited, false);
        settledNodesCount = 0;
        
        // Initialize source
        gScores[sourceId] = 0;
        fScores[sourceId] = calculateHeuristic(sourceId, targetId);
        
        PriorityQueue<AStarEntry> pq = new PriorityQueue<>();
        pq.add(new AStarEntry(sourceId, fScores[sourceId]));
        
        Node targetNode = nodes[targetId];
        
        while (!pq.isEmpty()) {
            AStarEntry current = pq.poll();
            int u = current.nodeId;
            
            // If target reached, we define this as the shortest path found
            if (u == targetId) {
                return gScores[targetId];
            }
            
            // Skip if already settled (lazy deletion)
            if (visited[u]) {
                continue;
            }
            
            visited[u] = true;
            settledNodesCount++;
            
            // Explore neighbors
            Edge[] edges = graph.getEdgeArray();
            int edgeStart = graph.getEdgeStart(u);
            int edgeEnd = graph.getEdgeEnd(u);
            
            for (int i = edgeStart; i < edgeEnd; i++) {
                Edge edge = edges[i];
                int v = edge.getTargetNodeId();
                
                if (visited[v]) continue;
                
                // Calculate tentative g-score
                // Note: Currently using pure distance (cm) as weight
                // To support the slider, this cost calculation needs to match the slider logic
                long weight = edge.getDistance(); 
                long tentativeG = gScores[u] + weight;
                
                if (tentativeG < gScores[v]) {
                    gScores[v] = tentativeG;
                    long h = calculateHeuristic(v, targetId);
                    fScores[v] = tentativeG + h;
                    
                    // Add to PQ
                    // Note: Java PQ doesn't support decreaseKey, so we insert duplicate
                    pq.add(new AStarEntry(v, fScores[v]));
                }
            }
        }
        
        return -1; // No path found
    }
    
    /**
     * Calculates the heuristic (h-score) using Haversine distance
     * This provides the "crow-flies" distance in centimeters
     */
    private long calculateHeuristic(int uId, int targetId) {
        Node u = nodes[uId];
        Node target = nodes[targetId];
        
        return calculateHaversineDistance(
            u.getLatitude(), u.getLongitude(),
            target.getLatitude(), target.getLongitude()
        );
    }
    
    /**
     * Standard Haversine formula to calculate distance between two points on Earth
     * Returns distance in centimeters to match edge weights
     */
    private long calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 637100000; // Earth radius in centimeters
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
                
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return (long) (R * c);
    }
    
    public int getSettledNodesCount() {
        return settledNodesCount;
    }
}
