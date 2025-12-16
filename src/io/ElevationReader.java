package io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads and caches NASA SRTM elevation data from .hgt filess
 * 
 * Each .hgt file contains a grid of elevation 
 * 
 * Each elevation value is stored as a 2-byte signed integer
 * Data is ordered from North to South, West to East
 */

public class ElevationReader {
    
    private static final int GRID_SIZE = 3601;
    private static final int BYTES_PER_VALUE = 2;
    
    private final String srtmDirectory;
    private final Map<String, short[]> cache;
    
    /**
     * Creates an ElevationReader for the specified SRTM directory
     * 
     * @param srtmDirectory Path to directory containing .hgt files
     */

    public ElevationReader(String srtmDirectory) {
        this.srtmDirectory = srtmDirectory;
        this.cache = new HashMap<>();
    }
    
    /**
     * Gets the elevation in meters for the specified coordinates
     * Uses barycentric interpolation for subgrid accuracy
     * 
     * @param latitude Latitude in degrees
     * @param longitude Longitude in degrees
     * @return Elevation in meter
     * @throws IOException If the required .hgt file cannot be read
     */

    public double getElevation(double latitude, double longitude) throws IOException {
        
        int latFloor = (int) Math.floor(latitude);
        int lonFloor = (int) Math.floor(longitude);
        
        String filename = getFilename(latFloor, lonFloor);
        short[] data = loadFile(filename);
        
        
        double latFraction = latitude - latFloor;
        double lonFraction = longitude - lonFloor;
        
        
        double row = (1.0 - latFraction) * 3600.0;
        double col = lonFraction * 3600.0;
        
        
        int row0 = (int) Math.floor(row);
        int row1 = Math.min(row0 + 1, 3600);
        int col0 = (int) Math.floor(col);
        int col1 = Math.min(col0 + 1, 3600);
        
        double rowFrac = row - row0;
        double colFrac = col - col0;
        
        double elev00 = getGridElevation(data, row0, col0);
        double elev01 = getGridElevation(data, row0, col1);
        double elev10 = getGridElevation(data, row1, col0);
        double elev11 = getGridElevation(data, row1, col1);
        
        
        double elevation;
        if (rowFrac + colFrac <= 1.0) {
        
            double w0 = 1.0 - rowFrac - colFrac;
            double w1 = colFrac;
            double w2 = rowFrac;
            elevation = w0 * elev00 + w1 * elev01 + w2 * elev10;
        } else {
            
            double w0 = rowFrac + colFrac - 1.0;  
            double w1 = 1.0 - rowFrac;
            double w2 = 1.0 - colFrac;
            elevation = w0 * elev11 + w1 * elev01 + w2 * elev10;
        }
        
        return elevation;
    }
    
    /**
     * Gets elevation in centimeter
     * 
     * @param latitude Latitude in degrees
     * @param longitude Longitude in degrees
     * @return Elevation in centimeters
     * @throws IOException If the required .hgt file cannot be read
     */

    public int getElevationCm(double latitude, double longitude) throws IOException {
        double elevationMeters = getElevation(latitude, longitude);
        return (int) Math.round(elevationMeters * 100.0);
    }
    
    /**
     * Loads an .hgt file from disk, or returns cached data if already loaded
     * 
     * @param filename Name of the .hgt file
     * @return Array of elevation values
     * @throws IOException If file cannot be read
     */

    private short[] loadFile(String filename) throws IOException {
        if (cache.containsKey(filename)) {
            return cache.get(filename);
        }
        
        String filepath = srtmDirectory + File.separator + filename;
        File file = new File(filepath);
        
        if (!file.exists()) {
            throw new IOException("SRTM file not found: " + filepath);
        }
        
        System.out.println("Loading elevation file: " + filename);
        byte[] bytes = Files.readAllBytes(file.toPath());
        
        int expectedSize = GRID_SIZE * GRID_SIZE * BYTES_PER_VALUE;
        if (bytes.length != expectedSize) {
            throw new IOException("Invalid .hgt file size: " + bytes.length + 
                                " (expected " + expectedSize + ")");
        }
        
        short[] elevations = new short[GRID_SIZE * GRID_SIZE];
        for (int i = 0; i < elevations.length; i++) {
            int byteIndex = i * BYTES_PER_VALUE;
            elevations[i] = (short) (
                ((bytes[byteIndex] & 0xFF) << 8) | 
                (bytes[byteIndex + 1] & 0xFF)
            );
        }
        
        cache.put(filename, elevations);
        System.out.println("  Cached " + filename + " (" + (bytes.length / (1024 * 1024)) + " MB)");
        
        return elevations;
    }
    
    /**
     * Gets elevation at a specific grid point
     * 
     * @param data Elevation data array
     * @param row Row index (0-3600)
     * @param col Column index (0-3600)
     * @return Elevation in meter
     */
    private double getGridElevation(short[] data, int row, int col) {
        int index = row * GRID_SIZE + col;
        return data[index];
    }
    
    /**
     * Generates the filename for a given lat/lon floor
     * 
     * @param latFloor Latitude floor
     * @param lonFloor Longitude floor
     * @return Filename
     */

    private String getFilename(int latFloor, int lonFloor) {
        String latPrefix = latFloor >= 0 ? "N" : "S";
        String lonPrefix = lonFloor >= 0 ? "E" : "W";
        
        int latAbs = Math.abs(latFloor);
        int lonAbs = Math.abs(lonFloor);
        
        return String.format("%s%02d%s%03d.hgt", latPrefix, latAbs, lonPrefix, lonAbs);
    }
    
    /**
     * Returns the number of cached files
     */

    public int getCacheSize() {
        return cache.size();
    }
    
    /**
     * Clears the cache to free memory.
     */
    
    public void clearCache() {
        cache.clear();
    }
}
