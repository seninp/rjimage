package edu.hawaii.senin.rjimage.model;

import java.awt.Image;
import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.apache.commons.math.MathException;
import org.apache.commons.math.random.RandomDataImpl;
import org.apache.commons.math.special.Beta;

import edu.hawaii.senin.rjimage.controller.Controller;

/**
 * This is the main class for the Reversible Jump Image Segmentation. The work based on the Zoltan
 * Kato articles concerning image segmentation problem.
 * 
 * @author Pavel Senin.
 * 
 */
public class ImageFactory extends Observable implements Runnable, Observer {
  /**
   * File name holder.
   */
  @SuppressWarnings("unused")
  private String imageFileName;
  /**
   * Holds original image.
   */
  private BufferedImage originalImage;
  /**
   * Holds original image raster in short format, grey intensity level from 0-255.
   */
  private short[][] originalRaster;
  /**
   * Holds image that in the process of segmentation.
   */
  private BufferedImage image;
  /**
   * Holds current image raster USHORT type
   */
  private short[][] raster;

  /**
   * Holds Gaussians for the image segmentation, i.e. class label is a key and IClass object holds
   * all parameters for the distribution itself.
   */
  private TreeMap<Integer, IClass> classes;
  /**
   * Holds array of labels.
   */
  private Integer[][] labels;
  /**
   * Holds beta value.
   */
  private Double beta = 0.9D;
  /**
   * Starting temperature
   */
  private Double startTemp = 6D;
  /**
   * Temperature decrease rate.
   */
  private Double coolingRate = 0.995D;

  private Integer minClasses = 2;

  private Integer maxClasses = 10;

  private String method;

  private boolean stopSimulation;

  private short[][] splitRaster;
  private Integer[][] splitLabels;
  private TreeMap<Integer, IClass> splitClasses;

  private short[][] mergeRaster;
  private Integer[][] mergeLabels;
  private TreeMap<Integer, IClass> mergeClasses;

  /**
   * Resets labeling with initial values. See Kato99Bayesian explanation.
   * 
   */
  private void resetClasses() {
    if (this.classes.size() > 0) {
      this.classes.clear();
    }
    else {
      this.classes = new TreeMap<Integer, IClass>();
    }
    // this.classes.put(Integer.valueOf(0), new IClass(0.2, 0.1, 0.2));
    // this.classes.put(Integer.valueOf(1), new IClass(0.3, 0.1, 0.2));
    // this.classes.put(Integer.valueOf(2), new IClass(0.4, 0.1, 0.2));
    // this.classes.put(Integer.valueOf(3), new IClass(0.5, 0.1, 0.2));
    // this.classes.put(Integer.valueOf(4), new IClass(0.6, 0.1, 0.2));
    // this.classes.put(Integer.valueOf(5), new IClass(0.7, 0.1, 0.2));
    // this.classes.put(Integer.valueOf(6), new IClass(0.9, 0.1, 0.2));

    this.classes.put(Integer.valueOf(0), new IClass(0.2, 0.2, 0.2));
    this.classes.put(Integer.valueOf(1), new IClass(0.4, 0.2, 0.2));
    this.classes.put(Integer.valueOf(2), new IClass(0.6, 0.2, 0.2));
    this.classes.put(Integer.valueOf(3), new IClass(0.8, 0.2, 0.2));

    // this.classes.put(Integer.valueOf(0), new IClass(0.2, 0.1, 0.2));
    // this.classes.put(Integer.valueOf(1), new IClass(0.5, 0.1, 0.2));
    // this.classes.put(Integer.valueOf(2), new IClass(0.9, 0.1, 0.2));
  }

  /**
   * Instantiates ImageFactory.
   * 
   */
  public ImageFactory() {

  }

