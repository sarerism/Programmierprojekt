package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import graph.Graph;
import graph.Node;
import io.ElevationReader;
import io.GraphReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import routing.NodeFinder;
import routing.RouteService;

/**
 * Simple HTTP server :))
 * Serves static files and provides REST API for routing
 */
public class WebServer {
    
    private final Graph graph;
    private final NodeFinder nodeFinder;
    private final RouteService routeService;
    private final int port;
    
    public WebServer(Graph graph, int port) {
        this.graph = graph;
        this.nodeFinder = new NodeFinder(graph);
        this.routeService = new RouteService(graph);
        this.port = port;
    }
    
    /**
     * Starts the HTTP server
     */
    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        server.createContext("/nearest", new NearestHandler());
        server.createContext("/route", new RouteHandler());
        server.createContext("/bounds", new BoundsHandler());
        
        server.createContext("/", new StaticFileHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("Server started on http://localhost:" + port);
        System.out.println("Open http://localhost:" + port + " in your browser");
    }
    
    /**
     * Handler for /nearest endpoint
     * GET /nearest?lat=xx.xxxx&lon=yy.yyyy
     */
    private class NearestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }
            
            try {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                
                double lat = Double.parseDouble(params.get("lat"));
                double lon = Double.parseDouble(params.get("lon"));
                
                int nodeId = nodeFinder.findNearestNode(lat, lon);
                
                String json = String.format(
                    "{\"nodeId\":%d,\"lat\":%.7f,\"lon\":%.7f}",
                    nodeId,
                    graph.getNode(nodeId).getLatitude(),
                    graph.getNode(nodeId).getLongitude()
                );
                
                sendResponse(exchange, 200, json, "application/json");
                
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 400, "{\"error\":\"Invalid parameters\"}");
            }
        }
    }
    
    /**
     * Handler for /route endpoint
     * GET /route?from=xxxxxxx&to=yyyyyyy&slider=0.xx
     */
    private class RouteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }
            
            try {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                
                int from = Integer.parseInt(params.get("from"));
                int to = Integer.parseInt(params.get("to"));
                double slider = Double.parseDouble(params.get("slider"));
                
                String json = routeService.computeRoute(from, to, slider);
                
                sendResponse(exchange, 200, json, "application/json");
                
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }
    
    /**
     * Handler for /bounds endpoint
     * GET /bounds
     * Returns the geographic bounds of the loaded graph
     */
    private class BoundsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }
            
            try {
                double minLat = Double.MAX_VALUE;
                double maxLat = -Double.MAX_VALUE;
                double minLon = Double.MAX_VALUE;
                double maxLon = -Double.MAX_VALUE;
                
                Node[] nodes = graph.getNodes();
                for (Node node : nodes) {
                    double lat = node.getLatitude();
                    double lon = node.getLongitude();
                    
                    if (lat < minLat) minLat = lat;
                    if (lat > maxLat) maxLat = lat;
                    if (lon < minLon) minLon = lon;
                    if (lon > maxLon) maxLon = lon;
                }
                
                double centerLat = (minLat + maxLat) / 2.0;
                double centerLon = (minLon + maxLon) / 2.0;
                
                String json = String.format(
                    "{\"minLat\":%.7f,\"maxLat\":%.7f,\"minLon\":%.7f,\"maxLon\":%.7f,\"centerLat\":%.7f,\"centerLon\":%.7f}",
                    minLat, maxLat, minLon, maxLon, centerLat, centerLon
                );
                
                sendResponse(exchange, 200, json, "application/json");
                
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }
    
    /**
     * Handler for static files
     */
    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            if ("/".equals(path)) {
                path = "/index.html";
            }
            
            if (path.contains("..") || path.contains("//")) {
                sendResponse(exchange, 403, "Forbidden");
                return;
            }
            
            File webDir = new File("web").getCanonicalFile();
            File file = new File(webDir, path).getCanonicalFile();
            
            if (!file.getCanonicalPath().startsWith(webDir.getCanonicalPath())) {
                sendResponse(exchange, 403, "Forbidden");
                return;
            }
            
            if (!file.exists() || !file.isFile()) {
                sendResponse(exchange, 404, "Not Found");
                return;
            }
            
            byte[] content = Files.readAllBytes(file.toPath());
            String contentType = getContentType(path);
            
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, content.length);
            OutputStream os = exchange.getResponseBody();
            os.write(content);
            os.close();
        }
        
        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html";
            if (path.endsWith(".js")) return "application/javascript";
            if (path.endsWith(".css")) return "text/css";
            return "application/octet-stream";
        }
    }
    
    /**
     * Parses query parameters from URL
     */
    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null) return params;
        
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2) {
                params.put(pair[0], pair[1]);
            }
        }
        return params;
    }
    
    /**
     * Sends HTTP response
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        sendResponse(exchange, statusCode, response, "text/plain");
    }
    
    private void sendResponse(HttpExchange exchange, int statusCode, String response, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
    
    /**
     * Main entry point
     */
    public static void main(String[] args) {
        try {
            // Parse command line arguments
            String graphPath = null;
            int port = 8080;
            
            for (int i = 0; i < args.length; i++) {
                if ("--graph".equals(args[i]) && i + 1 < args.length) {
                    graphPath = args[++i];
                } else if ("--port".equals(args[i]) && i + 1 < args.length) {
                    port = Integer.parseInt(args[++i]);
                }
            }
            
            if (graphPath == null) {
                System.out.print("Enter path to graph file (.fmi): ");
                Scanner scanner = new Scanner(System.in);
                graphPath = scanner.nextLine().trim();
                scanner.close();
            }
            
            File graphFile = new File(graphPath);
            if (!graphFile.exists()) {
                System.err.println("Error: Graph file not found: " + graphPath);
                System.exit(1);
            }
            
            System.out.println("Loading graph from: " + graphPath);
            
            GraphReader reader = new GraphReader();
            Graph graph = reader.readGraph(graphPath);
            
            System.out.println("Graph loaded: " + graph);
            
            File srtmDir = new File(graphFile.getParent(), "srtm");
            if (srtmDir.exists() && srtmDir.isDirectory()) {
                System.out.println("Loading elevation data from: " + srtmDir.getAbsolutePath());
                ElevationReader elevationReader = new ElevationReader(srtmDir.getAbsolutePath());
                
                int totalNodes = graph.getNodeCount();
                System.out.println("Computing elevations for " + totalNodes + " nodes...");
                
                long elevStart = System.currentTimeMillis();
                for (int i = 0; i < totalNodes; i++) {
                    Node node = graph.getNode(i);
                    int elevation = elevationReader.getElevationCm(
                        node.getLatitude(),
                        node.getLongitude()
                    );
                    node.setElevation(elevation);
                    
                    if (i > 0 && i % 100000 == 0) {
                        System.out.println("  Progress: " + i + "/" + totalNodes + " nodes");
                    }
                }
                
                long elevEnd = System.currentTimeMillis();
                System.out.println("Elevation data loaded in " + (elevEnd - elevStart) + "ms");
                
                GraphReader.updateEdgeElevations(graph);
                System.out.println("Edge elevations updated");
            } else {
                System.out.println("Warning: SRTM directory not found, elevation data not loaded");
            }
            
            WebServer server = new WebServer(graph, port);
            server.start();
            
        } catch (Exception e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
