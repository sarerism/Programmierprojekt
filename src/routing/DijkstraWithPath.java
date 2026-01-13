package routing;

import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import routing.RouteService.RouteResult;

/**
 * Extended Dijkstra implementation with path reconstruction
 * Computes shortest paths and tracks predecessors to reconstruct routes
 */
public class DijkstraWithPath {
    
    private final Graph graph;
    private long[] distances;
    private int[] predecessors;
    private boolean[] visited;
    
    /**
     * Entry in the priority queue for Dijkstra algorithm
     */
    private static class DijkstraEntry implements Comparable<DijkstraEntry> {
        final int nodeId;
        final long distance;
        
        DijkstraEntry(int nodeId, long distance) {
            this.nodeId = nodeId;
            this.distance = distance;
        }
        
        @Override
        public int compareTo(DijkstraEntry other) {
            return Long.compare(this.distance, other.distance);
        }
    }
    
    public DijkstraWithPath(Graph graph) {
        this.graph = graph;
        this.distances = new long[graph.getNodeCount()];
        this.predecessors = new int[graph.getNodeCount()];
        this.visited = new boolean[graph.getNodeCount()];
    }
    
    /**
     * Computes route from source to target and reconstructs the path
     * Also calculates total distance and elevation gain along the path
     * 
     * @param sourceId Source node ID
     * @param targetId Target node ID
     * @param weight Weight between 0.0 (elevation priority) and 1.0 (distance priority)
     * @return RouteResult with path and metrics, or null if no route exists
     */
    public RouteResult computeRoute(int sourceId, int targetId, double weight) {
        boolean found = runDijkstra(sourceId, targetId, weight);
        
        if (!found) {
            return null;
        }

        List<Integer> path = reconstructPath(sourceId, targetId);
        
        long totalDistance = 0;
        long totalElevationGain = 0;
        
        for (int i = 0; i < path.size() - 1; i++) {
            int currentNode = path.get(i);
            int nextNode = path.get(i + 1);
            
            Edge edge = findEdge(currentNode, nextNode);
            
            if (edge != null) {
                totalDistance += edge.getDistance();
                totalElevationGain += edge.getElevationGain();
            }
        }
        
        return new RouteResult(path, totalDistance, totalElevationGain);
    }
    
    /**
     * Runs Dijkstra's algorithm from source to target
     * 
     * @return true if target was reached, false otherwise
     */
    private boolean runDijkstra(int sourceId, int targetId, double weight) {
        Arrays.fill(distances, Long.MAX_VALUE);
        Arrays.fill(predecessors, -1);
        Arrays.fill(visited, false);
        
        distances[sourceId] = 0;
        
        PriorityQueue<DijkstraEntry> pq = new PriorityQueue<>();
        pq.add(new DijkstraEntry(sourceId, 0));
        
        while (!pq.isEmpty()) {
            DijkstraEntry current = pq.poll();
            int currentNode = current.nodeId;
            
            if (currentNode == targetId) {
                return true;
            }
            
            if (visited[currentNode]) {
                continue;
            }
            
            visited[currentNode] = true;
            

            Edge[] edges = graph.getEdgeArray();
            int edgeStart = graph.getEdgeStart(currentNode);
            int edgeEnd = graph.getEdgeEnd(currentNode);
            
            for (int i = edgeStart; i < edgeEnd; i++) {
                Edge edge = edges[i];
                int neighbor = edge.getTargetNodeId();
                
                if (visited[neighbor]) {
                    continue;
                }
                
                long edgeCost = edge.getCost(weight);
                long newDistance = distances[currentNode] + edgeCost;
                
                if (newDistance < distances[neighbor]) {
                    distances[neighbor] = newDistance;
                    predecessors[neighbor] = currentNode;
                    pq.add(new DijkstraEntry(neighbor, newDistance));
                }
            }
        }
        
        return false; 
    }
    
    /**
     * Reconstructs the path from source to target using predecessors
     */
    private List<Integer> reconstructPath(int sourceId, int targetId) {
        List<Integer> path = new ArrayList<>();
        int current = targetId;
        
        while (current != -1) {
            path.add(current);
            current = predecessors[current];
        }
        
        Collections.reverse(path);
        return path;
    }
    
    /**
     * Finds the edge from source to target node
     */
    private Edge findEdge(int sourceNode, int targetNode) {
        Edge[] edges = graph.getEdgeArray();
        int edgeStart = graph.getEdgeStart(sourceNode);
        int edgeEnd = graph.getEdgeEnd(sourceNode);
        
        for (int i = edgeStart; i < edgeEnd; i++) {
            Edge edge = edges[i];
            if (edge.getTargetNodeId() == targetNode) {
                return edge;
            }
        }
        
        return null;
    }
}
