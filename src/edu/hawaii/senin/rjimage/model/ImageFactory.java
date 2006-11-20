package edu.hawaii.senin.rjimage.model;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Observable;

import javax.imageio.ImageIO;

import edu.hawaii.senin.rjimage.view.View;

public class ImageFactory extends Observable {

  private BufferedImage originalImage;
  private BufferedImage currentSegmentation;
  private String imageFileName;

  public ImageFactory(String imageFileName) {
    this.imageFileName = imageFileName;
    this.originalImage = null;
    try {
      originalImage = ImageIO.read(new File(imageFileName));
    }
    catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public ImageFactory() {
    this.originalImage = null;
    this.originalImage = null;
  }

  public void addObserver(View view) {
    // TODO Auto-generated method stub

  }

  public void RunSegmentation() {
    // TODO Auto-generated method stub

  }

  public void initFactory(File selectedFile) {
    this.imageFileName = selectedFile.getPath();
    this.originalImage = null;
    try {
      originalImage = ImageIO.read(selectedFile);
    }
    catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    this.notifyChange();
  }

  private void notifyChange() {
    setChanged();
    notifyObservers("fix");
  }
}
