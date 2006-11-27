package edu.hawaii.senin.rjimage.model;

import org.apache.commons.math.distribution.NormalDistributionImpl;

/**
 * Represent image segmentation class i.e. contains mean and stdev for particular image class.
 * 
 * @author Pavel Senin.
 * 
 */
public class IClass {

  /**
   * Mean of the class Gaussian.
   */
  private Double mean;

  /**
   * Standard Deviation of the class Gaussian.
   */
  private Double stdev;

  /**
   * Weight of particular label.
   */
  private Double weight;
  /**
   * This distribution Factory.
   */
  public NormalDistributionImpl dist;

  /**
   * Instantiates new class Gaussian.
   * 
   * @param mean mean value.
   * @param stdev standard deviation value.
   */
  public IClass(Double mean, Double stdev, Double weight) {
    this.mean = mean;
    this.stdev = stdev;
    this.weight = weight;
    this.dist = new NormalDistributionImpl(this.mean, this.stdev);
  }

  public Double getPValue(Double r) {
    Double part1 = 1 / (Math.sqrt(2 * Math.PI) * this.stdev);
    Double part2 = Math.exp(-(((r - this.mean) * (r - this.mean)) / (2 * this.stdev * this.stdev)));
    return part1 * part2;
  }

  /**
   * Returns this gaussian mean value.
   * 
   * @return mean.
   */
  public Double getMean() {
    return this.mean;
  }

  /**
   * Returns this gaussian standard deviation.
   * 
   * @return standard deviation.
   */
  public Double getStDev() {
    return this.stdev;
  }

  /**
   * Returns this class label weight.
   * 
   * @return weight.
   */
  public Double getWeight() {
    return this.weight;
  }
}
