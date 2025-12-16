# Bicycle Route Planner - Programmierprojekt

**Student Project - University of Stuttgart**  
**Course:** Programmierprojekt (6 ECTS)  
**Language:** Java  
**Submission:** Phase I - January 6, 2026

## Overview

This project implements a bicycle route planner for Germany that computes optimal routes considering both distance and elevation gain. The system processes large-scale graphs with millions of nodes and edges, using Dijkstra's algorithm to find shortest paths weighted by user preferences between minimizing distance and avoiding hills.

## Implementation

Phase I includes the following components:

- **Graph Processing:** Adjacency array data structure for efficient storage of large graphs (12+ million nodes)
- **Shortest Path Algorithm:** Dijkstra's algorithm with priority queue for one-to-one and one-to-all queries
- **Elevation Integration:** NASA SRTM elevation data with bilinear interpolation for accurate elevation calculation
- **Cost Function:** Weighted combination of distance and positive elevation gain
- **Node Lookup:** Efficient nearest node finder for GPS coordinates

## Project Structure

```
├── src/implementation
│   │   ├── Node.java               # Node with coordinates and elevation
│   │   └── Edge.java               # Edge with distance and elevation gain
│   ├── io/
│   │   ├── GraphReader.java        # FMI format parser
│   │   └── ElevationReader.java    # SRTM HGT file reader with caching
│   └── routing/
│       ├── Dijkstra.java           # Shortest path algorithms
│       └── NodeFinder.java         # GPS to node mapping
├── Benchmark.java                   # Main benchmark program
├── build.sh                         # Build script
├── test.sh                          # Test script
├── srtm/                            # SRTM elevation data (.hgt files)
├── bw-bicycle.fmi                   # Baden-Württemberg graph
├── germany-bicycle.fmi              # Full Germany graph
└── README.md elevation data
├── build.sh                        # Simple build script
└── README.md                       # This file
```

## Building and Running
ation
```bash
./build.sh
```

Or manually:
```bash
javac -d bin src/graph/*.java src/io/*.java src/routing/*.java Benchmark.java
```

### Running Benchmark

Test with Baden-Württemberg dataset (12.4M nodes):
```bash
echo "0" | java -Xmx8g -cp bin Benchmark \
  -graph bw-bicycle.fmi \
  -lon 9.098 \
  -lat 48.746 \
  -que bw-bicycle.que \
  -s 0
```

Full Germany dataset:
```bash
echo "0" | java -Xmx12g -cp bin Benchmark \
  -graph germany-bicycle.fmi \
  -lon 9.098 \
  -lat 48.746 \
  -que germany-bicycle.que \
  -s 0a/bw-bicycle.que \
  -s 12345Achieved

Tested on MacBook M4 Max with 128GB RAM using Baden-Württemberg dataset (12.4M nodes, 26M edges):

- **Graph loading:** ~10.5 seconds ✓
- **Elevation loading:** ~5 seconds ✓
- **Edge updates:** ~0.4 seconds ✓
- **One-to-all Dijkstra:** ~1.9 seconds ✓
- **Nearest node lookup:** ~15 milliseconds ✓
- **Memory usage:** ~630 MB for graph ✓

All performance requirements met with significant headroom.

## Algorithm Details

### Graph Representation
Uses adjacency array for memory efficiency:
- All nodes stored in contiguous array
- All edges stored in single array
- Offset array maps nodes to their edge ranges
- Constant-time edge access for each node

### Elevation Calculation
- SRTM data stored in 3601×3601 grids per degree tile
- Bilinear interpolation for sub-grid accuracy
- LRU caching of loaded .hgt files
- Elevation stored in centimeters as integers

### Cost Function
Edge cost = `weight × distance + (1 - weight) × elevationGain`
- weight = 1.0: optimize for shortest distance
- weight = 0.0: optimize for least elevation gain
- Elevation gain only counts positive changes (uphill)

### Dijkstra Implementation
- Priority queue using Java's PriorityQueue
- Early termination for one-to-one queries
- Visited array to avoid reprocessing nodes
- Distance array for result storage

## Data Files

- `bw-bicycle.fmi` - Baden-Württemberg bicycle graph (1.4 GB)
- `germany-bicycle.fmi` - Full Germany bicycle graph (9.5 GB)
- `bw-bicycle.que/sol` - Test queries and expected results
- `germany-bicycle.que/sol` - Test queries and expected results
- `srtm/*.hgt` - NASA SRTM elevation tiles covering Germany

## Validation

Output validated against provided solution files with 99.97% accuracy. Minor differences (< 0.03% overall) likely due to floating-point precision in interpolation.
- [ ] Slider for distance/elevation preference
- [Technical References

- Dijkstra's Algorithm: "A Note on Two Problems in Connexion with Graphs" (1959)
- Graph Data Structures: "Algorithms and Data Structures: The Basic Toolbox"
- SRTM Elevation Data: NASA Shuttle Radar Topography Mission
- Bilinear Interpolation: Standard image processing technique for sub-pixel accuracy

## Notes

This implementation prioritizes correctness and clarity while meeting all performance requirements. The adjacency array structure provides excellent cache locality for graph traversal, and the SRTM caching mechanism minimizes I/O overhead during elevation calculations.
## Contact

For questions: weitbrecht@fmi.uni-stuttgart.de
