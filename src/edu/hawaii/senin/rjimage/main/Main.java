package edu.hawaii.senin.rjimage.main;

import edu.hawaii.senin.rjimage.controller.Controller;
import edu.hawaii.senin.rjimage.model.ImageFactory;
import edu.hawaii.senin.rjimage.view.View;

/**
 * The main program which creates the Model, View, and Controller for rjimage project.
 * 
 * @author Pavel Senin
 */
public class Main {

  /**
   * The main method, which instantiates the Model, View, and Controller.
   * 
   * @param args Ignored.
   */
  public static void main(String[] args) {
    ImageFactory imageFactory = new ImageFactory();
    View view = new View();
    new Controller(imageFactory, view);
  }

  /**
   * Make constructor private to prevent unintended instantiation.
   */
  private Main() {
    // Do nothing.
  }
}
