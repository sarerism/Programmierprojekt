#!/bin/bash

# Compiles all Java source files

echo "Let me cook"

mkdir -p bin

javac -d bin \
    Benchmark.java \
    src/graph/Node.java \
    src/graph/Edge.java \
    src/graph/Graph.java \
    src/io/GraphReader.java \
    src/io/ElevationReader.java \
    src/routing/Dijkstra.java \
    src/routing/NodeFinder.java \
    src/routing/DijkstraWithPath.java \
    src/routing/RouteService.java \
    src/server/WebServer.java

if [ $? -eq 0 ]; then
    
    echo ""
    echo "Cooked successfully!"
    echo ""
    echo "Output directory: bin/"
    echo ""
    echo "To run benchmark, run the command below :"
    echo ""
    echo "  java -Xmx8g -cp bin Benchmark -graph bw-bicycle.fmi -lon 9.098 -lat 48.746 -que bw-bicycle.que -s 0"
    echo ""
else
    echo "Over cooked :( Compilation failed!"
    exit 1
fi
