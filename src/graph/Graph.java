package graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Adjacency Array representation of the bicycle route graph
 * 
 * This is a space efficient representation that:
 * - Stores all nodes in a single array
 * - Stores all edges in a single array
 * - Uses offset arrays to quickly access edges for every node
 */

public class Graph {
    private Node[] nodes;
    private Edge[] edges;
    private int[] edgeOffsets;

    private int nodeCount;
    private int edgeCount;

    /**
     * Creates an empty graph
     * 
     * @param nodeCount Number of nodes
     * @param edgeCount Number of edges
     */

    public Graph(int nodeCount, int edgeCount) {
        this.nodeCount = nodeCount;
        this.edgeCount = edgeCount;
        this.nodes = new Node[nodeCount];
        this.edges = new Edge[edgeCount];
        this.edgeOffsets = new int[nodeCount + 1];
    }

    /**
     * Sets a node at the specified index
     */

    public void setNode(int index, Node node) {
        nodes[index] = node;
    }

    /**
     * Gets a node by its index
     */

    public Node getNode(int index) {
        return nodes[index];
    }

    /**
     * Gets all nodes
     */

    public Node[] getNodes() {
        return nodes;
    }

    /**
     * Sets an edge at the specified index
     */

    public void setEdge(int index, Edge edge) {
        edges[index] = edge;
    }

    /**
     * Sets the edge offset for a node
     */

    public void setEdgeOffset(int nodeIndex, int offset) {
        edgeOffsets[nodeIndex] = offset;
    }

    /**
     * Finalizes the graph after all nodes and edges have been added
     * Sets the final offset marker
     */

    public void finalizeGraph() {
        edgeOffsets[nodeCount] = edgeCount;
    }

    /**
     * Gets all outgoing edges from a specific node
     * 
     * @param nodeId Node ID
     * @return List of outgoing edges
     */

    public List<Edge> getOutgoingEdges(int nodeId) {
        List<Edge> outgoing = new ArrayList<>();
        int start = edgeOffsets[nodeId];
        int end = edgeOffsets[nodeId + 1];
        
        for (int i = start; i < end; i++) {
            outgoing.add(edges[i]);
        }
        
        return outgoing;
    }

    /**
     * Gets the number of outgoing edges from a node
     */

    public int getOutgoingEdgeCount(int nodeId) {
        return edgeOffsets[nodeId + 1] - edgeOffsets[nodeId];
    }

    /**
     * Gets the direct edge array and offset for efficient iteration
     * Used by Dijkstra for performance :)
     */

    public Edge[] getEdgeArray() {
        return edges;
    }

    public int getEdgeStart(int nodeId) {
        return edgeOffsets[nodeId];
    }

    public int getEdgeEnd(int nodeId) {
        return edgeOffsets[nodeId + 1];
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public int getEdgeCount() {
        return edgeCount;
    }

    /**
     * Returns memory usage estimate in bytes
     */

    public long estimateMemoryUsage() {
        long nodeMemory = (long) nodeCount * (4 + 8 + 8 + 4); 
        long edgeMemory = (long) edgeCount * (4 + 4 + 4);
        long offsetMemory = (long) (nodeCount + 1) * 4;
        return nodeMemory + edgeMemory + offsetMemory;
    }

    @Override
    public String toString() {
        return String.format("Graph[nodes=%d, edges=%d, memory≈%.1fMB]", 
                           nodeCount, edgeCount, estimateMemoryUsage() / (1024.0 * 1024.0));
    }
}
