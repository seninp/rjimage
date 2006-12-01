package edu.hawaii.senin.rjimage.model;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Observable;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.apache.commons.math.MathException;
import org.apache.commons.math.random.RandomDataImpl;
import org.apache.commons.math.special.Beta;

/**
 * This is the main class for the Reversible Jump Image Segmentation. The work based on the Zoltan
 * Kato articles concerning image segmentation problem.
 * 
 * @author Pavel Senin.
 * 
 */
public class ImageFactory extends Observable implements Runnable {
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
  private short[][] original_raster;
  /**
   * Holds image that in the process of segmentation.
   */
  private BufferedImage currentImage;
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
  private Double beta = 2.5D;
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
    this.classes.put(Integer.valueOf(0), new IClass(0.2, 0.1, 0.2));
    this.classes.put(Integer.valueOf(1), new IClass(0.3, 0.1, 0.2));
    this.classes.put(Integer.valueOf(2), new IClass(0.4, 0.1, 0.2));
    this.classes.put(Integer.valueOf(3), new IClass(0.5, 0.1, 0.2));
    this.classes.put(Integer.valueOf(4), new IClass(0.6, 0.1, 0.2));
    this.classes.put(Integer.valueOf(5), new IClass(0.7, 0.1, 0.2));
    this.classes.put(Integer.valueOf(6), new IClass(0.9, 0.1, 0.2));

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
      if (imageType == BufferedImage.TYPE_BYTE_GRAY) {
        this.currentImage = null;
        this.original_raster = null;
        this.raster = null;
        this.labels = null;
        this.classes = new TreeMap<Integer, IClass>();
        resetSegmentation();
      }
      else {
        this.originalImage = toGrayScale(this.originalImage);
        setChanged();
        notifyObservers(ImageFactoryStatus.INVALID_IMAGE);
      }
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
    this.labels = new Integer[height][width];
    this.raster = new short[height][width];
    this.original_raster = new short[height][width];

    byte[][] rasterIntermediate = new byte[height][width];
    short[][] rasterPlain = new short[height][width];

    Double[][] probHolder = new Double[height][width];

