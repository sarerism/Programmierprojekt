package io;

import graph.Graph;
import graph.Node;
import graph.Edge;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Reads graph data from .fmi files and constructs a Graph object
 * 
 * File format:
 * - Node lines: <node_id> <osm_id> <lat> <lon> 0
 * - Edge lines: <source_id> <target_id> <length_cm> <edge_type>
 */

public class GraphReader {
    
    /**
     * Reads a graph from the specified .fmi file
     * 
     * @param filename Path to the .fmi file
     * @return Constructed Graph object with nodes and edges
     * @throws IOException for 404 
     */

    public static Graph readGraph(String filename) throws IOException {
        System.out.println("Reading graph file: " + filename);
        long startTime = System.currentTimeMillis();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    break;
                }
            }
            
            int nodeCount = Integer.parseInt(line.trim());
            
            
            line = reader.readLine().trim();
            int edgeCount = Integer.parseInt(line);
            
            System.out.println("  Nodes: " + nodeCount);
            System.out.println("  Edges: " + edgeCount);
            
            
            Graph graph = new Graph(nodeCount, edgeCount);
            
            
            for (int i = 0; i < nodeCount; i++) {
                line = reader.readLine();
                String[] parts = line.trim().split("\\s+");
                
                int nodeId = Integer.parseInt(parts[0]);
            
                double latitude = Double.parseDouble(parts[2]);
                double longitude = Double.parseDouble(parts[3]);
            
                
                Node node = new Node(nodeId, latitude, longitude);
                graph.setNode(nodeId, node);
                
                if (i % 100000 == 0 && i > 0) {
                    System.out.println("  Read " + i + " nodes...");
                }
            }
            
            
            int edgeIndex = 0;
            int currentSourceNode = -1;
            
            for (int i = 0; i < edgeCount; i++) {
                line = reader.readLine();
                String[] parts = line.trim().split("\\s+");
                
                int sourceId = Integer.parseInt(parts[0]);
                int targetId = Integer.parseInt(parts[1]);
                int distanceCm = Integer.parseInt(parts[2]);
            
                

                if (sourceId != currentSourceNode) {

                    for (int nodeId = currentSourceNode + 1; nodeId <= sourceId; nodeId++) {
                        graph.setEdgeOffset(nodeId, edgeIndex);
                    }
                    currentSourceNode = sourceId;
                }
                


                Edge edge = new Edge(targetId, distanceCm, 0);
                graph.setEdge(edgeIndex, edge);
                edgeIndex++;
                
                if (i % 500000 == 0 && i > 0) {
                    System.out.println("  Read " + i + " edges...");
                }
            }
            

            for (int nodeId = currentSourceNode + 1; nodeId < nodeCount; nodeId++) {
                graph.setEdgeOffset(nodeId, edgeIndex);
            }
            

            graph.finalizeGraph();
            
            long endTime = System.currentTimeMillis();
            System.out.println("Graph reading completed in " + (endTime - startTime) + "ms");
            System.out.println("Memory usage: " + (graph.estimateMemoryUsage() / (1024 * 1024)) + " MB");
            
            return graph;
        }
    }
    
    /**
     * Updates edge elevation gains after node elevations have been set
     * 
     * @param graph The graph with node elevations already set
     */

    public static void updateEdgeElevations(Graph graph) {
        System.out.println("Updating edge elevation gains...");
        long startTime = System.currentTimeMillis();
        
        Edge[] edges = graph.getEdgeArray();
        
        for (int nodeId = 0; nodeId < graph.getNodeCount(); nodeId++) {
            Node sourceNode = graph.getNode(nodeId);
            int start = graph.getEdgeStart(nodeId);
            int end = graph.getEdgeEnd(nodeId);
            
            for (int i = start; i < end; i++) {
                Edge oldEdge = edges[i];
                Node targetNode = graph.getNode(oldEdge.getTargetNodeId());
                
                
                int elevationDiff = targetNode.getElevation() - sourceNode.getElevation();
                int elevationGain = Math.max(0, elevationDiff);
                
                Edge newEdge = new Edge(
                    oldEdge.getTargetNodeId(),
                    oldEdge.getDistance(),
                    elevationGain
                );
                
                graph.setEdge(i, newEdge);
            }
            
            if (nodeId % 100000 == 0 && nodeId > 0) {
                System.out.println("  Updated edges for " + nodeId + " nodes...");
            }
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("Edge elevation update completed in " + (endTime - startTime) + "ms");
    }
}