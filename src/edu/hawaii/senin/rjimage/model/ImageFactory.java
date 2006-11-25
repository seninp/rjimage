package edu.hawaii.senin.rjimage.model;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
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

/**
 * This is the main class for the Reversible Jump Image Segmentation. The work based on the Zoltan
 * Kato articles concerning image segmentation problem.
 * 
 * @author Pavel Senin.
 * 
 */
public class ImageFactory extends Observable {

  /**
   * Holds original image.
   */
  private BufferedImage originalImage;
  /**
   * Holds image that in the process of segmentation.
   */
  private BufferedImage currentImage;
  /**
   * File name holder.
   */
  private String imageFileName;
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
   * Holds raster USHORT type
   */
  private short raster[][];
  /**
   * Holds beta value.
   */
  private Double beta = 0.28D;

  /**
   * Resets labeling with initial values. See Kato99Bayesian explanation.
   * 
   */
  private void clearClasses() {
    if (this.classes.size() > 0) {
      this.classes.clear();
    }
    this.classes.add(new IClass(0.33, 0.3, 0.5));
    this.classes.add(new IClass(0.66, 0.3, 0.5));
  }

  /**
   * Instantiates image factory with clen parameters.
   * 
   */
  public ImageFactory() {
    this.originalImage = null;
    this.originalImage = null;
    this.segmentation = null;
    this.classes = new ArrayList<IClass>();
  }

  /**
   * Instantiates factory using provided path to image file.
   * 
   * @param imageFileName path to the image file.
   */

  public ImageFactory(String imageFileName) {
    this.imageFileName = imageFileName;
    this.originalImage = null;
    this.classes = new ArrayList<IClass>();
    try {
      originalImage = ImageIO.read(new File(imageFileName));
      // originalImage = convertToGrayscale(ImageIO.read(new File(imageFileName)));
      // int imageType = originalImage.getType();
      // int imageType = image.getType();
      currentImage = new BufferedImage(originalImage.getColorModel(), originalImage.getRaster(),
          originalImage.isAlphaPremultiplied(), null);
    }
    catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Instantiates factory from supplied file.
   * 
   * @param selectedFile file to load original image.
   */
  public void initFactory(File selectedFile) {
    this.originalImage = null;
    this.originalImage = null;
    this.classes = new ArrayList<IClass>();
    try {
      originalImage = ImageIO.read(selectedFile);
      int imageType = originalImage.getType();
      if (imageType != BufferedImage.TYPE_BYTE_GRAY) {
        setChanged();
        notifyObservers(ImageFactoryStatus.INVALID_IMAGE);
      }
      else {
        currentImage = new BufferedImage(originalImage.getColorModel(), originalImage.getRaster(),
            false, null);
      }
    }
    catch (IOException e) {
      // TODO Auto-generated catch block
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
   * Resegments image using given Gaussians.
   */
  public void resegmentImage() {
    this.segmentation = new Segmentation(this.currentImage, this.classes);
    this.labels = this.segmentation.getSegmentation();
  }

  public void RunSegmentation() {
    // clean all classes
    clearClasses();
    // resegment
    resegmentImage();
    // create new image
    this.currentImage = generateSegmentedImage();
    setChanged();
    notifyObservers(ImageFactoryStatus.NEW_SEGMENTATION);
  }

  private BufferedImage generateSegmentedImage() {

    Raster raster = this.originalImage.getRaster();
    Integer height = raster.getHeight();
    Integer width = raster.getWidth();

    byte[] rasterPlain = new byte[height * width];

    Integer increment = 255 / this.classes.size();

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        Integer value = this.labels[y][x] * increment;
        rasterPlain[y * width + x] = value.byteValue();
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

  public Image getCurrentImage() {
    return this.currentImage;
  }

  public BufferedImage toGrayScale(BufferedImage image) {
    BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(),
        BufferedImage.TYPE_USHORT_GRAY);
    Graphics2D g = result.createGraphics();
    g.drawRenderedImage(image, null);
    g.dispose();

    return result;
  }

  private Double getLocalEnergy(Integer x, Integer y, Integer label) {
    return Singleton(x, y, label) + Doubleton(x, y, label);
  }

  private Double Singleton(Integer x, Integer y, Integer label) {
    Double m = this.classes.get(label).getMean();
    Double s = this.classes.get(label).getStDev();

    return Math.log(Math.sqrt(2.0 * Math.PI * s) + Math.pow(this.raster[x][y] - m, 2)) / (2.0 * s);
  }

  private Double Doubleton(int x, int y, int label)
  {
    Integer height = this.raster.length;
    Integer width = this.raster[0].length;
    
    double energy = 0.0;

    if (y!=height-1) // south
      {
        if (label == this.labels[x][x]) energy -= beta;
        else energy += beta;
      }
    if (x!=width-1) // east
      {
        if (label == this.labels[][j+1]) energy -= beta;
        else energy += beta;
      }
    if (i!=0) // nord
      {
        if (label == classes[i-1][j]) energy -= beta;
        else energy += beta;
      }
    if (j!=0) // west
      {
        if (label == classes[i][j-1]) energy -= beta;
        else energy += beta;
      }
    return energy;
  }

}
