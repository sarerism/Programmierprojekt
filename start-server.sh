#!/bin/bash

# Bicycle Route Planner - Server Startup Script
# University of Stuttgart - FMI - Phase II

set -e

echo "================================================"
echo "  Bicycle Route Planner - Starting Server"
echo "================================================"
echo ""

# Check if graph file is provided
if [ -z "$1" ]; then
    echo "Usage: ./start-server.sh <path-to-graph.fmi> [port]"
    echo ""
    echo "Example:"
    echo "  ./start-server.sh bw-bicycle.fmi"
    echo "  ./start-server.sh germany-bicycle.fmi 8080"
    echo ""
    exit 1
fi

GRAPH_FILE=$1
PORT=${2:-8080}

# Check if graph file exists
if [ ! -f "$GRAPH_FILE" ]; then
    echo "Error: Graph file not found: $GRAPH_FILE"
    exit 1
fi

# Compile Java sources if needed
if [ ! -d "bin" ] || [ ! -f "bin/server/WebServer.class" ]; then
    echo "Compiling Java sources..."
    ./build.sh
    echo ""
fi

# Start the server
echo "Starting server with:"
echo "  Graph: $GRAPH_FILE"
echo "  Port: $PORT"
echo ""

java -cp bin server.WebServer --graph "$GRAPH_FILE" --port "$PORT"
