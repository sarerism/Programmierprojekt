package routing;

import graph.Graph;
import graph.Edge;

import java.util.Arrays;
import java.util.PriorityQueue;

/**
 * Implements Dijkstra's shortest path algorithm
 * 
 * Supports two modes:
 * - One-to-one: Find shortest path from source to specific target
 * - One-to-all: Find shortest paths from source to all nodes
 */

public class Dijkstra {
    
    private final Graph graph;
    private long[] distances;
    private boolean[] visited;
    
    /**
     * Entry in the priority queue for Dijkstra algorithm
     * Contains node ID and distance to that node
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
    
    /**
     * Creates a Dijkstra instance for the given graph
     * 
     * @param graph The graph to perform searches on
     */

    public Dijkstra(Graph graph) {
        this.graph = graph;
        this.distances = new long[graph.getNodeCount()];
        this.visited = new boolean[graph.getNodeCount()];
    }
    
    /**
     * Computes shortest path from source to target node
     * Stops as soon as target is reached (one-to-one)
     * 
     * @param sourceId Source node ID
     * @param targetId Target node ID
     * @param weight Weight between 0 and 1 (1.0 = distance only, 0.0 = elevation only)
     * @return Shortest distance from source to target, or -1 if unreachable
     */

    public long oneToOne(int sourceId, int targetId, double weight) {

        Arrays.fill(distances, Long.MAX_VALUE);
        Arrays.fill(visited, false);
        
        distances[sourceId] = 0;
        
        PriorityQueue<DijkstraEntry> pq = new PriorityQueue<>();
        pq.add(new DijkstraEntry(sourceId, 0));
        
        while (!pq.isEmpty()) {
            DijkstraEntry current = pq.poll();
            int currentNode = current.nodeId;
            

            if (currentNode == targetId) {
                return distances[targetId];
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
                    pq.add(new DijkstraEntry(neighbor, newDistance));
                }
            }
        }
        
        // Target not reachable
        return -1;
    }
    
    /**
     * Computes shortest paths from source to all nodes (one-to-all)
     * Does not stop early explores the entire graph
     * 
     * @param sourceId Source node ID
     * @param weight Weight between 0 and 1 (1.0 = distance only, 0.0 = elevation only)
     */

    public void oneToAll(int sourceId, double weight) {
        Arrays.fill(distances, Long.MAX_VALUE);
        Arrays.fill(visited, false);
        
        distances[sourceId] = 0;
        
        PriorityQueue<DijkstraEntry> pq = new PriorityQueue<>();
        pq.add(new DijkstraEntry(sourceId, 0));
        
        while (!pq.isEmpty()) {
            DijkstraEntry current = pq.poll();
            int currentNode = current.nodeId;
            
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
                    pq.add(new DijkstraEntry(neighbor, newDistance));
                }
            }
        }
    }
    
    /**
     * Gets the computed distance to a specific node
     * Must be called after oneToAll() has been executed
     * 
     * @param nodeId Node ID to query
     * @return Distance to node, or Long.MAX_VALUE if unreachable
     */

    public long getDistance(int nodeId) {
        return distances[nodeId];
    }
    
    /**
     * Gets all computed distances
     * Must be called after oneToAll() has been executed
     * 
     * @return Array of distances to all nodes
     */
    
    public long[] getAllDistances() {
        return distances;
    }
}
