package edu.hawaii.senin.rjimage.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JFileChooser;

import edu.hawaii.senin.rjimage.model.ImageFactory;
import edu.hawaii.senin.rjimage.utils.ImageFileView;
import edu.hawaii.senin.rjimage.utils.ImageFilter;
import edu.hawaii.senin.rjimage.utils.ImagePreview;
import edu.hawaii.senin.rjimage.view.View;

public class Controller {

  private ImageFactory imageFactory;

  private View view;

  private ArrayList<Thread> threads = new ArrayList<Thread>();

  public Controller(ImageFactory imageFactory, View view) {
    this.imageFactory = imageFactory;
    imageFactory.addObserver(view);
    this.view = view;
    view.addLoadListener(new ImageLoadListener());
    view.addICMListener(new RunICMListener());
    view.addGibbsListener(new RunGibbsListener());
    view.addMetropolisListener(new RunMetropolisListener());
    view.addRJListener(new RunRJListener());
    view.addStopSimulationListener(new StopSimulationListener());
    view.showGUI();
  }

  private class ImageLoadListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {

      JFileChooser fc = new JFileChooser();

      fc.addChoosableFileFilter(new ImageFilter());
      fc.setAcceptAllFileFilterUsed(false);

      // Add custom icons for file types.
      fc.setFileView(new ImageFileView());

      // Add the preview pane.
      fc.setAccessory(new ImagePreview(fc));
      int returnVal = fc.showDialog(view.getJFrame(), "Attach");
      // Process the results.
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        view.addToLog("Attaching file: " + fc.getSelectedFile() + ".\n");
        imageFactory.initFactory(fc.getSelectedFile());
      }
      else {
        assert true;
        view.addToLog("Attachment cancelled by user.\n");
      }
      fc.setSelectedFile(null);
    }
  }

  private class RunGibbsListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      imageFactory.setMethod("gibbs");
      imageFactory.setStartTemperature(view.getStartTemperature());
      imageFactory.setCoolingSchedule(view.getCoolingSchedule());
      view.addToLog("will start with temperature: " + imageFactory.getStartTemperature()
          + ", cooling rate: " + imageFactory.getCoolingRate());
      new Thread(imageFactory).start();
    }
  }

  private class RunICMListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      imageFactory.setMethod("icm");
      imageFactory.setStartTemperature(view.getStartTemperature());
      imageFactory.setCoolingSchedule(view.getCoolingSchedule());
      view.addToLog("will start with temperature: " + imageFactory.getStartTemperature()
          + ", cooling rate: " + imageFactory.getCoolingRate());
      new Thread(imageFactory).start();
    }
  }

  private class RunMetropolisListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      imageFactory.setMethod("metropolis");
      imageFactory.setStartTemperature(view.getStartTemperature());
      imageFactory.setCoolingSchedule(view.getCoolingSchedule());
      view.addToLog("will start with temperature: " + imageFactory.getStartTemperature()
          + ", cooling rate: " + imageFactory.getCoolingRate());
      new Thread(imageFactory).start();
    }
  }

  private class RunRJListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      imageFactory.setStartTemperature(view.getStartTemperature());
      imageFactory.setCoolingSchedule(view.getCoolingSchedule());
      imageFactory.setMethod("rjmcmc");
      view.addToLog("will start with temperature: " + imageFactory.getStartTemperature()
          + ", cooling rate: " + imageFactory.getCoolingRate());
      new Thread(imageFactory).start();
    }
  }

  private class StopSimulationListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      view.addToLog("THIS FUNCTION IS NOT YET IMPLEMENTED");
    }
  }

}
