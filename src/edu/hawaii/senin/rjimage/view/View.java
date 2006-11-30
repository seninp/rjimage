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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import net.miginfocom.swing.MigLayout;
import edu.hawaii.senin.rjimage.model.ImageFactory;
import edu.hawaii.senin.rjimage.model.ImageFactoryStatus;

/**
 * Main view panel for the RJMCMC demo.
 * 
 * @author Pavel Senin.
 * 
 */
public class View implements Observer {

  // setup main frame
  private JFrame frame = new JFrame("rjimageDemo v 0.01");

  // setup original image panel
  private JPanel originalImagePane = new JPanel();
  private ImageIcon originalImageIcon = new ImageIcon();
  private JLabel originalImageLabel = new JLabel(originalImageIcon);

  // setup current (segmented) original image panel
  private JPanel currentImagePane = new JPanel();
  private ImageIcon currentImageIcon = new ImageIcon();
  private JLabel currentImageLabel = new JLabel(originalImageIcon);

  // setup parameters panel along with fields
  private JPanel parametersPane = new JPanel();
  private JLabel temperatureLabel = new JLabel("Starting temperature");
  private JTextField temperatureTextField = new JTextField("6.0");
  private JLabel coolingScheduleLabel = new JLabel("Temperature schedule");
  private JTextField coolingScheduleTextField = new JTextField("0.98");

  // setup buttons panel
  private JPanel buttonsPane = new JPanel();
  private JButton loadImageButton = new JButton("Load...");
  private JButton startICMButton = new JButton("ICM");
  private JButton startGibbsButton = new JButton("Gibbs");
  private JButton startMetropolisButton = new JButton("Metropolis");
  private JButton startRJButton = new JButton("RJMCMC");
  private JButton stopSimulationButton = new JButton("Stop that!");

  // setup logging panel
  private JTextArea logTextArea = new JTextArea();
  JScrollPane logTextPane = new JScrollPane(logTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
      JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
  private StringBuffer logText = new StringBuffer(10000);

  /**
   * Needed by Java language standards.
   * 
   */
  public View() {
    // does nothing
    assert true;
  }
  
  /**
   * Needed by Java language standards to run this stuff.
   *
   */
  public void run(){
    // does nothing
    assert true;    
  }

  /**
   * Main frame getter.
   * 
   * @return main frame.
   */
  public JFrame getJFrame() {
    return this.frame;
  }

  public void addLoadListener(ActionListener listener) {
    loadImageButton.addActionListener(listener);
  }

  public void addICMListener(ActionListener listener) {
    startICMButton.addActionListener(listener);
  }

  public void addGibbsListener(ActionListener listener) {
    startGibbsButton.addActionListener(listener);
  }

  public void addMetropolisListener(ActionListener listener) {
    startMetropolisButton.addActionListener(listener);
  }

  public void addRJListener(ActionListener listener) {
    startRJButton.addActionListener(listener);
  }

  public void addStopSimulationListener(ActionListener listener) {
    stopSimulationButton.addActionListener(listener);
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

    // original Image display panel
    this.frame.getContentPane().add(originalImagePane, "width :320:, height :320:");
    originalImagePane.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
    originalImagePane.add(originalImageLabel);

    // current segmented image panel
    this.frame.getContentPane().add(currentImagePane, "width :320:, height :320:, wrap");
    currentImagePane.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
    currentImagePane.add(currentImageLabel);

    this.frame.getContentPane().add(parametersPane, "grow, span 2, wrap");
    parametersPane.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
    MigLayout staticLayout = new MigLayout("fillx", "[left]5[grow,fill]unrel[left]5[grow,fill]",
        "[]10[]10[]");
    this.parametersPane.setLayout(staticLayout);
    this.parametersPane.add(new JLabel("Temperature settings:"), "span 2,wrap");
    this.parametersPane.add(temperatureLabel, "");
    this.parametersPane.add(temperatureTextField, "");
    this.parametersPane.add(coolingScheduleLabel, "");
    this.parametersPane.add(coolingScheduleTextField, "wrap");

    // all buttons pane
    this.frame.getContentPane().add(buttonsPane, "width :690:, height :60:, span 2, wrap");
    // MigLayout buttonsPaneLayout = new MigLayout("", "[center]50[center]", "[]10[]10[]");
    // logPane.setLayout(new MigLayout());
    buttonsPane.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));

    // add load image button
    buttonsPane.add(loadImageButton);

    // add load image button
    buttonsPane.add(startICMButton);

    // add load image button
    buttonsPane.add(startGibbsButton);

    // add start button
    buttonsPane.add(startMetropolisButton);

    // add start button
    buttonsPane.add(startRJButton);

    // add stop button
    buttonsPane.add(stopSimulationButton, "wrap");

    // adding logging panel
    this.logTextArea.setEditable(false);
    this.logTextArea.append("running Reversible Jump image segmentation Demo v. 0.000001\n");
    this.logTextArea.setCaretPosition(this.logTextArea.getDocument().getLength());
    this.frame.getContentPane().add(logTextPane, "height 200:200:200, grow, span 2");
    // logPane.setLayout(new MigLayout());
    // logPane.setViewportView(logTextPane,);
    logTextPane.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
    logTextPane.setAutoscrolls(true);
    this.logText.append("ReversibleJump MCMC Image segmentation ver. 1.0 \n logging:\n");

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
    // System.out.println(arg0.toString() + arg1.toString());
    // if reported loading of new image
    if (arg1 instanceof Integer) {
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

      if ((arg0 instanceof ImageFactory)
          && ((Integer) arg1).equals(ImageFactoryStatus.INVALID_IMAGE)) {
        this.logText.append("ERROR: Invalid Image selected!\n");
      }
    }

    if ((arg0 instanceof ImageFactory) && (arg1 instanceof String)) {
      this.logTextArea.append((String) arg1);
      this.logTextArea.setCaretPosition(this.logTextArea.getDocument().getLength());
    }

  }
  

  /**
   * Reports initial temperature for simulated annealing.
   * 
   * @return initial temperature.
   */
  public Double getStartTemperature() {
    return Double.valueOf(this.temperatureTextField.getText());
  }

  /**
   * Reports temperature schedule.
   * 
   * @return temperature schedule.
   */
  public Double getCoolingSchedule() {
    return Double.valueOf(this.coolingScheduleTextField.getText());
  }

  /**
   * Adds provided string to the log panel.
   * 
   * @param str string to add.
   */
  public void addToLog(String str) {
    if(str.charAt(str.length()-1) != '\n'){
      str = str.concat("\n");
    }
    this.logTextArea.append(str);
    this.logTextArea.setCaretPosition(this.logTextArea.getDocument().getLength());
  }
}
