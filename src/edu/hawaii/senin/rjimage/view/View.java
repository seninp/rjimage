package edu.hawaii.senin.rjimage.view;

import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.BevelBorder;

import net.miginfocom.swing.MigLayout;
import edu.hawaii.senin.rjimage.model.ImageFactory;
import edu.hawaii.senin.rjimage.model.ImageFactoryStatus;

public class View implements Observer {

  private JFrame frame = new JFrame("rjimageDemo v 0.01");
  private JButton loadImageButton = new JButton("Load...");
  private JTextPane logTextPane = new JTextPane();
  private JButton startProcessButton = new JButton("Start!");
  private JScrollPane logPane = new JScrollPane();;
  private JPanel currentImagePane = new JPanel();
  private JPanel originalImagePane = new JPanel();
  private ImageIcon originalImageIcon = new ImageIcon();
  private JLabel originalImageLabel = new JLabel(originalImageIcon);
  private ImageIcon currentImageIcon = new ImageIcon();
  private JLabel currentImageLabel = new JLabel(originalImageIcon);
  private StringBuffer logText = new StringBuffer(10000);

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

    // set look and fill
    JFrame.setDefaultLookAndFeelDecorated(true);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // set the layout for the application
    MigLayout layout = new MigLayout("", "[center]50[center]", "[]10[]10[]");
    this.frame.getContentPane().setLayout(layout);

    // add and set basic settings for the all panels
    //
    // original Image display panel
    this.frame.getContentPane().add(originalImagePane, "width :200:, height :200:");
    originalImagePane.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
    originalImagePane.add(originalImageLabel);

    // current segmented image panel
    this.frame.getContentPane().add(currentImagePane, "width :200:, height :200:, wrap");
    currentImagePane.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
    currentImagePane.add(currentImageLabel);

    // add load image button
    this.frame.getContentPane().add(loadImageButton);
    loadImageButton.setBounds(7, 210, 119, 28);

    // add start button
    this.frame.getContentPane().add(startProcessButton, "wrap");
    startProcessButton.setBounds(133, 210, 105, 28);

    // adding logging panel
    this.frame.getContentPane().add(logTextPane, "span");
    // logPane.setLayout(new MigLayout());
    // logPane.setViewportView(logTextPane,);
    logTextPane.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
    logTextPane.setAutoscrolls(true);
    this.logText.append("ReversibleJump MCMC Image segmentation ver. 1.0 \n logging:\n");
    logTextPane.setText(this.logText.toString());

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
    System.out.println(arg0.toString() + arg1.toString());
    // if reported loading of new image
    if ((arg0 instanceof ImageFactory) && ((Integer) arg1).equals(ImageFactoryStatus.NEW_IMAGE)) {
      if (null == ((ImageFactory) arg0).getOriginalImage()) {
        assert true;
      }
      else {
        originalImageIcon.setImage(((ImageFactory) arg0).getOriginalImage());
        originalImageLabel.setIcon(originalImageIcon);
        originalImageLabel.updateUI();
        this.originalImagePane.repaint();

        currentImageIcon.setImage(((ImageFactory) arg0).getCurrentImage());
        currentImageLabel.setIcon(currentImageIcon);
        currentImageLabel.updateUI();
        this.currentImagePane.repaint();
      }
    }

    if ((arg0 instanceof ImageFactory)
        && ((Integer) arg1).equals(ImageFactoryStatus.NEW_SEGMENTATION)) {
      if (null == ((ImageFactory) arg0).getOriginalImage()) {
        assert true;
      }
      else {
        currentImageIcon.setImage(((ImageFactory) arg0).getCurrentImage());
        currentImageLabel.setIcon(currentImageIcon);
        currentImageLabel.updateUI();
        this.currentImagePane.repaint();
      }
    }

    if ((arg0 instanceof ImageFactory) && ((Integer) arg1).equals(ImageFactoryStatus.INVALID_IMAGE)) {
      this.logText.append("ERROR: Invalid Image selected!\n");
    }
  }
}
