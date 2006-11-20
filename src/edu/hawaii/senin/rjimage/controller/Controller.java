package edu.hawaii.senin.rjimage.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;

import edu.hawaii.senin.rjimage.model.ImageFactory;
import edu.hawaii.senin.rjimage.utils.ImageFileView;
import edu.hawaii.senin.rjimage.utils.ImageFilter;
import edu.hawaii.senin.rjimage.utils.ImagePreview;
import edu.hawaii.senin.rjimage.view.View;

public class Controller {

  private final ImageFactory imageFactory;

  private View view;

  public Controller(ImageFactory imageFactory, View view) {
    this.imageFactory = imageFactory;
    this.view = view;
    view.addLoadListener(new ImageLoadListener());
    view.addRunListener(new RunSegmentationListener());
    this.imageFactory.addObserver(this.view);
    view.showGUI();
  }

  private class ImageLoadListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {

      JFileChooser fc = new JFileChooser();
      // Add a custom file filter and disable the default
      // (Accept All) file filter.
      fc.addChoosableFileFilter(new ImageFilter());
      fc.setAcceptAllFileFilterUsed(false);

      // Add custom icons for file types.
      fc.setFileView(new ImageFileView());

      // Add the preview pane.
      fc.setAccessory(new ImagePreview(fc));
      int returnVal = fc.showDialog(view, "Attach");
      // Process the results.
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        imageFactory.initFactory(fc.getSelectedFile());
        // log.append("Attaching file: " + file.getName() + "." + newline);
        System.out.println("Choosed file " + fc.getSelectedFile().getPath());
      }
      else {
        // log.append("Attachment cancelled by user." + newline);
      }
      // log.setCaretPosition(log.getDocument().getLength());

      // Reset the file chooser for the next time it's shown.
      fc.setSelectedFile(null);

    }
  }

  private class RunSegmentationListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      imageFactory.RunSegmentation();
    }
  }

}
