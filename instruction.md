# Web-based Bicycle Route Planner – Project Instructions (Vibe Coding Guide)

This document explains **in detail** what the project is about, what must be implemented, and how the system should behave. It is written to be used as **context for AI-assisted (vibe) coding in VS Code**. The goal is that an AI reading this file fully understands the requirements, constraints, and expectations of the university project.

---

## 1. Project Overview

This project is part of a **Programmierprojekt** at the **University of Stuttgart** (FMI).

The goal is to implement a **web-based bicycle route planner** for Germany (or Baden-Württemberg), similar in spirit to Google Maps bicycle routing, but simplified and focused on algorithmic correctness and performance.

The core of the project is:
- Working with **very large graphs** (millions of nodes and edges)
- Computing **shortest paths** using **Dijkstra’s algorithm**
- Taking into account both
  - physical **distance**, and
  - **positive elevation gain** (uphill only)

The project is completed for **6 ECTS (pass/fail)** and is implemented **solo**, using **Java**.

AI usage is explicitly allowed and encouraged.

---

## 2. High-Level System Architecture

The system consists of two main parts:

### 2.1 Backend (Core Logic)

Implemented in **Java**.

Responsibilities:
- Read and store a large road graph from disk
- Read and cache elevation data from NASA SRTM files
- Compute edge costs (distance + elevation gain)
- Run Dijkstra’s algorithm efficiently
- Answer routing queries
- Provide an HTTP API for the web frontend

### 2.2 Frontend (Web Interface – Phase II)

Implemented using:
- HTML
- JavaScript
- Leaflet.js (map rendering)

Responsibilities:
- Display a map
- Let the user click start and destination points
- Send AJAX requests to the backend
- Display the computed route on the map
- Show distance and elevation gain

---

## 3. Input Data Files

### 3.1 Graph Files (`*.fmi`)

These files represent the road / bicycle network.

Structure:
1. Optional empty lines and comment lines starting with `#`
2. One line: number of nodes `n`
3. One line: number of edges `m`
4. `n` lines describing nodes:
   ```
   <node id> <osm id> <latitude> <longitude> 0
   ```
   - Node IDs are consecutive
   - Only latitude and longitude are relevant
5. `m` lines describing edges:
   ```
   <source node id> <target node id> <edge length in cm> <edge type>
   ```
   - Edge type is irrelevant
   - Edges are sorted by source and target node IDs

### 3.2 Elevation Files (`*.hgt`)

Elevation data comes from **NASA SRTM** files.

Properties:
- Each file covers a **1° x 1° geographic tile**
- Example: `N47E009.hgt`
- Each file contains `3601 x 3601` height values
- Each value:
  - 2 bytes (signed integer)
  - Measured in **meters**
- Stored:
  - North → South
  - West → East
- No line breaks (pure binary data)

Important rules:
- Each `.hgt` file must be loaded **at most once**
- Files must be cached in memory
- Heights must be converted to **centimeters** and stored as `int`

### 3.3 Query Files (`*.que`, `*.sol`)

- `.que`: routing queries
  ```
  <source node id> <target node id> <weight>
  ```
- `.sol`: expected shortest-path results

Console output must **exactly match** the `.sol` files.

---

## 4. Graph Representation (Mandatory)

The graph must be stored using an **Adjacency Array representation**.

### 4.1 Nodes

For each node store:
- Latitude (`double`)
- Longitude (`double`)
- Height (`int`, in centimeters)

### 4.2 Edges

For each edge store:
- Distance (`int`, in centimeters)
- Positive elevation gain (`int`, in centimeters)

Rules for elevation gain:
- If target node is higher → `height(target) - height(source)`
- If flat or downhill → `0`

---

## 5. Elevation Interpolation

Node heights are **not directly stored** in the graph file.

To compute node height:
1. Determine the corresponding `.hgt` tile
2. Locate the surrounding grid points
3. Use **three surrounding elevation values**
4. Compute the height using **barycentric interpolation**

This must be done when the graph is loaded.

---

## 6. Shortest Path Algorithms

Two versions of **Dijkstra’s algorithm** must be implemented:

### 6.1 One-to-One Dijkstra

- Computes the shortest path from a source node to a target node
- Stops as soon as the target is reached
- Used for interactive routing

### 6.2 One-to-All Dijkstra

- Computes distances from a source node to all other nodes
- Used for benchmarking

### 6.3 Edge Cost Function

Each query provides a weight `w` in `[0, 1]`.

Edge cost:
```
cost(e) = w * distance(e) + (1 - w) * elevationGain(e)
```

### 6.4 Priority Queue Rules (Very Important)

In Java:
- Node distances must **not be modified** while inside a `PriorityQueue`
- Use immutable queue entries or re-insert nodes

Failure to follow this rule leads to incorrect results.

---

## 7. Performance Constraints (Germany Graph)

The implementation must satisfy:

- Graph loading: ≤ 3 minutes
- One-to-all Dijkstra: ≤ 40 seconds
- Nearest-node lookup: ≤ 1 second
- Memory usage: ≤ 12 GB RAM

Performance is tested on the instructor’s machine.

---

## 8. Nearest Node Lookup

Given a latitude/longitude position:
- Find the closest graph node
- A naive O(n) search using Euclidean distance is acceptable
- Must finish within 1 second

---

## 9. Benchmark Interface (Phase I)

The program must be callable from the console without menus.

Example:
```
java Benchmark \
  -graph germany-bicycle.fmi \
  -lon 9.098 \
  -lat 48.746 \
  -que germany-bicycle.que \
  -s 638394
```

The output must exactly match the `.sol` files.

---

## 10. Web Application (Phase II)

### 10.1 Backend Server

- Simple HTTP server (e.g., `com.sun.net.httpserver.HttpServer`)
- Runs locally
- Accessible via `http://localhost:8080`

### 10.2 Frontend

- HTML + JavaScript
- Leaflet.js for map rendering
- Tile server must be:
  ```
  tiles.fmi.uni-stuttgart.de
  ```

### 10.3 Features

- Click on map to select start and destination
- Server returns nearest node ID
- Marker placed immediately
- Slider controls distance vs elevation trade-off
- Route returned as **GeoJSON**
- Display:
  - Total distance
  - Positive elevation gain

Multiple routes must be computed sequentially using **one-to-one Dijkstra**.

---

## 11. Slider Rescaling Logic

Because distance and elevation have different magnitudes:

1. Compute shortest-distance route
2. Compute minimum-elevation route
3. Use these to estimate max distance and elevation
4. Map slider values so that:
   - Changes affect most of the slider range
   - All weights from 0 to 1 remain selectable

---

## 12. Constraints and Rules

- Must run on Ubuntu 20.04
- Must compile and run from the command line
- Build system should be simple (e.g., `javac`)
- Graph path must NOT be hardcoded
- Configuration via CLI arguments or config file

---

## 13. Development Philosophy (Vibe Coding)

This project is developed using **AI-assisted iterative coding**:

- Start simple
- Validate correctness first
- Optimize only as much as required
- Prefer clarity over cleverness
- Avoid over-engineering

AI is used to:
- Generate boilerplate code
- Implement algorithms
- Debug issues
- Refactor for clarity

Human focus is on:
- Understanding requirements
- Connecting components
- Testing and validation

---

## 14. Goal

The final system must:
- Correctly compute routes
- Meet performance constraints
- Pass automated benchmarks
- Provide a usable web interface

Passing the course is the primary objective.

