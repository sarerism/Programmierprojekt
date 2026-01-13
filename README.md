# Bicycle Route Planner - Programmierprojekt - Sareer Ahmed

Phase I submission for Programmierprojekt!!

## What it does

Calculates bicycle routes in Germany that minimize either distance or elevation gain (or something in between ) Uses Dijkstra on a graph with 12M+ nodes

The program reads a large graph file (.fmi format), loads elevation data from NASA SRTM files, and computes shortest paths based on a weighted cost function combining distance and uphill climbing

## Requirements

- Java 17 or newer
- At least 8GB RAM for bw-bicycle.fmi, 12GB for germany-bicycle.fmi
- Graph files (.fmi) and elevation data (srtm/*.hgt) need to be in the same directory

## Compilation

On Ubuntu 20.04 (or similar):

```bash
javac -d bin src/graph/*.java src/io/*.java src/routing/*.java Benchmark.java
```

Or just use the build script ;)

```bash
./build.sh
```

## Running the Benchmark

Basic format:
```bash
echo "0" | java -Xmx8g -cp bin Benchmark \
  -graph <graph-file>.fmi \
  -que <query-file>.que \
  -lon <longitude> \
  -lat <latitude> \
  -s <random-seed>
```

Example with Baden-Württemberg:
```bash
echo "0" | java -Xmx8g -cp bin Benchmark \
  -graph bw-bicycle.fmi \
  -que bw-bicycle.que \
  -lon 9.098 \
  -lat 48.746 \
  -s 0
```

Example with full Germany graph:
```bash
echo "0" | java -Xmx12g -cp bin Benchmark \
  -graph germany-bicycle.fmi \
  -que germany-bicycle.que \
  -lon 9.098 \
  -lat 48.746 \
  -s 638394
```

The `echo "0"` is because the benchmark asks for the weight parameter (0 = minimize elevation, 1 = minimize distance).

## How it works

**Graph structure:** Adjacency arrays (one array for nodes, one for edges, offset array to map between them)Loads the entire graph into memory at startup.

**Elevation data:** Reads .hgt files on-demand and caches them. Each file is a 3601×3601 grid of elevation values. Uses barycentric interpolation to get elevation at arbitrary coordinates

**Routing:** Standard Dijkstra with a priority queue. For one-to-one queries it stops early when the target is found. For one-to-all it runs until all reachable nodes are processed

**Cost function:** `cost = weight × distance + (1-weight) × elevationGain` where elevation gain is only counted when going uphill.

## Project Structure

```
src/
  ├── Benchmark.java          # Main program
  ├── graph/
  │   ├── Graph.java          # Adjacency array representation
  │   ├── Node.java           # Stores lat/lon/elevation
  │   └── Edge.java           # Stores distance and elevation gain
  ├── io/
  │   ├── GraphReader.java    # Parses .fmi files
  │   └── ElevationReader.java # Reads .hgt files with caching
  └── routing/
      ├── Dijkstra.java       # Shortest path implementation
      └── NodeFinder.java     # Finds nearest node to GPS coordinates
```

## Performance

Tested on MacBook M4 Max 128GB RAM:

- Loading bw-bicycle.fmi: ~10 seconds
- Loading elevation data: ~5 seconds
- One-to-all Dijkstra on 12M nodes: ~2 seconds
- Finding nearest node: ~15ms

Should easily meet the performance requirements (3 minutes for loading, 1 minute for elevation, 30 seconds for Dijkstra) (hopefully 🤞)

## Notes

The results match the provided .sol files to within 0.03% (probably rounding differences in the interpolation). The graph uses about 600MB of RAM when loaded.

Elevation files need to be in a folder called `srtm/` in the working directory. The program will crash if it can't find the files it needs.
