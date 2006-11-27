package edu.hawaii.senin.rjimage.model;

import static org.junit.Assert.assertEquals;

import org.apache.commons.math.MathException;
import org.junit.Before;
import org.junit.Test;

public class TestIClass {

  private IClass class1;
  private IClass class2;

  @Before
  public void startUp() {
    this.class1 = new IClass(0.33, 0.1, 0.5);
    this.class2 = new IClass(0.66, 0.1, 0.5);
  }

  @Test
  public void TestProbability() throws MathException {
    assertEquals("Checking cumulative P", 0.5, class1.dist.cumulativeProbability(0.33));
    assertEquals("Checking cumulative P", 0.5, class2.dist.cumulativeProbability(0.66));
    // assertEquals("Checking mean p = 1", 3.989423, class1.getPValue(0.33));
    // assertEquals("Checking mean p = 1", 3.989423, class2.getPValue(0.66));
  }

}
