# Phase I: Requirements vs Implementation Breakdown

## Overview
This document provides a detailed step-by-step breakdown of what was required for Phase I of the Programmierprojekt and what was implemented.

---

## Requirements → Implementation

### 1. **Graph Data Structure**
**Requirement:** Implement an efficient graph representation that can handle millions of nodes and edges.

**Implementation:**
- Created `Graph.java` with **Adjacency Array** structure
- Used three arrays: `nodes[]`, `edges[]`, `edgeOffsets[]`
- Achieves O(1) access to node data and edges
- Stores 12.4M nodes and 26M edges in ~630MB RAM
- Memory-efficient: ~50 bytes per node

---

### 2. **Node Class**
**Requirement:** Store node information including geographic coordinates and elevation.

**Implementation:**
- Created `Node.java` with:
  - `latitude` (double): Geographic latitude
  - `longitude` (double): Geographic longitude
  - `elevation` (int): Elevation in centimeters
- Simple data class with getters/setters

---

### 3. **Edge Class**
**Requirement:** Store edge information including distance and elevation gain.

**Implementation:**
- Created `Edge.java` with:
  - `targetId` (int): Target node index
  - `distance` (int): Distance in centimeters
  - `elevationGain` (int): Positive elevation gain in centimeters
- `getCost(double weight)` method:
  - Formula: `weight × distance + (1-weight) × elevationGain`
  - Returns rounded integer cost

---

### 4. **Graph File Reading**
**Requirement:** Parse `.fmi` graph files efficiently.

**Implementation:**
- Created `GraphReader.java`
- Reads custom binary `.fmi` format
- Parses 12.4M nodes in **10.5 seconds** (requirement: <3 minutes)
- Builds adjacency array in single pass
- Handles large files with BufferedReader

---

### 5. **Elevation Data Reading**
**Requirement:** Read NASA SRTM `.hgt` files and interpolate elevation for arbitrary coordinates using **barycentric interpolation**.

**Implementation:**
- Created `ElevationReader.java`
- Reads binary SRTM files (3601×3601 grids)
- Implements **barycentric interpolation**:
  - Splits each grid square into 2 triangles
  - Uses 3 points for weighted interpolation
  - More accurate than bilinear (4 points)
- Caches up to 91 `.hgt` files in HashMap
- Loads all elevation data in **5 seconds** (requirement: <1 minute)

---

### 6. **Edge Elevation Update**
**Requirement:** Calculate elevation gain for each edge based on node elevations.

**Implementation:**
- Implemented `updateEdgeElevations()` in `GraphReader.java`
- For each edge:
  - Gets source and target node elevations
  - Calculates: `elevationGain = max(0, targetElevation - sourceElevation)`
  - Stores only positive gains (uphill)
- Processes 26M edges in **0.4 seconds**

---

### 7. **Dijkstra's Algorithm - One-to-One**
**Requirement:** Find shortest path between two specific nodes.

**Implementation:**
- Created `Dijkstra.java` with `oneToOne()` method
- Uses `PriorityQueue` for efficient node selection
- Immutable `DijkstraEntry` objects (no external mutation)
- Early termination when target is reached
- Returns shortest path cost

---

### 8. **Dijkstra's Algorithm - One-to-All**
**Requirement:** Find shortest paths from one source to all other nodes.

**Implementation:**
- Implemented `oneToAll()` in `Dijkstra.java`
- Processes entire graph from source
- Returns array of distances to all nodes
- Completes in **1.9 seconds** for 12.4M nodes (requirement: <5 minutes)
- Returns `-1` for unreachable nodes

---

### 9. **Node Finder**
**Requirement:** Find nearest graph node to given GPS coordinates.

**Implementation:**
- Created `NodeFinder.java`
- `findNearestNode()` uses linear search
- Calculates Euclidean distance to all nodes
- Completes in **15ms** for 12.4M nodes
- Simple but effective O(n) algorithm

---

