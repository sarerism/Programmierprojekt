#!/bin/bash

# Set graph to use (bw-bicycle or germany-bicycle)
GRAPH=${1:-bw-bicycle}

# Set memory based on graph size
if [ "$GRAPH" = "germany-bicycle" ]; then
    MEM="12g"
else
    MEM="8g"
fi

echo ""
echo "Graph: $GRAPH"
echo "Memory: $MEM"
echo ""

# Run benchmark (provide "0" as input for the final query)
echo "0" | java -Xmx$MEM -cp bin Benchmark \
  -graph ${GRAPH}.fmi \
  -lon 9.098 \
  -lat 48.746 \
  -que ${GRAPH}.que \
  -s 0

echo ""
echo "Test complete!"