    // getting real raster
    for (int j = 0; j < width; j++) {
      raster.getDataElements(j, 0, 1, height, rasterIntermediate[j]);
    }
    // making it "short"
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        rasterPlain[i][j] = rasterIntermediate[i][j];
        rasterPlain[i][j] &= 0xff;// shift from "-128...+127" to "0...255"
        this.original_raster[i][j] = rasterPlain[i][j];
      }
    }
    //
    for (int i = 0; i < raster.getHeight(); i++) {
      for (int j = 0; j < raster.getWidth(); j++) {

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

    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        this.raster[i][j] = ((Double) Math
            .floor(this.classes.get(this.labels[i][j]).getMean() * 255)).shortValue();
      }
    }

  }

  public void resetSegmentation() {
    resetClasses();
    resegmentImage();
    this.currentImage = generateSegmentedImage();
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
    this.currentImage = generateSegmentedImage();
    return this.currentImage;
  }

  /**
   * Suppose to converts image to grayscale. Not tested yet.
   * 
   * @param image image to convert.
   * @return grayscale image.
   */
  public BufferedImage toGrayScale(BufferedImage image) {
    BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(),
        BufferedImage.TYPE_USHORT_GRAY);
    Graphics2D g = result.createGraphics();
    g.drawRenderedImage(image, null);
    g.dispose();

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
    Double m = this.classes.get(label).getMean();
    Double s = this.classes.get(label).getStDev();
    // return log(sqrt(2.0*3.141592653589793*variance[label])) +
    // pow((double)in_image_data[i][j]-mean[label],2)/(2.0*variance[label]);
    return Math.log(Math.sqrt(2.0 * Math.PI * s))
        + Math.pow(this.original_raster[i][j] / 255D - m, 2) / (2.0 * s);
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
  public void gibbsSampler() {

    resegmentImage();
    setChanged();
    notifyObservers(ImageFactoryStatus.NEW_SEGMENTATION);

    Integer height = this.raster.length;
    Integer width = this.raster[0].length;
    // make instance of random number generator
    RandomDataImpl randGen = new RandomDataImpl();
    // storage
    Double[] classE = new Double[this.classes.size()];

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
          for (Integer cls : classes.keySet()) {
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
      setChanged();
      notifyObservers("Iteration: " + iterationsCounter + " T: " + temp + " energy: "
          + currentEnergy + "E delta: " + deltaEnergy + "\n");
      this.currentImage = generateSegmentedImage();
      setChanged();
      notifyObservers(ImageFactoryStatus.NEW_SEGMENTATION);
    }
    setChanged();
    notifyObservers("Gibbs sampler finished at iteration " + iterationsCounter + " with energy: "
        + currentEnergy + ".");
  }

  public void metropolisSampler() {

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
            // if (!mmd) // Metropolis: kszi is a uniform random number
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
      this.currentImage = generateSegmentedImage();
      setChanged();
      notifyObservers(ImageFactoryStatus.NEW_SEGMENTATION);

    }// while
    setChanged();
    notifyObservers("Metropolis sampler finished at iteration " + iterationsCounter
        + " with energy: " + currentEnergy + ".");
  }

  public void icm() {

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
          for (Integer cls : classes.keySet()) {
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

      this.currentImage = generateSegmentedImage();

      setChanged();
      notifyObservers("Iteration: " + iterationsCounter + " T: " + temp + " energy: "
          + currentEnergy + "E delta: " + deltaEnergy + "\n");

      setChanged();
      notifyObservers(ImageFactoryStatus.NEW_SEGMENTATION);
    }
    setChanged();
    notifyObservers("ICM algorithm finished at " + iterationsCounter + " iteration, T: " + temp
        + " energy: " + currentEnergy + ".");

  }

  public void run() {
    if (this.method.equalsIgnoreCase("icm")) {
      icm();
    }
    else if (this.method.equalsIgnoreCase("metropolis")) {
      metropolisSampler();
    }
    else if (this.method.equalsIgnoreCase("gibbs")) {
      gibbsSampler();
    }
    else if (this.method.equalsIgnoreCase("rjmcmc")) {
      rjmcmcSampler();
    }
  }

  private void rjmcmcSampler() {

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
    while ((deltaEnergy > deltaEnergyMin) && (iterationsCounter < 20000)) {// stop when energy
      // change is
      // small

      deltaEnergy = 0D;
      Double currentEnergy = getGlobalEnergy();

      // ######## MOVE 1.
      // having current segmentation in the classes Map go with the first move - Gaussian
      // segmentation
      this.resegmentImage();

      // ######## MOVE 2.
      // check the deviation within segmentation
      Double rnd = randGen.nextUniform(0D, 1D);
      // we choose class to split by uniform distribution.
      Integer class2Split = ((Double) (rnd / (1 / ((Integer) this.classes.size()).doubleValue())))
          .intValue();
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
      Integer minClass1 = 0;
      Integer minClass2 = 1;
      Double tmpDist = mahalanobisDistance(minClass1, minClass2);
      Double minDist = tmpDist;
      for (Integer cls : this.classes.keySet()) {
        for (int c = 0; c < this.classes.size(); c++) {
          if (!cls.equals(c)) {
            tmpDist = mahalanobisDistance(cls, c);
            if (tmpDist < minDist) {
              minDist = tmpDist;
              minClass1 = cls;
              minClass2 = c;
            }
          }
        }
      }// minimal pair search finished.

      // probabilities of merge and split
      Double pSplit = 0D;
      Double pMerge = 0D;
      IClass underSplitting = null;
      ArrayList<IClass> splitClasses = null;

      if (this.classes.size() == this.minClasses) {
        underSplitting = this.classes.get(class2Split);
        splitClasses = doSplit(underSplitting);
        pSplit = 1D;
        pMerge = 0D;
      }
      else if (this.classes.size() == this.maxClasses) {
        IClass mergeClass = doMerge(this.classes.get(minClass1), this.classes.get(minClass2));
        pSplit = 0D;
        pMerge = 1D;
      }
      else {// here we need to decide which way to choose
        underSplitting = this.classes.get(class2Split);
        splitClasses = doSplit(underSplitting);
        IClass mergeClass = doMerge(this.classes.get(minClass1), this.classes.get(minClass2));
        pSplit = 0.5;
        pMerge = 0.5;
      }

      //
      // now we have set up probabilities and having split and merge classes ready - let's do the
      // job, first calculate acceptance probability.
      //
      if (pSplit > 0) {
        Double P_realloc = ProbabilityOfReallocation(class2Split, splitClasses);

      }
      temp = temp * tempRate; // decrease temperature

      iterationsCounter++;
      setChanged();
      notifyObservers("Iteration: " + iterationsCounter + " T: " + temp + " energy: "
          + currentEnergy + "E delta: " + deltaEnergy + "\n");
      this.currentImage = generateSegmentedImage();
      setChanged();
      notifyObservers(ImageFactoryStatus.NEW_SEGMENTATION);

    }// while
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
          Double pixel = this.original_raster[i][j] / 255D;
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
    //############################
    // now calculate probabilities
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
          Double f = ((Short) this.original_raster[i][j]).doubleValue() / 255;

          Double part1 = 1 / (Math.sqrt(Math.pow(2 * Math.PI, 3) * s));
          part1 = part1 * Math.exp(-1 / 2 * (f - m) * (1 / s) * (f - m));
          Double part2 = p*Math.exp(-energy);

        }
      }// i
    }// j

    return null;
  }

  private IClass doMerge(IClass class1, IClass class2) {
    Double newMean = (class1.getMean() + class2.getMean()) / 2;
    Double newSigma = (class1.getStDev() + class2.getStDev());
    Double newP = class1.getWeight() + class2.getWeight();
    // public IClass(Double mean, Double stdev, Double weight) {
    return new IClass(newMean, newSigma, newP);
  }

  /**
   * Does splitting IClass by two
   * 
   * @param underSplitting class to split by two.
   * @return classes that result splitting.
   */
  private ArrayList<IClass> doSplit(IClass underSplitting) {

    try {
      RandomDataImpl randGen = new RandomDataImpl();

      Double m_old = underSplitting.getMean();
      Double l_old = underSplitting.getStDev();
      // generating sets of random variables and getting parameters for the + class
      // BETA RANDOM FUNCTION NEED TO BE IMPLEMENTED
      Double u1 = Beta.regularizedBeta(randGen.nextUniform(0D, 1D), 1.1, 1.1);
      Double p_lambda1 = l_old * u1;
      Double p_lambda2 = l_old * (1 - u1);

      Double u2 = Beta.regularizedBeta(randGen.nextUniform(0D, 1D), 1.1, 1.1);
      Double m_lambda1 = m_old + u2 * Math.sqrt(l_old * ((1 - u1) / u1));
      Double m_lambda2 = m_old - u2 * Math.sqrt(l_old * (u1 / (1 - u1)));

      Double u3 = Beta.regularizedBeta(randGen.nextUniform(0D, 1D), 1.1, 1.1);
      Double l_lambda1 = u3 * (1 - u2 * u2) * l_old * (1 / u1);
      Double l_lambda2 = (1 - u3) * (1 - u2 * u2) * l_old * (1 / u1);

      ArrayList<IClass> res = new ArrayList<IClass>();
      // public IClass(Double mean, Double stdev, Double weight) {
      res.add(new IClass(m_lambda1, l_lambda1, p_lambda1));
      res.add(new IClass(m_lambda2, l_lambda2, p_lambda2));
      return res;
    }
    catch (MathException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;

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

}
