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

/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI Builder, which is free
 * for non-commercial use. If Jigloo is being used commercially (ie, by a corporation, company or
 * business for any purpose whatever) then you should purchase a license for each developer using
 * Jigloo. Please visit www.cloudgarden.com for details. Use of Jigloo implies acceptance of these
 * licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS
 * CODE CANNOT BE USED LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
public class View extends javax.swing.JFrame implements Observer {

  /**
   * 
   */
  private static final long serialVersionUID = -8414404741589511194L;
  private JFrame frame;
  private JButton loadImageButton;
  private JTextPane logTextPane;
  private JButton startProcessButton;
  private JScrollPane logPane;
  private JPanel segmentedImagePane;
  private JPanel originalImagePane;

  /**
   * Auto-generated main method to display this JFrame
   */
  public static void main(String[] args) {
    View inst = new View();
    inst.setVisible(true);
  }

  public View() {
    super();
    initGUI();
  }

  private void initGUI() {
    try {
      {
        this.setTitle("rjimageDemo v 0.01");
        getContentPane().setLayout(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setFocusTraversalKeysEnabled(false);
        {
          originalImagePane = new JPanel();
          getContentPane().add(originalImagePane);
          originalImagePane.setBounds(7, 7, 231, 189);
          originalImagePane.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
        }
        {
          segmentedImagePane = new JPanel();
          getContentPane().add(segmentedImagePane);
          segmentedImagePane.setBounds(252, 7, 238, 189);
          segmentedImagePane.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
        }
        {
          logPane = new JScrollPane();
          getContentPane().add(logPane);
          logPane.setBounds(7, 252, 483, 63);
          {
            logTextPane = new JTextPane();
            logPane.setViewportView(logTextPane);
            logTextPane.setText("logging:");
          }
        }
        {
          startProcessButton = new JButton();
          getContentPane().add(startProcessButton);
          startProcessButton.setText("Start!");
          startProcessButton.setBounds(133, 210, 105, 28);
        }
        {
          loadImageButton = new JButton();
          getContentPane().add(loadImageButton);
          loadImageButton.setText("Load...");
          loadImageButton.setBounds(7, 210, 119, 28);
        }
      }
      this.setSize(505, 381);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
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
    this.frame = new JFrame();
    this.frame.setTitle("rjimageDemo v 0.01");
    this.frame.getContentPane().setLayout(null);
    this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.frame.setFocusTraversalKeysEnabled(false);
    {
      originalImagePane = new JPanel();
      this.frame.getContentPane().add(originalImagePane);
      originalImagePane.setBounds(7, 7, 231, 189);
      originalImagePane.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
    }
    {
      segmentedImagePane = new JPanel();
      this.frame.getContentPane().add(segmentedImagePane);
      segmentedImagePane.setBounds(252, 7, 238, 189);
      segmentedImagePane.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
    }
    {
      logPane = new JScrollPane();
      this.frame.getContentPane().add(logPane);
      logPane.setBounds(7, 252, 483, 63);
      {
        logTextPane = new JTextPane();
        logPane.setViewportView(logTextPane);
        logTextPane.setText("logging:");
      }
    }
    {
      startProcessButton = new JButton();
      this.frame.getContentPane().add(startProcessButton);
      startProcessButton.setText("Start!");
      startProcessButton.setBounds(133, 210, 105, 28);
    }
    {
      loadImageButton = new JButton();
      this.frame.getContentPane().add(loadImageButton);
      loadImageButton.setText("Load...");
      loadImageButton.setBounds(7, 210, 119, 28);
    }
    this.frame.setSize(505, 381);

    // Show frame
    this.frame.pack();
    this.frame.setVisible(true);
  }

  /**
   * Constructs and displays the GUI on the AWT event thread to ensure thread-safety.
   */
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
