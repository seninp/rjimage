package edu.hawaii.senin.rjimage.main;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistributionImpl;

public class Test {

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub
    NormalDistributionImpl dist = new NormalDistributionImpl();
    try {
      System.out.println(1
          / (dist.getStandardDeviation() * Math.sqrt(2 * Math.PI))
          * Math.exp(-(0.2 - dist.getMean()) * (0.2 - dist.getMean())
              / (2 * dist.getStandardDeviation() * dist.getStandardDeviation())));

      System.out.println(1
          / (dist.getStandardDeviation() * Math.sqrt(2 * Math.PI))
          * Math.exp(-(-0.2 - dist.getMean()) * (-0.2 - dist.getMean())
              / (2 * dist.getStandardDeviation() * dist.getStandardDeviation())));

      System.out.println(dist.cumulativeProbability(-0.00001));
      System.out.println(dist.cumulativeProbability(0.8));
      System.out.println(dist.cumulativeProbability(0.12));
      System.out.println(dist.cumulativeProbability(0.2));
    }
    catch (MathException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
