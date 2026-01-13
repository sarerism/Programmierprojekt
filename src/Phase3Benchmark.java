import graph.Graph;
import io.GraphReader;
import java.io.File;
import java.util.Random;
import routing.AStar;
import routing.Dijkstra;

public class Phase3Benchmark {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Phase3Benchmark -graph <path> [-queries <count>] [-seed <seed>]");
            return;
        }

        String graphPath = null;
        int queryCount = 50;
        long seed = System.currentTimeMillis();

        for (int i = 0; i < args.length; i++) {
            if ("-graph".equals(args[i]) && i + 1 < args.length) {
                graphPath = args[++i];
            } else if ("-queries".equals(args[i]) && i + 1 < args.length) {
                queryCount = Integer.parseInt(args[++i]);
            } else if ("-seed".equals(args[i]) && i + 1 < args.length) {
                seed = Long.parseLong(args[++i]);
            }
        }

        if (graphPath == null) {
            System.err.println("Error: No graph path provided.");
            return;
        }

        try { runBenchmark(graphPath, queryCount, seed); } 
        catch (Exception e) { e.printStackTrace(); }
    }

    private static void runBenchmark(String graphPath, int queryCount, long seed) throws Exception {
        System.out.println("================================");
        System.out.println("PHASE III - ALGORITHM EVALUATION");
        System.out.println("================================");
        System.out.println("Graph: " + new File(graphPath).getName());
        System.out.println("Queries: " + queryCount);
        System.out.println("Seed: " + seed);
        
        long startLoad = System.currentTimeMillis();
        Graph graph = GraphReader.readGraph(graphPath);
        long endLoad = System.currentTimeMillis();
        System.out.println("Graph Loaded in: " + (endLoad - startLoad) + " ms");
        System.out.println("Nodes: " + graph.getNodeCount());

        // 2. Setup
        Dijkstra dijkstra = new Dijkstra(graph);
        AStar aStar = new AStar(graph);
        Random random = new Random(seed);
        int nodeCount = graph.getNodeCount();

        long totalDijkstraTime = 0;
        long totalAStarTime = 0;
        long totalDijkstraSettled = 0;
        long totalAStarSettled = 0;
        
        System.out.println("\nRunning Benchmarks...");
        System.out.printf("%-10s | %-12s | %-12s | %-15s | %-15s | %s\n", 
            "Dist(km)", "Dijkstra(ms)", "A*(ms)", "Settled(D)", "Settled(A)", "Speedup");
        System.out.println("-----------------------------------------------------------------------------------------");

        int validQueries = 0;
        while (validQueries < queryCount) {
            int source = random.nextInt(nodeCount);
            int target = random.nextInt(nodeCount);
            
            if (source == target) continue;

            // Warmup / Run Dijkstra
            long t1 = System.nanoTime();
            long distD = dijkstra.oneToOne(source, target, 1.0); // 1.0 = Pure Distance
            long t2 = System.nanoTime();
            
            if (distD == -1) continue; // Unreachable (e.g. disconnected components)

            // Run A*
            long t3 = System.nanoTime();
            long distA = aStar.search(source, target);
            long t4 = System.nanoTime();

            if (distA != distD) {
                System.err.println("MISMATCH! " + source + "->" + target + " D:" + distD + " A:" + distA);
                continue;
            }

            double timeD = (t2 - t1) / 1_000_000.0;
            double timeA = (t4 - t3) / 1_000_000.0;
            
            // We use the new getter from modified Dijkstra.java
            long settledD = dijkstra.getSettledNodesCount();
            long settledA = aStar.getSettledNodesCount();
            
            totalDijkstraTime += (long)timeD;
            totalAStarTime += (long)timeA;
            totalDijkstraSettled += settledD;
            totalAStarSettled += settledA;

            double distKm = distD / 100000.0;
            double speedup = timeD / Math.max(timeA, 0.01);
            
            System.out.printf("%-10.2f | %-12.2f | %-12.2f | %-15d | %-15d | %.2fx\n", 
                distKm, timeD, timeA, settledD, settledA, speedup);
            
            validQueries++;
        }

        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println("Average Dijkstra Time: " + (totalDijkstraTime / (double)queryCount) + " ms");
        System.out.println("Average A* Time:       " + (totalAStarTime / (double)queryCount) + " ms");
        System.out.println("Global Speedup:        " + String.format("%.2fx", (double)totalDijkstraTime/totalAStarTime));
    }
}
