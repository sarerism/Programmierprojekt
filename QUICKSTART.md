# Phase II - Quick Start Guide

## Start the Server

```bash
./start-server.sh bw-bicycle.fmi
```

Or with custom port:
```bash
./start-server.sh bw-bicycle.fmi 9000
```

## Access the Application

Open your browser and go to:
```
http://localhost:8080
```

## How to Use

1. **Click on the map** to set start point (green marker)
2. **Click again** to set target point (red marker)
3. Route is computed automatically
4. **Adjust the slider** to change route preference:
   - Left: Minimize elevation gain
   - Right: Minimize distance
5. **Click "Clear Route"** to start over

## Files Added for Phase II

### Backend (Java)
- `src/server/WebServer.java` - HTTP server and request handlers
- `src/routing/DijkstraWithPath.java` - Dijkstra with path reconstruction
- `src/routing/RouteService.java` - Route computation with weight rescaling

### Frontend
- `web/index.html` - Complete web interface with Leaflet map

### Scripts
- `start-server.sh` - Server startup script
- Updated `build.sh` - Includes Phase II files

### Documentation
- `README_PHASE_II.md` - Complete documentation
- `QUICKSTART.md` - This file

## API Endpoints

### Find Nearest Node
```
GET /nearest?lat=48.746&lon=9.098
```

### Compute Route
```
GET /route?from=638394&to=123456&slider=0.42
```

## Notes

- Graph loading takes 1-3 minutes depending on size
- Server stays running until manually stopped (Ctrl+C)
- All Phase I code is reused without modification
- Routes are computed using one-to-one Dijkstra
- Slider value is rescaled for meaningful effect across full range
