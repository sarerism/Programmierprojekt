import graph.Graph;
import io.GraphReader;
import io.ElevationReader;
import routing.Dijkstra;
import routing.NodeFinder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

public class Benchmark {

    public static void main(String[] args) {
        if (args.length < 10) {
            System.out.println("Usage: java Benchmark -graph <path> -lon <lon> -lat <lat> -que <path> -s <sourceId>");
            return;
        }
        
        String graphPath = args[1];
        double lon = Double.parseDouble(args[3]);
        double lat = Double.parseDouble(args[5]);
        String quePath = args[7];
        int sourceNodeId = Integer.parseInt(args[9]);

        System.out.println("Reading graph file and creating graph data structure (" + graphPath + ")");
        long graphReadStart = System.currentTimeMillis();
        
        Graph graph = null;
        try {


            graph = GraphReader.readGraph(graphPath);
            
            
            File graphFile = new File(graphPath);
            String graphDir = graphFile.getParent();
            if (graphDir == null) {
                graphDir = ".";
            }
            String srtmDir = graphDir + File.separator + "srtm";
            
            System.out.println("Reading elevation data from: " + srtmDir);
            ElevationReader elevationReader = new ElevationReader(srtmDir);
            
            
            System.out.println("Computing node elevations...");
            long elevStart = System.currentTimeMillis();
            for (int i = 0; i < graph.getNodeCount(); i++) {
                graph.Node node = graph.getNode(i);
                int elevation = elevationReader.getElevationCm(
                    node.getLatitude(),
                    node.getLongitude()
                );
                node.setElevation(elevation);
                
                if (i % 100000 == 0 && i > 0) {
                    System.out.println("  Processed " + i + " nodes...");
                }
            }
            long elevEnd = System.currentTimeMillis();
            System.out.println("Elevation processing completed in " + (elevEnd - elevStart) + "ms");
            System.out.println("Cached " + elevationReader.getCacheSize() + " elevation files");
            
            
            GraphReader.updateEdgeElevations(graph);
            
        } catch (Exception e) {
            System.out.println("Error reading graph: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        long graphReadEnd = System.currentTimeMillis();
        System.out.println("\tgraph read took " + (graphReadEnd - graphReadStart) + "ms");

        System.out.println("Finding closest node to coordinates " + lon + " " + lat);
        long nodeFindStart = System.currentTimeMillis();
        double[] coords = {0.0, 0.0};
        
        NodeFinder nodeFinder = new NodeFinder(graph);
        coords = nodeFinder.findNearestNodeCoordinates(lat, lon);

        long nodeFindEnd = System.currentTimeMillis();
        System.out.println("\tfinding node took " + (nodeFindEnd - nodeFindStart) + "ms: " + coords[0] + ", " + coords[1]);

        System.out.println("Running one-to-one Dijkstras for queries in .que file " + quePath);
        long queStart = System.currentTimeMillis();
        
        Dijkstra dijkstra = new Dijkstra(graph);
        
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(quePath))) {
            String currLine;
            while ((currLine = bufferedReader.readLine()) != null) {
                // Skip empty lines and comments
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
        } catch (Exception e) {
            System.out.println("Exception...");
            e.printStackTrace();
        }
        long queEnd = System.currentTimeMillis();
        System.out.println("\tprocessing .que file took " + (queEnd - queStart) + "ms");

        System.out.println("Running one-to-all Dijkstra from node " + sourceNodeId);
        long oneToAllStart = System.currentTimeMillis();
        
        dijkstra.oneToAll(sourceNodeId, 0.5);
        
        long oneToAllEnd = System.currentTimeMillis();
        System.out.println("\tone-to-all Dijkstra took " + (oneToAllEnd - oneToAllStart) + "ms");
        
        System.out.println("\nBenchmark completed successfully!");
    }
}
