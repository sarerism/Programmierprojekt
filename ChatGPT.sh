#!/bin/bash

# Paths to data files - UPDATE THESE to your local paths!
GRAPH_PATH="/Users/SARAHME/Developer/Programmierprojekt/bicycle.fmi"
QUE_PATH="/Users/SARAHME/Developer/Programmierprojekt/germany-bicycle.que"
SOL_PATH="/Users/SARAHME/Developer/Programmierprojekt/germany-bicycle.sol"
SRTM_DIR="/Users/SARAHME/Developer/Programmierprojekt/srtm" # Folder containing all .hgt files

echo "Compiling..."
javac -d bin src/**/*.java || exit 1

echo "Timing graph load + elevation..."
start=$(date +%s)
java -cp bin Benchmark -graph "$GRAPH_PATH" -srtm "$SRTM_DIR" -timingOnly load
end=$(date +%s)
load_time=$((end - start))
echo "Graph + Elevation loading took $load_time seconds"

echo "Timing one-to-all Dijkstra..."
start=$(date +%s)
java -cp bin Benchmark -graph "$GRAPH_PATH" -que "$QUE_PATH" -s 638394 -timingOnly dijkstra
end=$(date +%s)
dijkstra_time=$((end - start))
echo "One-to-all Dijkstra took $dijkstra_time seconds"

echo "Timing nearest node lookup..."
start=$(date +%s)
java -cp bin Benchmark -graph "$GRAPH_PATH" -lon 9.098 -lat 48.746 -timingOnly nearestNode
end=$(date +%s)
nearest_time=$((end - start))
echo "Nearest node lookup took $nearest_time seconds"