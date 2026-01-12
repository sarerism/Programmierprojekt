import graph.Graph;
import graph.Node;
import io.GraphReader;
import io.ElevationReader;
import routing.Dijkstra;
import routing.NodeFinder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Benchmark {

    public static void main(String[] args) {
        Map<String, String> argMap = new HashMap<>();
        // Parse args as key-value pairs
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].startsWith("-")) {
                argMap.put(args[i], args[i + 1]);
                i++;
            }
        }

        // Extract arguments with defaults or null
        String graphPath = argMap.get("-graph");
        String quePath = argMap.get("-que");
        String srtmDir = argMap.get("-srtm");
        String latStr = argMap.get("-lat");
        String lonStr = argMap.get("-lon");
        String sStr = argMap.get("-s");
        String timingOnly = argMap.get("-timingOnly");

        if (graphPath == null) {
            System.err.println("Error: -graph argument is required");
            System.exit(1);
        }
        if (srtmDir == null) {
            // If not provided, try to guess srtm folder next to graph
            File graphFile = new File(graphPath);
            String graphDir = graphFile.getParent();
            if (graphDir == null) graphDir = ".";
            srtmDir = graphDir + File.separator + "srtm";
        }

        double lat = 0;
        double lon = 0;
        int s = 0;

        try {
            if (latStr != null) lat = Double.parseDouble(latStr);
            if (lonStr != null) lon = Double.parseDouble(lonStr);
            if (sStr != null) s = Integer.parseInt(sStr);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing numeric argument: " + e.getMessage());
            System.exit(1);
        }

        try {
            // Run phases depending on timingOnly flag
            if ("load".equals(timingOnly)) {
                loadGraphAndElevation(graphPath, srtmDir);
                return;
            } else if ("dijkstra".equals(timingOnly)) {
                if (quePath == null) {
                    System.err.println("Error: -que argument required for dijkstra timing");
                    System.exit(1);
                }
                runDijkstra(graphPath, srtmDir, quePath, s);
                return;
            } else if ("nearestNode".equals(timingOnly)) {
                runNearestNodeLookup(graphPath, srtmDir, lat, lon);
                return;
            }

            // Full benchmark run
            fullBenchmark(graphPath, srtmDir, lon, lat, quePath, s);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static Graph loadGraphAndElevation(String graphPath, String srtmDir) throws Exception {
        System.out.println("Reading graph file and creating graph data structure (" + graphPath + ")");
        long graphReadStart = System.currentTimeMillis();

        Graph graph = GraphReader.readGraph(graphPath);

        System.out.println("Reading elevation data from: " + srtmDir);
        long elevStart = System.currentTimeMillis();
        ElevationReader elevationReader = new ElevationReader(srtmDir);

        int totalNodes = graph.getNodeCount();
        int progressInterval = totalNodes / 20;
        if (progressInterval < 100000) progressInterval = 100000;

        for (int i = 0; i < totalNodes; i++) {
            Node node = graph.getNode(i);
            int elevation = elevationReader.getElevationCm(node.getLatitude(), node.getLongitude());
            node.setElevation(elevation);

            if (i > 0 && i % progressInterval == 0) {
                long elapsed = System.currentTimeMillis() - elevStart;
                int percent = (int) ((i * 100.0) / totalNodes);
                double rate = i / (elapsed / 1000.0);
                int remaining = (int) ((totalNodes - i) / rate);
                System.out.println(String.format("  Progress: %d%% (%d/%d nodes, %.0f nodes/sec, ~%d sec remaining)",
                        percent, i, totalNodes, rate, remaining));
            }
        }

        long elevEnd = System.currentTimeMillis();
        System.out.println("Elevation loading completed in " + (elevEnd - elevStart) + "ms");
        System.out.println("Cached SRTM files: " + elevationReader.getCacheSize());

        GraphReader.updateEdgeElevations(graph);

        long graphReadEnd = System.currentTimeMillis();
        System.out.println("\tgraph read took " + (graphReadEnd - graphReadStart) + "ms");

        return graph;
    }

    private static void runDijkstra(String graphPath, String srtmDir, String quePath, int s) throws Exception {
        Graph graph = loadGraphAndElevation(graphPath, srtmDir);

        System.out.println("Running one-to-one Dijkstras for queries in .que file " + quePath);
        long queStart = System.currentTimeMillis();

        Dijkstra dijkstra = new Dijkstra(graph);

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(quePath))) {
            String currLine;
            while ((currLine = bufferedReader.readLine()) != null) {
                currLine = currLine.trim();
                if (currLine.isEmpty() || currLine.startsWith("#")) {
                    continue;
                }

                String[] parts = currLine.split("\\s+");
                int oneToOneSourceNodeId = Integer.parseInt(parts[0]);
                int oneToOneTargetNodeId = Integer.parseInt(parts[1]);
                double oneToOneWeight = Double.parseDouble(parts[2]);

                long oneToOneDistance = dijkstra.oneToOne(
                        oneToOneSourceNodeId,
                        oneToOneTargetNodeId,
                        oneToOneWeight
                );

                System.out.println(oneToOneDistance);
            }
        }

        long queEnd = System.currentTimeMillis();
        System.out.println("\tprocessing .que file took " + (queEnd - queStart) + "ms");
    }

    private static void runNearestNodeLookup(String graphPath, String srtmDir, double lat, double lon) throws Exception {
        Graph graph = loadGraphAndElevation(graphPath, srtmDir);

        System.out.println("Finding closest node to coordinates " + lon + " " + lat);
        long nodeFindStart = System.currentTimeMillis();

        NodeFinder nodeFinder = new NodeFinder(graph);
        double[] coords = nodeFinder.findNearestNodeCoordinates(lat, lon);

        long nodeFindEnd = System.currentTimeMillis();
        System.out.println("\tfinding node took " + (nodeFindEnd - nodeFindStart) + "ms: " + coords[0] + ", " + coords[1]);
    }

    private static void fullBenchmark(String graphPath, String srtmDir, double lon, double lat, String quePath, int sourceNodeId) throws Exception {
        Graph graph = loadGraphAndElevation(graphPath, srtmDir);

        System.out.println("Finding closest node to coordinates " + lon + " " + lat);
        long nodeFindStart = System.currentTimeMillis();

        NodeFinder nodeFinder = new NodeFinder(graph);
        double[] coords = nodeFinder.findNearestNodeCoordinates(lat, lon);

        long nodeFindEnd = System.currentTimeMillis();
        System.out.println("\tfinding node took " + (nodeFindEnd - nodeFindStart) + "ms: " + coords[0] + ", " + coords[1]);

        if (quePath == null) {
            System.err.println("Error: -que argument is required for full benchmark");
            System.exit(1);
        }

        System.out.println("Running one-to-one Dijkstras for queries in .que file " + quePath);
        long queStart = System.currentTimeMillis();

        Dijkstra dijkstra = new Dijkstra(graph);

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(quePath))) {
            String currLine;
            while ((currLine = bufferedReader.readLine()) != null) {
                currLine = currLine.trim();
                if (currLine.isEmpty() || currLine.startsWith("#")) {
                    continue;
                }

                String[] parts = currLine.split("\\s+");
                int oneToOneSourceNodeId = Integer.parseInt(parts[0]);
                int oneToOneTargetNodeId = Integer.parseInt(parts[1]);
                double oneToOneWeight = Double.parseDouble(parts[2]);

                long oneToOneDistance = dijkstra.oneToOne(
                        oneToOneSourceNodeId,
                        oneToOneTargetNodeId,
                        oneToOneWeight
                );

                System.out.println(oneToOneDistance);
            }
        }

        long queEnd = System.currentTimeMillis();
        System.out.println("\tprocessing .que file took " + (queEnd - queStart) + "ms");

        System.out.println("Computing one-to-all Dijkstra from node id " + sourceNodeId);
        long oneToAllStart = System.currentTimeMillis();
        dijkstra.oneToAll(sourceNodeId, 1.0);
        long oneToAllEnd = System.currentTimeMillis();
        System.out.println("\tone-to-all Dijkstra took " + (oneToAllEnd - oneToAllStart) + "ms");

        System.out.print("Enter target node id... ");
        Scanner scanner = new Scanner(System.in);
        int targetNodeId = scanner.nextInt();
        System.out.println("Distance from " + sourceNodeId + " to " + targetNodeId + " is " + dijkstra.getDistance(targetNodeId));
        scanner.close();
    }
}
