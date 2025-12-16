#!/bin/bash

# Set graph to use (bw-bicycle or germany-bicycle)
GRAPH=${1:-bw-bicycle}

echo ""
echo "Graph: $GRAPH"
echo "Memory: 8GB"
echo ""

# Run benchmark (provide "0" as input for the final query)
echo "0" | java -Xmx8g -cp bin Benchmark \
  -graph ${GRAPH}.fmi \
  -lon 9.098 \
  -lat 48.746 \
  -que ${GRAPH}.que \
  -s 0

echo ""
echo "Test complete!"