  /**
   * Instantiates factory from supplied file.
   * 
   * @param selectedFile file to load original image.
   */
  public void initFactory(File selectedFile) {
    try {
      this.originalImage = ImageIO.read(selectedFile);
      int imageType = originalImage.getType();
      if (imageType != BufferedImage.TYPE_BYTE_GRAY) {
        this.originalImage = toGrayScale(this.originalImage);
        setChanged();
        notifyObservers(ImageFactoryStatus.INVALID_IMAGE);
      }
      this.image = null;
      this.originalRaster = null;
      this.raster = null;
      this.labels = null;
      this.classes = new TreeMap<Integer, IClass>();
      resetSegmentation();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    setChanged();
    notifyObservers(ImageFactoryStatus.NEW_IMAGE);
  }

  /**
   * Returns originally loaded image for this factory.
   * 
   * @return original image.
   */
  public BufferedImage getOriginalImage() {
    return this.originalImage;
  }

  /**
   * Re-Segments the image using given Gaussians.
   */
  public void resegmentImage() {
    // collect useful numbers
    Raster raster = originalImage.getRaster();
    Integer height = raster.getHeight();
    Integer width = raster.getWidth();
    // refresh all needed variables
    this.labels = new Integer[width][height];
    this.splitLabels = new Integer[width][height];
    this.mergeLabels = new Integer[width][height];

    this.raster = new short[width][height];
    this.splitRaster = new short[width][height];
    this.mergeRaster = new short[width][height];
    this.originalRaster = new short[width][height];

    byte[][] rasterIntermediate = new byte[width][height];
    short[][] rasterPlain = new short[width][height];

    Double[][] probHolder = new Double[width][height];

    // getting real raster
    for (int j = 0; j < width; j++) {
      raster.getDataElements(j, 0, 1, height, rasterIntermediate[j]);
    }

    // making it "short"
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        rasterPlain[i][j] = rasterIntermediate[i][j];
        rasterPlain[i][j] &= 0xff;// shift from "-128...+127" to "0...255"
        this.originalRaster[i][j] = rasterPlain[i][j];
      }
    }
    //
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {

        for (Integer c : this.classes.keySet()) {
          IClass cls = classes.get(c);
          Double pixel = rasterPlain[i][j] / 255D;
          Double p = cls.getPValue(pixel);
          if (null == probHolder[i][j]) {
            this.labels[i][j] = c;
            probHolder[i][j] = p;
          }
          else if (probHolder[i][j] < p) {
            this.labels[i][j] = c;
            probHolder[i][j] = p;
          }
        }

      }// i
    }// j

    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        this.raster[i][j] = ((Double) Math
            .floor(this.classes.get(this.labels[i][j]).getMean() * 255)).shortValue();
      }
    }

  }

  public void resetSegmentation() {
    resetClasses();
    resegmentImage();
    this.image = generateSegmentedImage();
    setChanged();
    notifyObservers(ImageFactoryStatus.NEW_SEGMENTATION);
  }

  private BufferedImage generateSegmentedImage() {

    Raster raster = this.originalImage.getRaster();
    Integer height = raster.getHeight();
    Integer width = raster.getWidth();

    byte[] rasterPlain = new byte[height * width];

    // Integer increment = 255 / this.classes.size();

    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        rasterPlain[i * width + j] = ((Double) (this.classes.get(this.labels[j][i]).getMean() * 255))
            .byteValue();
      }
    }
    // ComponentColorModel ccm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY),
    // new int[] { 255 }, false, false, Transparency.OPAQUE, DataBuffer.TYPE_USHORT);
    ColorModel ccm = this.originalImage.getColorModel();
    // ComponentSampleModel csm = new ComponentSampleModel(DataBuffer.TYPE_USHORT, width, height, 1,
    // width, new int[] { 0 });
    SampleModel csm = this.originalImage.getSampleModel();
    DataBuffer dataBuf = new DataBufferByte(rasterPlain, width);
    WritableRaster wr = Raster.createWritableRaster(csm, dataBuf, new Point(0, 0));
    Hashtable<String, String> ht = new Hashtable<String, String>();
    ht.put("owner", "Senin Pavel");
    return new BufferedImage(ccm, wr, true, ht);
  }

  /**
   * Returns current "segmented" image.
   * 
   * @return "in process" segmented image.
   */
  public Image getCurrentImage() {
    this.image = generateSegmentedImage();
    return this.image;
  }

  /**
   * Suppose to converts image to grayscale. Not tested yet.
   * 
   * @param image image to convert.
   * @return grayscale image.
   */
  public BufferedImage toGrayScale(BufferedImage image) {
    // BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(),
    // BufferedImage.TYPE_USHORT_GRAY);
    // Graphics2D g = result.createGraphics();
    // g.drawRenderedImage(image, null);
    // g.dispose();
    // return result;

    // grey = (3*red + 6*green + blue)/10;
    BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(),
        BufferedImage.TYPE_BYTE_GRAY);
    ColorSpace grayColorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
    ColorConvertOp colorConvertOp = new ColorConvertOp(grayColorSpace, null);
    colorConvertOp.filter(image, result);

    Raster raster = image.getRaster();
    Integer height = raster.getHeight();
    Integer width = raster.getWidth();

    // Raster rasterGary = result.getRaster();
    // byte[] dataGray = new byte[height * width];
    // int[][] dataColor = new int[height][width];
    //
    // for (int j = 0; j < width; j++) {
    // raster.getDataElements(j, 0, 1, height, dataColor[j]);
    // }
    //
    // // making it "byte"
    // for (int i = 0; i < height; i++) {
    // for (int j = 0; j < width; j++) {
    // int rgb = dataColor[i][j];
    // int r = (rgb >> 16) & 0xff;
    // int g = (rgb >> 8) & 0xff;
    // int b = (rgb) & 0xff;
    // int gray = (r * 30 + g * 59 + b * 11) / 100;
    // // return the color code of the new color
    // // return (rgb & 0xff000000) | (gray<<16) | (gray<<8) | (gray);
    // dataGray[i * width + j] = ((Integer) ((3 * r + 6 * g + b) / 10)).byteValue();
    // }
    // }
    //
    // ColorModel ccm = result.getColorModel();
    // SampleModel csm = result.getSampleModel();
    // DataBuffer dataBuf = new DataBufferByte(dataGray, width);
    // WritableRaster wr = Raster.createWritableRaster(csm, dataBuf, new Point(0, 0));
    // Hashtable<String, String> ht = new Hashtable<String, String>();
    // ht.put("owner", "Senin Pavel");
    // return new BufferedImage(ccm, wr, true, ht);

    return result;
  }

  /**
   * Computes singleton energy for the pixel.
   * 
   * @param i pixel row.
   * @param j pixel column.
   * @param label pixel label.
   * @return singleton energy.
   */
  private Double singleton(Integer i, Integer j, Integer label) {
    Double m = this.classes.get(label).getMean() * 255;
    Double s = this.classes.get(label).getStDev() * 255;
    Double val = ((Short) this.originalRaster[i][j]).doubleValue();
    // return log(sqrt(2.0*3.141592653589793*variance[label])) +
    // pow((double)in_image_data[i][j]-mean[label],2)/(2.0*variance[label]);
    return Math.log(Math.sqrt(2.0 * Math.PI * s)) + Math.pow(val - m, 2) / (2.0 * s);
  }

  /**
   * Computes singleton energy for the pixel in splitMove.
   * 
   * @param i pixel row.
   * @param j pixel column.
   * @param label pixel label.
   * @return singleton energy.
   */

  private Double splitSingleton(Integer i, Integer j, Integer label) {
    Double m = this.splitClasses.get(label).getMean() * 255;
    Double s = this.splitClasses.get(label).getStDev() * 255;
    Double val = ((Short) this.originalRaster[i][j]).doubleValue();
    // return log(sqrt(2.0*3.141592653589793*variance[label])) +
    // pow((double)in_image_data[i][j]-mean[label],2)/(2.0*variance[label]);
    return Math.log(Math.sqrt(2.0 * Math.PI * s)) + Math.pow(val - m, 2) / (2.0 * s);
  }

  /**
   * Computes singleton energy for the pixel in mergeMove.
   * 
   * @param i pixel row.
   * @param j pixel column.
   * @param label pixel label.
   * @return singleton energy.
   */

  private Double mergeSingleton(Integer i, Integer j, Integer label) {
    Double m = this.splitClasses.get(label).getMean() * 255;
    Double s = this.splitClasses.get(label).getStDev() * 255;
    Double val = ((Short) this.originalRaster[i][j]).doubleValue();
    // return log(sqrt(2.0*3.141592653589793*variance[label])) +
    // pow((double)in_image_data[i][j]-mean[label],2)/(2.0*variance[label]);
    return Math.log(Math.sqrt(2.0 * Math.PI * s)) + Math.pow(val - m, 2) / (2.0 * s);
  }

  /**
   * Computes doubleton energy for the pixel.
   * 
   * @param i pixel row.
   * @param j pixel column.
   * @param label pixel label.
   * @return doubleton energy.
   */
  private Double doubleton(int i, int j, int label) {
    Integer height = this.raster.length;
    Integer width = this.raster[0].length;

    double energy = 0.0;

    if (i != height - 1) // south
    {
      if (label == this.labels[i + 1][j])
        energy -= beta;
      else
        energy += beta;
    }
    if (j != width - 1) // east
    {
      if (label == this.labels[i][j + 1])
        energy -= beta;
      else
        energy += beta;
    }
    if (i != 0) // nord
    {
      if (label == this.labels[i - 1][j])
        energy -= beta;
      else
        energy += beta;
    }
    if (j != 0) // west
    {
      if (label == this.labels[i][j - 1])
        energy -= beta;
      else
        energy += beta;
    }
    return energy;
  }

  /**
   * Computes doubleton energy for the pixel in Split Move.
   * 
   * @param i pixel row.
   * @param j pixel column.
   * @param label pixel label.
   * @return doubleton energy.
   */
  private Double splitDoubleton(int i, int j, int label) {
    Integer height = this.raster.length;
    Integer width = this.raster[0].length;

    double energy = 0.0;

    if (i != height - 1) // south
    {
      if (label == this.splitLabels[i + 1][j])
        energy -= beta;
      else
        energy += beta;
    }
    if (j != width - 1) // east
    {
      if (label == this.splitLabels[i][j + 1])
        energy -= beta;
      else
        energy += beta;
    }
    if (i != 0) // nord
    {
      if (label == this.splitLabels[i - 1][j])
        energy -= beta;
      else
        energy += beta;
    }
    if (j != 0) // west
    {
      if (label == this.splitLabels[i][j - 1])
        energy -= beta;
      else
        energy += beta;
    }
    return energy;
  }

  /**
   * Computes doubleton energy for the pixel in Merge Move.
   * 
   * @param i pixel row.
   * @param j pixel column.
   * @param label pixel label.
   * @return doubleton energy.
   */
  private Double mergeDoubleton(int i, int j, int label) {
    Integer height = this.raster.length;
    Integer width = this.raster[0].length;

    double energy = 0.0;

    if (i != height - 1) // south
    {
      if (label == this.mergeLabels[i + 1][j])
        energy -= beta;
      else
        energy += beta;
    }
    if (j != width - 1) // east
    {
      if (label == this.mergeLabels[i][j + 1])
        energy -= beta;
      else
        energy += beta;
    }
    if (i != 0) // nord
    {
      if (label == this.mergeLabels[i - 1][j])
        energy -= beta;
      else
        energy += beta;
    }
    if (j != 0) // west
    {
      if (label == this.mergeLabels[i][j - 1])
        energy -= beta;
      else
        energy += beta;
    }
    return energy;
  }

  /**
   * Returns computed local energy for the pixel.
   * 
   * @param i row of the image.
   * @param j column of the image.
   * @param label label specified.
   * @return energy.
   */
  private Double getLocalEnergy(Integer i, Integer j, Integer label) {
    return singleton(i, j, label) + doubleton(i, j, label);
  }

  /**
   * Returns computed local energy for the pixel.
   * 
   * @param i row of the image.
   * @param j column of the image.
   * @param label label specified.
   * @return energy.
   */
  private Double getSplitLocalEnergy(Integer i, Integer j, Integer label) {
    return splitSingleton(i, j, label) + splitDoubleton(i, j, label);
  }

  /**
   * Calculates global energy of the image.
   * 
   * @return global energy.
   */
  private Double getGlobalEnergy() {
    Integer height = this.raster.length;
    Integer width = this.raster[0].length;
    Double singletons = 0.0;
    Double doubletons = 0.0;
    for (int i = 0; i < height; ++i)
      for (int j = 0; j < width; ++j) {
        Integer cls = this.labels[i][j];
        singletons += singleton(i, j, cls);
        doubletons += doubleton(i, j, cls);
      }
    return singletons + doubletons / 2;
  }

  /**
   * Runs over the existed labeling and relabels all pixels using Gibbs sampler that samples from
   * the all available labels( set omega ). Simulated Annealing used as the criteria to keep new
   * labeling.
   * 
   */
  public void gibbsSampler(boolean print) {

    Long startTime = System.currentTimeMillis();

    if (print) {
      setChanged();
      notifyObservers("Starting Gibbs samplerGibbs sampler with " + this.classes.size()
          + " classes.");
    }
    resegmentImage();
    setChanged();
    notifyObservers(ImageFactoryStatus.NEW_SEGMENTATION);

    Integer height = this.raster.length;
    Integer width = this.raster[0].length;
    // make instance of random number generator
    RandomDataImpl randGen = new RandomDataImpl();
    // storage
    Double[] classE = new Double[this.classes.size() + 1];

    Double deltaEnergy = 10000D;
    Double deltaEnergyMin = 0.01;
    Double currentEnergy = 0D;
    Double oldEnergy = 0D;

    Double temp = this.startTemp;
    Double tempRate = this.coolingRate;

    Double sumEnergy = 0D;

    Integer iterationsCounter = 0;
    while ((deltaEnergy > deltaEnergyMin) && (iterationsCounter < 20000)) {

      deltaEnergy = 0D;

      for (int i = 0; i < height; ++i) {
        for (int j = 0; j < width; ++j) {

          sumEnergy = 0D;
          for (Integer cls : this.classes.keySet()) {
            classE[cls] = Math.exp(-getLocalEnergy(i, j, cls) / temp);
            sumEnergy = sumEnergy + classE[cls];
          }

          Double r = randGen.nextUniform(0D, 1D); // r is a uniform random number
          Double z = 0.0;
          for (Integer cls : classes.keySet()) {
            z = z + classE[cls] / sumEnergy;
            if (z > r) // choose new label with probability exp(-U/T).
            {
              this.labels[i][j] = cls;
              break;
            }
          }
        }
      }
      currentEnergy = getGlobalEnergy();
      deltaEnergy = Math.abs(oldEnergy - currentEnergy);
      oldEnergy = currentEnergy;

      temp = temp * tempRate; // decrease temperature

      iterationsCounter++;
      if (print) {
        setChanged();
        notifyObservers("Iteration: " + iterationsCounter + " T: " + temp + " energy: "
            + currentEnergy + "E delta: " + deltaEnergy + "\n");
      }
      this.image = generateSegmentedImage();
      setChanged();
      notifyObservers(ImageFactoryStatus.NEW_SEGMENTATION);

      if (this.stopSimulation) {
        iterationsCounter = 100000;
      }
    }
    Long endTime = System.currentTimeMillis();
    if (print) {
      setChanged();
      notifyObservers("Gibbs sampler finished at iteration " + iterationsCounter + " with energy: "
          + currentEnergy + ".\n");
      Long totalTime = endTime - startTime;
      Integer hours = Math.round(totalTime / 3600000);
      Integer minutes = Math.round((totalTime - 3600000 * hours) / 60000);
      Integer seconds = Math.round((totalTime - 3600000 * hours - 60000 * minutes) / 1000);

      setChanged();
      notifyObservers("Total time consumed " + hours + " hours, " + minutes + " min., " + seconds
          + " sec.\n");
    }

  }

  public void metropolisSampler() {
    Long startTime = System.currentTimeMillis();
    Integer height = this.raster.length;
    Integer width = this.raster[0].length;
    Integer no_regions = this.classes.keySet().size();
    // make instance of random number generator
    RandomDataImpl randGen = new RandomDataImpl();

    Double temp = this.startTemp;
    Double tempRate = this.coolingRate;

    Double oldEnergy = 0D;
    Double deltaEnergy = 10000D;
    Double deltaEnergyMin = 0.01;
    Double currentEnergy = getGlobalEnergy();

    Integer iterationsCounter = 0;
    while ((deltaEnergy > deltaEnergyMin) && (iterationsCounter < 20000)) {// stop when energy
      // change is
      // small

      deltaEnergy = 0D;
      for (int i = 0; i < height; ++i) {
        for (int j = 0; j < width; ++j) {

          Double kszi;
          Integer r;
          // Generate a new label different from the current one with uniform distribution.
          if (no_regions == 2)
            r = 1 - this.labels[i][j];
          else {
            Double rnd = randGen.nextUniform(0D, 1D);
            r = (this.labels[i][j] + ((Double) (rnd * (no_regions - 1))).intValue() + 1)
                % no_regions;
            rnd = randGen.nextUniform(0D, 1D);
            kszi = Math.log(rnd);
            /*
             * Accept the new label according to Metropolis dynamics.
             */
            if (kszi <= (getLocalEnergy(i, j, this.labels[i][j]) - getLocalEnergy(i, j, r)) / temp) {
              deltaEnergy = deltaEnergy
                  + Math.abs(getLocalEnergy(i, j, r) - getLocalEnergy(i, j, labels[i][j]));
              oldEnergy = currentEnergy - getLocalEnergy(i, j, labels[i][j])
                  + getLocalEnergy(i, j, r);
              labels[i][j] = r;
              currentEnergy = oldEnergy;
            }
          }
        }
      }

      temp = temp * tempRate; // decrease temperature

      iterationsCounter++;
      setChanged();
      notifyObservers("Iteration: " + iterationsCounter + " T: " + temp + " energy: "
          + currentEnergy + "E delta: " + deltaEnergy + "\n");
      this.image = generateSegmentedImage();
      setChanged();
      notifyObservers(ImageFactoryStatus.NEW_SEGMENTATION);

      if (this.stopSimulation) {
        iterationsCounter = 100000;
      }

    }// while
    setChanged();
    notifyObservers("Metropolis sampler finished at iteration " + iterationsCounter
        + " with energy: " + currentEnergy + ".");
    Long endTime = System.currentTimeMillis();
    Long totalTime = endTime - startTime;
    Integer hours = Math.round(totalTime / 3600000);
    Integer minutes = Math.round((totalTime - 3600000 * hours) / 60000);
    Integer seconds = Math.round((totalTime - 3600000 * hours - 60000 * minutes) / 1000);

    setChanged();
    notifyObservers("Total time consumed " + hours + " hours, " + minutes + " min., " + seconds
        + " sec.\n");

  }

  public void icm() {
    Long startTime = System.currentTimeMillis();
    Integer height = this.raster.length;
    Integer width = this.raster[0].length;

    Double temp = this.startTemp;
    Double tempRate = this.coolingRate;

    Double oldEnergy = 0D;
    Double currentEnergy = 0D;
    Double deltaEnergy = 10000D;
    Double deltaEnergyMin = 0.01;

    Integer iterationsCounter = 0;
    while ((deltaEnergy > deltaEnergyMin) && (iterationsCounter < 20000)) {// stop when energy
      // change is
      // small

      for (int i = 0; i < height; ++i) {
        for (int j = 0; j < width; ++j) {
          for (Integer cls : this.classes.keySet()) {
            if (getLocalEnergy(i, j, labels[i][j]) > getLocalEnergy(i, j, cls)) {
              labels[i][j] = cls;
            }
          }
        }
      }
      currentEnergy = getGlobalEnergy();
      deltaEnergy = Math.abs(oldEnergy - currentEnergy);
      oldEnergy = currentEnergy;

      temp = temp * tempRate; // decrease temperature

      iterationsCounter++;

      this.image = generateSegmentedImage();

      setChanged();
      notifyObservers("Iteration: " + iterationsCounter + " T: " + temp + " energy: "
          + currentEnergy + "E delta: " + deltaEnergy + "\n");

      setChanged();
      notifyObservers(ImageFactoryStatus.NEW_SEGMENTATION);
    }
    setChanged();
    notifyObservers("ICM algorithm finished at " + iterationsCounter + " iteration, T: " + temp
        + " energy: " + currentEnergy + ".");
    Long endTime = System.currentTimeMillis();
    Long totalTime = endTime - startTime;
    Integer hours = Math.round(totalTime / 3600000);
    Integer minutes = Math.round((totalTime - 3600000 * hours) / 60000);
    Integer seconds = Math.round((totalTime - 3600000 * hours - 60000 * minutes) / 1000);

    setChanged();
    notifyObservers("Total time consumed " + hours + " hours, " + minutes + " min., " + seconds
        + " sec.\n");

    if (this.stopSimulation) {
      iterationsCounter = 100000;
    }

  }

  public void run() {
    this.stopSimulation = false;
    if (this.method.equalsIgnoreCase("icm")) {
      icm();
    }
    else if (this.method.equalsIgnoreCase("metropolis")) {
      metropolisSampler();
    }
    else if (this.method.equalsIgnoreCase("gibbs")) {
      gibbsSampler(true);
    }
    else if (this.method.equalsIgnoreCase("rjmcmc")) {
      rjmcmcSampler();
    }
  }

  private void rjmcmcSampler() {

    resegmentImage();
    // collect useful variables
    Integer height = this.raster.length;
    Integer width = this.raster[0].length;
    Integer no_regions = this.classes.keySet().size();
    // make instance of random number generator
    RandomDataImpl randGen = new RandomDataImpl();

    Double temp = this.startTemp;
    Double tempRate = this.coolingRate;

    Double oldEnergy = 0D;
    Double deltaEnergy = 10000D;
    Double deltaEnergyMin = 0.01;

    Integer iterationsCounter = 0;
    // stop when energy change is small
    while ((deltaEnergy > deltaEnergyMin) && (iterationsCounter < 20000)) {// stop when energy

      deltaEnergy = 0D;
      Double currentEnergy = getGlobalEnergy();

      // ######## MOVE 1.
      // having current segmentation in the classes Map go with the first move - Gaussian
      // segmentation
      rjmcmc2_1SampleOmega();

      // ######## MOVE 2.
      // we choose class to split by uniform distribution.
      Double rnd = randGen.nextUniform(0D, 0.99999D);
      Double PSplitSelect = 1D / ((Integer) this.classes.size()).doubleValue();
      Integer class2Split = ((Double) (rnd / PSplitSelect)).intValue();

      // we choose classes by minimal Minkovsky distance
      Integer class2Merge1 = 0;
      Integer class2Merge2 = 1;
      Double PMergeSelect = getMergeCandidate(class2Merge1, class2Merge2);

      // probabilities of merge and split
      Double pSplit = 0D;
      Double pMerge = 0D;

      if (this.classes.size() == this.minClasses) {
        pSplit = 1D;
        pMerge = 0D;
      }
      else if (this.classes.size() == this.maxClasses) {
        pSplit = 0D;
        pMerge = 1D;
      }
      else {// here we need to decide which way to choose
        pSplit = 0.5;
        pMerge = 0.5;
      }
      //
      // now we have set up probabilities and having split and merge classes ready - let's do the
      // job, first calculate acceptance probability.
      //
      // Generate U random parameters

      // generating sets of random variables and getting parameters for the + class
      // BETA RANDOM FUNCTION NEED TO BE IMPLEMENTED
      Double u1 = 0D, u1_beta = 0D, u2 = 0D, u2_beta = 0D, u3 = 0D, u3_beta = 0D;
      try {
        u1 = randGen.nextUniform(0D, 1D);
        u1_beta = Beta.regularizedBeta(randGen.nextUniform(0D, 1D), 1.1, 1.1);

        u2 = randGen.nextUniform(0D, 1D);
        u2_beta = Beta.regularizedBeta(randGen.nextUniform(0D, 1D), 1.1, 1.1);

        u3 = randGen.nextUniform(0D, 1D);
        u3_beta = Beta.regularizedBeta(randGen.nextUniform(0D, 1D), 1.1, 1.1);
      }
      catch (MathException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      if ((pSplit > 0D) && (pMerge > 0D)) {

        IClass mergeClass = rjmcmcSelect2Merge(this.classes.get(class2Merge1), this.classes
            .get(class2Merge2));
        ArrayList<IClass> splitClasses = rjmcmcSelect2Split(class2Split, u1_beta, u2_beta, u3_beta);

        rjmcmcDoSplit(class2Split, splitClasses, randGen);
        rjmcmcDoMerge(mergeClass, class2Merge1, class2Merge2);

        Double mergeEnergy = getGlobalMergeEnergy();
        Double splitEnergy = getGlobalSplitEnergy();

        if (mergeEnergy < splitEnergy) {
          rjmcmcKeepMerge();
          setChanged();
          notifyObservers("Keeping MERGE\n");
        }
        else {
          rjmcmcKeepSplit();
          setChanged();
          notifyObservers("Keeping SPLIT\n");
        }

      }
      else if ((pSplit > 0D) && (pMerge == 0D)) {
        ArrayList<IClass> splitClasses = rjmcmcSelect2Split(class2Split, u1_beta, u2_beta, u3_beta);
        rjmcmcDoSplit(class2Split, splitClasses, randGen);
        rjmcmcKeepSplit();
        setChanged();
        notifyObservers("Keeping SPLIT\n");
      }
      else {
        IClass mergeClass = rjmcmcSelect2Merge(this.classes.get(class2Merge1), this.classes
            .get(class2Merge2));
        rjmcmcDoMerge(mergeClass, class2Merge1, class2Merge2);
        rjmcmcKeepMerge();
        setChanged();
        notifyObservers("Keeping MERGE\n");
      }

      currentEnergy = getGlobalEnergy();
      deltaEnergy = Math.abs(oldEnergy - currentEnergy);
      oldEnergy = currentEnergy;

      temp = temp * tempRate; // decrease temperature

      iterationsCounter++;
      setChanged();
      notifyObservers("Itr: " + iterationsCounter + ", classes: " + this.classes.size() + ", T: "
          + temp + " energy: " + currentEnergy + "E delta: " + deltaEnergy + "\n");
      this.image = generateSegmentedImage();
      setChanged();
      notifyObservers(ImageFactoryStatus.NEW_SEGMENTATION);

      if (this.stopSimulation) {
        iterationsCounter = 100000;
      }

    }// while
  }

  private void rjmcmcKeepMerge() {
    Integer height = this.raster.length;
    Integer width = this.raster[0].length;
    this.classes.clear();
    for (Integer cls : this.mergeClasses.keySet()) {
      this.classes.put(cls, this.mergeClasses.get(cls));
    }
    // assign labels
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        this.labels[i][j] = this.mergeLabels[i][j];
      }
    }
    resegmentImage();
  }

  private void rjmcmcKeepSplit() {
    Integer height = this.raster.length;
    Integer width = this.raster[0].length;
    this.classes.clear();
    for (Integer cls : this.splitClasses.keySet()) {
      this.classes.put(cls, this.splitClasses.get(cls));
    }
    // assign labels
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        this.labels[i][j] = this.splitLabels[i][j];
      }
    }
    resegmentImage();
  }

  private void rjmcmcDoMerge(IClass mergeClass, Integer class2Merge1, Integer class2Merge2) {
    Integer height = this.raster.length;
    Integer width = this.raster[0].length;
    if (null == this.mergeClasses) {
      this.mergeClasses = new TreeMap<Integer, IClass>();
    }
    else {
      this.mergeClasses.clear();
    }
    Integer classNum = 0;
    Integer newMergeClassLabel = 0;
    TreeMap<Integer, Integer> labelsReMapping = new TreeMap<Integer, Integer>();

    for (Integer cls : this.classes.keySet()) {
      if (class2Merge1.equals(cls)) {
        // this.mergeClasses.remove(class2Merge1);
        this.mergeClasses.put(classNum, mergeClass);
        newMergeClassLabel = classNum;
        classNum++;
      }
      else if (class2Merge2.equals(cls)) {
        assert true;
      }
      else {
        this.mergeClasses.put(classNum, this.classes.get(cls));
        labelsReMapping.put(cls, classNum);
        classNum++;
      }
    }

    // assign labels
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        Integer cLabel = this.labels[i][j];
        if ((class2Merge1.equals(cLabel)) || (class2Merge2.equals(cLabel))) {
          this.mergeLabels[i][j] = newMergeClassLabel;
        }
        else {
          this.mergeLabels[i][j] = labelsReMapping.get(cLabel);
        }
      }
    }

  }

  private void rjmcmcDoSplit(Integer class2Split, ArrayList<IClass> splitClasses,
      RandomDataImpl randGen) {
    // private short[][] splitRaster;
    // private Integer[][] splitLabels;
    // private TreeMap<Integer, IClass> splitClasses;
    if (null == this.splitClasses) {
      this.splitClasses = new TreeMap<Integer, IClass>();
    }
    else {
      this.splitClasses.clear();
    }
    Integer label1 = class2Split;
    for (Integer cls : this.classes.keySet()) {
      if (label1.equals(cls)) {
        this.splitClasses.put(label1, splitClasses.get(0));
      }
      else {
        this.splitClasses.put(cls, this.classes.get(cls));
      }
    }
    Integer label2 = this.splitClasses.size();
    this.splitClasses.put(label2, splitClasses.get(1));

    Integer height = this.raster.length;
    Integer width = this.raster[0].length;

    // assign labels randomly
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        if (class2Split.equals(this.labels[i][j])) {
          if (randGen.nextUniform(0D, 1D) > 0.5) {
            this.splitLabels[i][j] = label1;
          }
          else {
            this.splitLabels[i][j] = label2;
          }
        }
        else {
          this.splitLabels[i][j] = this.labels[i][j];
        }
      }
    }

    // run ICM algorithm to fix the stuff
    boolean inLoop = true;
    Integer counter = 0;
    while (inLoop) {
      for (int i = 0; i < height; i++) {
        for (int j = 0; j < width; j++) {
          for (Integer cls : this.splitClasses.keySet()) {
            if (cls.equals(label1) || cls.equals(label2))
              if (getSplitLocalEnergy(i, j, this.splitLabels[i][j]) > getSplitLocalEnergy(i, j, cls)) {
                this.splitLabels[i][j] = cls;
              }
          }
        }
      }
      counter++;
      if (counter > 5) {
        inLoop = false;
      }
    }

  }

  /**
   * Splits provided class by two using provided parameters.
   * 
   * @param class2Split class to split.
   * @param u1_beta random parameter u1.
   * @param u2_beta random parameter u2.
   * @param u3_beta random parameter u3.
   * @return resulting splitted classes.
   */
  private ArrayList<IClass> rjmcmcSelect2Split(Integer class2Split, Double u1_beta, Double u2_beta,
      Double u3_beta) {

    setChanged();
    notifyObservers("######## Class to split:" + class2Split + " this.classes size "
        + this.classes.size() + " " + this.classes.keySet());

    Double m_old = this.classes.get(class2Split).getMean();
    Double l_old = this.classes.get(class2Split).getStDev();
    // generating sets of random variables and getting parameters for the + class
    Double p_lambda1 = l_old * u1_beta;
    Double p_lambda2 = l_old * (1 - u1_beta);

    Double m_lambda1 = m_old + u2_beta * Math.sqrt(l_old * ((1 - u1_beta) / u1_beta));
    Double m_lambda2 = m_old - u2_beta * Math.sqrt(l_old * (u1_beta / (1 - u1_beta)));

    Double l_lambda1 = u3_beta * (1 - u2_beta * u2_beta) * l_old * (1 / u1_beta);
    Double l_lambda2 = (1 - u3_beta) * (1 - u2_beta * u2_beta) * l_old * (1 / u1_beta);

    ArrayList<IClass> res = new ArrayList<IClass>();
    // public IClass(Double mean, Double stdev, Double weight) {
    res.add(new IClass(m_lambda1, l_lambda1, p_lambda1));
    res.add(new IClass(m_lambda2, l_lambda2, p_lambda2));

    return res;

  }

  private void rjmcmc2_1SampleOmega() {
    resegmentImage();
    gibbsSampler(false);
  }

  private Double getMergeCandidate(Integer minClass1, Integer minClass2) {
    // we are choosing merging classes probability using calculations according Kato article
    // (5.17)
    Double distanceSum = 0D;
    for (Integer cls : this.classes.keySet()) {
      for (int c = 0; c < this.classes.size(); c++) {
        if (!cls.equals(c)) {
          distanceSum += mahalanobisDistance(cls, c);
        }
      }
    }// so we have calculated the sum of all distances
    // go find the minimal pair.
    Double tmpDist = mahalanobisDistance(minClass1, minClass2);
    Double minDist = tmpDist;
    Double prob = 0D;
    for (Integer cls : this.classes.keySet()) {
      for (int c = 0; c < this.classes.size(); c++) {
        if (!cls.equals(c)) {
          tmpDist = mahalanobisDistance(cls, c);
          if (tmpDist < minDist) {
            minDist = tmpDist;
            minClass1 = cls;
            minClass2 = c;
            prob = tmpDist / distanceSum;
          }
        }
      }
    }// minimal pair search finished.

    return prob;
  }

  private Double ProbabilityOfReallocation(Integer underSplitting, ArrayList<IClass> splitClasses) {

    Integer height = this.raster.length;
    Integer width = this.raster[0].length;

    Double currentP1 = 1D;
    Double currentP2 = 1D;

    Integer[][] tempLabels = new Integer[height][width];

    Double[][] probHolder = new Double[height][width];

    TreeMap<Integer, IClass> tmpClasses = new TreeMap<Integer, IClass>();

    // extracting only splitted class
    for (Integer i : this.classes.keySet()) {
      if (!i.equals(underSplitting)) {
        tmpClasses.put(i, this.classes.get(i));
      }
    }
    Integer label1 = underSplitting;
    Integer label2 = tmpClasses.size();
    tmpClasses.put(label1, splitClasses.get(0));
    tmpClasses.put(label2, splitClasses.get(1));

    // resegment image using new set of classes
    for (int i = 0; i < height; ++i) {
      for (int j = 0; j < width; ++j) {
        for (Integer c : tmpClasses.keySet()) {
          IClass cls = tmpClasses.get(c);
          Double pixel = this.originalRaster[i][j] / 255D;
          Double p = cls.getPValue(pixel);
          if (null == probHolder[i][j]) {
            tempLabels[i][j] = c;
            probHolder[i][j] = p;
          }
          else if (probHolder[i][j] < p) {
            tempLabels[i][j] = c;
            probHolder[i][j] = p;
          }
        }// integer c

      }// i
    }// j

    // we will need energy of the splitted field.
    Double energy = 0.0;
    for (int i = 0; i < height; ++i) {
      for (int j = 0; j < width; ++j) {
        Integer cls = tempLabels[i][j];
        Double e = 0D;
        if (cls.equals(label1) || cls.equals(label2)) {
          if (i != height - 1) // south
          {
            if (cls == tempLabels[i + 1][j])
              e -= beta;
            else
              e += beta;
          }
          if (j != width - 1) // east
          {
            if (cls == tempLabels[i][j + 1])
              e -= beta;
            else
              e += beta;
          }
          if (i != 0) // nord
          {
            if (cls == tempLabels[i - 1][j])
              e -= beta;
            else
              e += beta;
          }
          if (j != 0) // west
          {
            if (cls == this.labels[i][j - 1])
              e -= beta;
            else
              e += beta;
          }
        }// if cls==...
        energy += e;
      }// i
    }// j
    // ############################
    // now calculate probabilities
    Double pRealloc1 = 0D;
    Double pRealloc2 = 0D;
    for (int i = 0; i < height; ++i) {
      for (int j = 0; j < width; ++j) {
        if (labels[i][j].equals(label1) || labels[i][j].equals(label2)) {
          if (labels[i][j].equals(label1)) {
            IClass wPlus = tmpClasses.get(label1);
          }
          else {
            IClass wPlus = tmpClasses.get(label2);
          }
          Double m = tmpClasses.get(label1).getMean();
          Double s = tmpClasses.get(label1).getStDev();
          Double p = tmpClasses.get(label1).getWeight();
          Double f = ((Short) this.originalRaster[i][j]).doubleValue() / 255;

          Double part1 = 1 / (Math.sqrt(Math.pow(2 * Math.PI, 3) * s));
          part1 = part1 * Math.exp(-1 / 2 * (f - m) * (1 / s) * (f - m));
          Double part2 = p * Math.exp(-energy);
          pRealloc1 *= part1;
          pRealloc2 *= part2;
        }
      }// i
    }// j
    return pRealloc1 * pRealloc2;
  }

  private IClass rjmcmcSelect2Merge(IClass class1, IClass class2) {
    Double newMean = (class1.getMean() + class2.getMean()) / 2;
    Double newSigma = (class1.getStDev() + class2.getStDev()) / 2;
    Double newP = class1.getWeight() + class2.getWeight();
    // public IClass(Double mean, Double stdev, Double weight) {
    return new IClass(newMean, newSigma, newP);
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public Double mahalanobisDistance(Integer i, Integer j) {
    IClass class1 = this.classes.get(i);
    IClass class2 = this.classes.get(j);
    Double part1 = (class1.getMean() - class2.getMean()) * (1 / class1.getStDev())
        * (class1.getMean() - class2.getMean());
    Double part2 = (class2.getMean() - class1.getMean()) * (1 / class2.getStDev())
        * (class2.getMean() - class1.getMean());

    return part1 + part2;
  }

  /**
   * Sets starting temperature.
   * 
   * @param startTemperature temperature.
   */
  public void setStartTemperature(Double startTemperature) {
    this.startTemp = startTemperature;
  }

  /**
   * Sets cooling schedule.
   * 
   * @param temperatureSchedule cooling rate.
   */
  public void setCoolingSchedule(Double temperatureSchedule) {
    this.coolingRate = temperatureSchedule;

  }

  /**
   * Reports starting temperature.
   * 
   * @return starting temperature.
   */
  public Double getStartTemperature() {
    return this.startTemp;
  }

  /**
   * Reports cooling rate.
   * 
   * @return cooling rate.
   */
  public Double getCoolingRate() {
    return this.coolingRate;
  }

  public void setClasses(TreeMap<Integer, IClass> classes) {
    this.classes = classes;
    resegmentImage();
    this.image = generateSegmentedImage();
    setChanged();
    notifyObservers(ImageFactoryStatus.NEW_SEGMENTATION);
  }

  public RenderedImage getCurrentBufferedImage() {
    return this.image;
  }

  /**
   * Calculates global energy of the Splitted image .
   * 
   * @return global energy.
   */
  private Double getGlobalSplitEnergy() {
    Integer height = this.raster.length;
    Integer width = this.raster[0].length;
    Double singletons = 0.0;
    Double doubletons = 0.0;
    for (int i = 0; i < height; ++i)
      for (int j = 0; j < width; ++j) {
        Integer cls = this.splitLabels[i][j];
        singletons += splitSingleton(i, j, cls);
        doubletons += splitDoubleton(i, j, cls);
      }
    return singletons + doubletons / 2;
  }

  /**
   * Calculates global energy of the Merged image .
   * 
   * @return global energy.
   */
  private Double getGlobalMergeEnergy() {
    Integer height = this.raster.length;
    Integer width = this.raster[0].length;
    Double singletons = 0.0;
    Double doubletons = 0.0;
    for (int i = 0; i < height; ++i)
      for (int j = 0; j < width; ++j) {
        Integer cls = this.mergeLabels[i][j];
        singletons += mergeSingleton(i, j, cls);
        doubletons += mergeDoubleton(i, j, cls);
      }
    return singletons + doubletons / 2;
  }

  public void update(Observable o, Object arg) {
    if ((o instanceof Controller) && (arg instanceof String)) {
      if ("stop".equalsIgnoreCase((String) arg)) {
        this.stopSimulation = true;
      }
    }

  }

  /**
   * Sets beta parameter.
   * 
   * @param beta2 value to set.
   */
  public void setBeta(Double beta2) {
    this.beta = beta2;
  }

}
