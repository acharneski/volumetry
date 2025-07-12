package com.simiacryptus.probabilityModel.benchmark;

/**
 * Configuration settings for benchmark experiments
 */
public class Settings {
    
    // Default settings for benchmark execution
    public static final int DEFAULT_SAMPLE_SIZE = 1000;
    public static final int DEFAULT_MIN_POINT_THRESHOLD = 10;
    public static final double DEFAULT_VOLUME_MIN = 0.0;
    public static final double DEFAULT_VOLUME_MAX = 1.0;
    public static final boolean DEFAULT_DO_PLOTS = true;
    public static final boolean DEFAULT_SELF_CROSS = false;

    public Settings() {
        // Default constructor
    }

    @Override
    public String toString() {
        // Instance variables for runtime configuration
        return "Settings{" +
                "sampleSize=" + DEFAULT_SAMPLE_SIZE +
                ", minPointThreshold=" + DEFAULT_MIN_POINT_THRESHOLD +
                ", volumeMin=" + DEFAULT_VOLUME_MIN +
                ", volumeMax=" + DEFAULT_VOLUME_MAX +
                ", doPlots=" + DEFAULT_DO_PLOTS +
                ", selfCross=" + DEFAULT_SELF_CROSS +
                '}';
    }
}