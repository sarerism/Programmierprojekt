# Bicycle Route Planner - Phase II

Web-based bicycle route planning application developed for University of Stuttgart (FMI).

## Overview

This project implements a complete web-based bicycle route planner that:
- Computes optimal bicycle routes considering distance and elevation gain
- Provides an interactive map interface using Leaflet
- Allows trade-offs between shortest distance and minimal elevation gain
- Runs locally without external dependencies

## Features

### Backend (Java)
- Simple HTTP server using `com.sun.net.httpserver.HttpServer`
- Graph loaded once at startup and kept in memory
- RESTful API endpoints for nearest node lookup and route computation
- Efficient Dijkstra implementation with path reconstruction
- Smart weight rescaling for meaningful slider behavior

### Frontend (HTML + JavaScript)
- Interactive Leaflet map with OpenStreetMap tiles
- Click-based route planning (start → target)
- Slider for distance/elevation trade-off
- Real-time route computation and display
- Distance and elevation gain statistics

## System Requirements

- Java Development Kit (JDK) 11 or higher
- 12 GB RAM (for Germany graph)
- Modern web browser (Chrome, Firefox, Safari, Edge)
- Ubuntu 20.04 (or compatible Linux/macOS/Windows)

## Project Structure

```
.
├── src/
│   ├── graph/          # Graph data structures (Phase I)
│   ├── io/             # Graph and elevation readers (Phase I)
│   ├── routing/        # Dijkstra algorithms (Phase I + Phase II)
│   └── server/         # HTTP server (Phase II)
├── web/
│   └── index.html      # Frontend application
├── build.sh            # Compilation script
├── start-server.sh     # Server startup script
└── README_PHASE_II.md  # This file
```

## Compilation

### Automatic Compilation

The compilation happens automatically when you start the server for the first time.

### Manual Compilation

```bash
./build.sh
```

This compiles all Java sources into the `bin/` directory.

## Starting the Server

### Quick Start

```bash
./start-server.sh <path-to-graph.fmi> [port]
```

### Examples

```bash
# Start with Baden-Württemberg graph on default port (8080)
./start-server.sh bw-bicycle.fmi

# Start with Germany graph on custom port
./start-server.sh germany-bicycle.fmi 9000
```

### Manual Start

```bash
# Compile first
./build.sh

# Start server
java -cp bin server.WebServer --graph germany-bicycle.fmi --port 8080
```

### Alternative: Interactive Mode

If you don't provide the graph path, the server will ask for it:

```bash
java -cp bin server.WebServer
Enter path to graph file (.fmi): germany-bicycle.fmi
```

## Using the Application

1. **Start the server** using one of the methods above
2. **Open your browser** and navigate to: `http://localhost:8080`
3. **Click on the map** to set your start point (green marker)
4. **Click again** to set your target point (red marker)
5. The route will be computed and displayed automatically
6. **Adjust the slider** to change the route preference:
   - Left: Minimize elevation gain (hillier route may be longer)
   - Right: Minimize distance (shortest route, may be steeper)
7. **Click "Clear Route"** to start a new route planning

## API Endpoints

### GET /nearest

Finds the nearest graph node to given coordinates.

**Request:**
```
GET /nearest?lat=48.746&lon=9.098
```

**Response:**
```json
{
  "nodeId": 638394,
  "lat": 48.7458624,
  "lon": 9.0978176
}
```

### GET /route

Computes a route from source to target node.

**Request:**
```
GET /route?from=638394&to=123456&slider=0.42
```

Parameters:
- `from`: Source node ID
- `to`: Target node ID
- `slider`: Value between 0.0 (elevation priority) and 1.0 (distance priority)

**Response:**
```json
{
  "distanceCm": 12345678,
  "elevationGainCm": 345678,
  "geojson": {
    "type": "LineString",
    "coordinates": [[9.098, 48.746], [9.099, 48.747], ...]
  }
}
```

## Implementation Details

### Weight Rescaling

The slider value is rescaled to provide meaningful route variations across the entire range:

1. Compute pure distance route (weight = 1.0)
2. Compute pure elevation route (weight = 0.0)
3. Determine maximum distance and elevation bounds
4. Apply non-linear transformation (power 0.7) to slider value
5. Compute final route with rescaled weight

This ensures that most of the slider range affects routing, not just the extremes.

### Path Reconstruction

- Dijkstra algorithm tracks predecessors for each node
- Path is reconstructed by backtracking from target to source
- Actual distance and elevation gain are computed by traversing edges along the path

### Performance Considerations

- Graph is loaded once at startup and kept in memory
- One-to-one Dijkstra with early termination
- Typical route computation: 1-2 seconds (depending on graph size)
- Efficient adjacency array representation

## Configuration

### Port Configuration

Default port is 8080. To change:

```bash
./start-server.sh graph.fmi 9000
```

Or:

```bash
java -cp bin server.WebServer --graph graph.fmi --port 9000
```

### Graph Path

The graph path must be provided via:
- Command-line argument: `--graph <path>`
- Interactive input at startup

The SRTM elevation data directory must be in the same directory as the graph file, named `srtm/`.

## Troubleshooting

### Port Already in Use

```
Error: Address already in use
```

**Solution:** Use a different port or stop the process using port 8080:

```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill <PID>
```

### Graph Not Found

```
Error: Graph file not found
```

**Solution:** Provide the correct absolute or relative path to the `.fmi` file.

### Out of Memory

```
java.lang.OutOfMemoryError: Java heap space
```

**Solution:** Increase heap size:

```bash
java -Xmx12g -cp bin server.WebServer --graph germany-bicycle.fmi
```

### Browser Shows "Cannot GET /"

**Solution:** Make sure the `web/` directory exists and contains `index.html`.

## Technical Notes

### Why Simple HTTP Server?

Following Phase II requirements:
- No frameworks (Spring, Spark, etc.)
- Uses Java's built-in `com.sun.net.httpserver.HttpServer`
- Lightweight and sufficient for local development

### Why Recompute on Slider Change?

The slider affects the edge weight function, so the shortest path may change. Each slider adjustment triggers a new Dijkstra computation with the new weight.

### GeoJSON Format

Routes are returned as GeoJSON LineString with coordinates in `[longitude, latitude]` order (standard GeoJSON format).

## Phase I Reuse

This implementation **reuses** all Phase I code without modification:
- Graph data structures (Node, Edge, Graph)
- Graph loading (GraphReader)
- Elevation loading (ElevationReader)
- Original Dijkstra (for one-to-all benchmarks)
- Node finder

New Phase II code:
- `DijkstraWithPath` (extends Dijkstra with path reconstruction)
- `RouteService` (route computation and weight rescaling)
- `WebServer` (HTTP server and request handling)

## Authors

- University of Stuttgart - FMI
- Phase I: Graph, Dijkstra, Elevation loading
- Phase II: Web server, REST API, Frontend

## License

Educational project for University of Stuttgart.