### 10. **Query Processing**
**Requirement:** Process query files with source/target coordinates and routing weight.

**Implementation:**
- Implemented in `Benchmark.java`
- Parses `.que` files (format: `srcLat srcLon tgtLat tgtLon weight`)
- For each query:
  1. Finds nearest nodes to source/target coordinates
  2. Runs Dijkstra with specified weight
  3. Outputs result in centimeters
- Handles 150 queries efficiently

---

### 11. **Output Format**
**Requirement:** Output shortest path costs in centimeters, one per line.

**Implementation:**
- `Benchmark.java` outputs to stdout
- Format: One integer per line (cost in cm)
- Matches required `.sol` file format exactly
- Achieved **99.97% accuracy** vs reference solutions
- **100% perfect match** for distance-only queries (weight=1.0)

---

### 12. **Performance Requirements**
**Requirement:** Process large graphs within reasonable time limits.

**Implementation Results:**
| Operation | Time | Requirement | Status |
|-----------|------|-------------|--------|
| Load graph | 10.5s | <3 min | ✅ 17x faster |
| Load elevations | 5s | <1 min | ✅ 12x faster |
| Update edges | 0.4s | - | ✅ |
| Find nearest node | 15ms | - | ✅ |
| One-to-all Dijkstra | 1.9s | <5 min | ✅ 158x faster |

**All performance requirements exceeded by 10-158x!**

---

## Additional Components

### Main Program (`Benchmark.java`)
- Command-line interface
- Workflow orchestration:
  1. Load graph from `.fmi` file
  2. Load elevations from SRTM directory
  3. Update edge elevations
  4. Find nearest node (if coordinates provided)
  5. Process query file
  6. Run one-to-all Dijkstra (if source provided)
- Error handling and input validation

### Build System
- `build.sh`: Compiles all Java files
- `test.sh`: Runs tests with bw-bicycle dataset
- Uses OpenJDK 21

---

## Validation Results

### Accuracy Analysis
- Total queries tested: **150**
- Overall accuracy: **99.97%**
- Distance-only queries (weight=1.0): **100% perfect match**
- Total difference across ALL routes: **190,203 cm** (0.028% of total distance)
- Average error per route: **1,268 cm** (12.68 meters)

### Professor Approval
**Status:** ✅ **APPROVED**
> "Ja, das passt so" - Professor confirmation that 99.97% accuracy is acceptable

---

## Key Technical Decisions

1. **Adjacency Array over Adjacency List**
   - More memory-efficient for large graphs
   - Better cache locality
   - Direct array access (O(1))

2. **Barycentric vs Bilinear Interpolation**
   - Initially tried bilinear (4 points)
   - Switched to barycentric (3 points) per instruction.md
   - Triangle-based approach more accurate for terrain

3. **Elevation Gain = max(0, diff)**
   - Only stores positive elevation gains
   - Downhill sections contribute zero to cost
   - Matches real-world cycling preferences

4. **Immutable Dijkstra Entries**
   - Prevents external mutation bugs
   - Cleaner code
   - Easier to debug

5. **File Caching for SRTM**
   - HashMap cache for .hgt files
   - Avoids re-reading same files
   - Handles 91 files efficiently

---

## Files Created

### Source Code
- `src/graph/Node.java` - Node data structure
- `src/graph/Edge.java` - Edge data structure
- `src/graph/Graph.java` - Adjacency array graph
- `src/io/GraphReader.java` - Graph file parser
- `src/io/ElevationReader.java` - SRTM elevation reader
- `src/routing/Dijkstra.java` - Shortest path algorithms
- `src/routing/NodeFinder.java` - Nearest node finder
- `Benchmark.java` - Main program

### Build & Test
- `build.sh` - Compilation script
- `test.sh` - Testing script
- `README.md` - Project documentation

---

## Phase I Completion Status

**✅ COMPLETE AND APPROVED**

All requirements met and exceeded. Project ready for submission.

**Submission Deadline:** January 6, 2026

---

*Generated: December 16, 2024*
