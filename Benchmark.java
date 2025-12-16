import graph.Graph;
import graph.Node;
import io.GraphReader;
import io.ElevationReader;
import routing.Dijkstra;
import routing.NodeFinder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.Scanner;

public class Benchmark {

	public static void main(String[] args) {
		try {
			String graphPath = args[1];
			double lon = Double.parseDouble(args[3]);
			double lat = Double.parseDouble(args[5]);
			String quePath = args[7];
			int sourceNodeId = Integer.parseInt(args[9]);

			File graphFile = new File(graphPath);
		String graphDir = graphFile.getParent();
		if (graphDir == null) {
			graphDir = ".";
		}
		String srtmPath = graphDir + File.separator + "srtm";
			System.out.println("Reading graph file and creating graph data structure (" + graphPath + ")");
			long graphReadStart = System.currentTimeMillis();
			
			Graph graph = GraphReader.readGraph(graphPath);
			
			System.out.println("Reading elevation data from: " + srtmPath);
			long elevStart = System.currentTimeMillis();
			ElevationReader elevationReader = new ElevationReader(srtmPath);
			
			int totalNodes = graph.getNodeCount();
			int progressInterval = totalNodes / 20;
			if (progressInterval < 100000) progressInterval = 100000;
			
			for (int i = 0; i < totalNodes; i++) {
				Node node = graph.getNode(i);
				int elevation = elevationReader.getElevationCm(node.getLatitude(), node.getLongitude());
				node.setElevation(elevation);
				
				if (i > 0 && i % progressInterval == 0) {
					long elapsed = System.currentTimeMillis() - elevStart;
					int percent = (int)((i * 100.0) / totalNodes);
					double rate = i / (elapsed / 1000.0);
					int remaining = (int)((totalNodes - i) / rate);
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

			System.out.println("Finding closest node to coordinates " + lon + " " + lat);
			long nodeFindStart = System.currentTimeMillis();
			
			NodeFinder nodeFinder = new NodeFinder(graph);
			double[] coords = nodeFinder.findNearestNodeCoordinates(lat, lon);

			long nodeFindEnd = System.currentTimeMillis();
			System.out.println("\tfinding node took " + (nodeFindEnd - nodeFindStart) + "ms: " + coords[0] + ", " + coords[1]);

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
			} catch (Exception e) {
				System.out.println("Exception...");
				e.printStackTrace();
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
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

}
