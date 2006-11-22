package edu.hawaii.senin.rjimage.model;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;

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
   * Holds Gaussians for the image segmentation.
   */
  private ArrayList<IClass> classes;

  private Segmentation segmentation;

  private Integer[][] labels;

  /**
   * Resets labeling with initial values. See Kato99Bayesian explanation.
   * 
   */
  private void clearClasses() {
    if (this.classes.size() > 0) {
      this.classes.clear();
    }
    this.classes.add(new IClass(0.33, 0.33, 0.5));
    this.classes.add(new IClass(0.66, 0.66, 0.5));
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
      currentImage = new BufferedImage(originalImage.getColorModel(), originalImage.getRaster(),
          originalImage.isAlphaPremultiplied(), null);
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
    WritableRaster writableRaster = this.originalImage.getRaster();
    Integer increment = 255 / this.classes.size();
    Integer height = writableRaster.getHeight();
    Integer width = writableRaster.getWidth();
    double[] pixel = new double[1];
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        pixel[0] = (this.labels[i][j] + 1) * increment;
        writableRaster.setPixel(i, j, pixel);
      }
    }
    this.currentImage = new BufferedImage(originalImage.getColorModel(), writableRaster,
        originalImage.isAlphaPremultiplied(), null);
    setChanged();
    notifyObservers(ImageFactoryStatus.NEW_SEGMENTATION);

  }

  public Image getCurrentImage() {
    return this.currentImage;
  }

}
