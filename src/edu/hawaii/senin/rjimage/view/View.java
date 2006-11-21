package edu.hawaii.senin.rjimage.view;

import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.BevelBorder;

import net.miginfocom.swing.MigLayout;

public class View implements Observer {

  private JFrame frame = new JFrame("rjimageDemo v 0.01");
  private JButton loadImageButton = new JButton("Load...");
  private JTextPane logTextPane = new JTextPane();
  private JButton startProcessButton = new JButton("Start!");
  private JScrollPane logPane = new JScrollPane();;
  private JPanel segmentedImagePane = new JPanel();
  private JPanel originalImagePane = new JPanel();

  public View() {
    // does nothing
  }

  public JFrame getJFrame() {
    return this.frame;
  }

  public void addLoadListener(ActionListener listener) {
    loadImageButton.addActionListener(listener);
  }

  public void addRunListener(ActionListener listener) {
    startProcessButton.addActionListener(listener);
  }

  /**
   * Creates the layout of the GUI window. Uses the MigLayout manager to simplify the display.
   */
  private void configureGUI() {
    this.frame.setTitle("rjimageDemo v 0.01");
    JFrame.setDefaultLookAndFeelDecorated(true);
    MigLayout layout = new MigLayout();
    this.frame.getContentPane().setLayout(layout);
    {
      this.frame.getContentPane().add(originalImagePane);
      originalImagePane.setBounds(7, 7, 231, 189);
      originalImagePane.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
    }
    {
      this.frame.getContentPane().add(segmentedImagePane);
      segmentedImagePane.setBounds(252, 7, 238, 189);
      segmentedImagePane.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
    }
    {
      this.frame.getContentPane().add(logPane);
      logPane.setBounds(7, 252, 483, 63);
      {
        logPane.setViewportView(logTextPane);
        logTextPane.setText("logging:");
      }
    }
    {
      this.frame.getContentPane().add(startProcessButton);
      startProcessButton.setBounds(133, 210, 105, 28);
    }
    {
      this.frame.getContentPane().add(loadImageButton);
      loadImageButton.setBounds(7, 210, 119, 28);
    }
    this.frame.setSize(505, 381);

    // Show frame
    this.frame.pack();
    this.frame.setVisible(true);
  }

  public void showGUI() {
    // Schedule a job for the event-dispatching thread:
    // creating and showing this application's GUI.
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        configureGUI();
      }
    });
  }

  public void update(Observable arg0, Object arg1) {
    System.out.println("Got to observable");

  }
}
