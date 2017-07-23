package com.github.phenomics.annosimu.app;

import java.io.File;

/**
 * Configuration of patient simulation application.
 *
 * @author <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 */
public class Options {

  /** Path to HPO OBO file to load. */
  private File pathHpoObo;

  /** Path to HPO disease annotation file. */
  private File pathHpoDiseaseAnnotation;

  /** Disease identifier in database. */
  private String diseaseId;

  /** Seed for random number generation. */
  private long seed;

  /** Minimal number of HPO terms in query. */
  private int minQuerySize;

  /** Maximal number of HPO terms in query. */
  private int maxQuerySize;

  /** Fraction of noise to use in simulation. */
  private double noiseFraction;

  /** Chance of being mapped up. */
  private double mapUpProbability;

  /**
   * Default constructor.
   */
  public Options() {
    this.diseaseId = "";
    this.seed = 1;
    this.minQuerySize = 1;
    this.maxQuerySize = 5;
    this.noiseFraction = 0.05;
    this.mapUpProbability = 0.05;
  }

  /**
   * @return {@link File} with path to HPO OBO file.
   */
  public File getPathHpoObo() {
    return pathHpoObo;
  }

  /**
   * Set the path to the HPO OBO file.
   *
   * @param pathHpoObo The value to use.
   */
  public void setPathHpoObo(File pathHpoObo) {
    this.pathHpoObo = pathHpoObo;
  }

  /**
   * @return {@link File} with path to HPO disease annotation file.
   */
  public File getPathHpoDiseaseAnnotation() {
    return pathHpoDiseaseAnnotation;
  }

  /**
   * Set the path to the HPO to disease annotation file.
   *
   * @param pathHpoDiseaseAnnotation the pathHpoDiseaseAnnotation to set
   */
  public void setPathHpoDiseaseAnnotation(File pathHpoDiseaseAnnotation) {
    this.pathHpoDiseaseAnnotation = pathHpoDiseaseAnnotation;
  }

  /**
   * @return String representation of the disease identifier to simulate for.
   */
  public String getDiseaseId() {
    return diseaseId;
  }

  /**
   * @return The seed to use for random number generation.
   */
  public long getSeed() {
    return seed;
  }

  /**
   * Set seed to use for random number generation.
   *
   * @param seed The value to use.
   */
  public void setSeed(long seed) {
    this.seed = seed;
  }

  /**
   * Set string representation of the disease identifier to simulate for.
   *
   * @param diseaseId The disease ID to use.
   */
  public void setDiseaseId(String diseaseId) {
    this.diseaseId = diseaseId;
  }

  /**
   * @return Smallest number of query terms to simulate.
   */
  public int getMinQuerySize() {
    return minQuerySize;
  }

  /**
   * Set smallest number of query terms to simulate.
   *
   * @param minQuerySize The value to use.
   */
  public void setMinQuerySize(int minQuerySize) {
    this.minQuerySize = minQuerySize;
  }

  /**
   * @return Largest number of query terms to simulate.
   */
  public int getMaxQuerySize() {
    return maxQuerySize;
  }

  /**
   * Set largest number of query terms to simulate.
   *
   * @param maxQuerySize The value to use.
   */
  public void setMaxQuerySize(int maxQuerySize) {
    this.maxQuerySize = maxQuerySize;
  }

  /**
   * @return The fraction of noise.
   */
  public double getNoiseFraction() {
    return noiseFraction;
  }

  /**
   * Set fraction of noise.
   *
   * @param noiseFraction The value to use.
   */
  public void setNoiseFraction(double noiseFraction) {
    if (noiseFraction < 0.0 || noiseFraction > 1.0) {
      throw new IllegalArgumentException("Invalid probability: " + mapUpProbability);
    }
    this.noiseFraction = noiseFraction;
  }

  /**
   * @return The probability of mapping up.
   */
  public double getMapUpProbability() {
    return mapUpProbability;
  }

  /**
   * Set probability to being mapped up.
   *
   * @param mapUpProbability The value to use.
   */
  public void setMapUpProbability(double mapUpProbability) {
    if (mapUpProbability < 0.0 || mapUpProbability > 1.0) {
      throw new IllegalArgumentException("Invalid probability: " + mapUpProbability);
    }
    this.mapUpProbability = mapUpProbability;
  }

  @Override
  public String toString() {
    return "Options [pathHpoObo=" + pathHpoObo + ", pathHpoDiseaseAnnotation="
        + pathHpoDiseaseAnnotation + ", diseaseId=" + diseaseId + ", minQuerySize=" + minQuerySize
        + ", maxQuerySize=" + maxQuerySize + ", noiseFraction=" + noiseFraction
        + ", mapUpProbability=" + mapUpProbability + "]";
  }

}
