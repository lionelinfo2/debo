package ui;

import io.FileChooser;
import io.FileExtension;
import io.SignavioParser;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import main.Transformer;

public class MainWindow extends JPanel {
	
	private JCheckBox signavioCheck;
	private JTextField input = new JTextField();
	private File inputFile;
	private JTextField output = new JTextField();
	private File outputFile;
	private JLabel status = new JLabel();
	
	public MainWindow () {
		super(new BorderLayout());
		this.initComponents();
	}
	
	private void initComponents () {
		
		// Input File Chooser
		JPanel inputFileChooser = this.createFileChooserArea("Input:  ", input, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				inputFile = FileChooser.open(null, FileExtension.BPMN);
				if (inputFile == null)
					return;
				input.setText(inputFile.getAbsolutePath());
			}
		});
		
		// Output File Chooser
		JPanel outputFileChooser = this.createFileChooserArea("Output: ", output, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				outputFile = FileChooser.save(null, FileExtension.BPMN);
				if (outputFile == null)
					return;
				output.setText(outputFile.getAbsolutePath());
			}
		});
				
		// Convertor area
		JPanel convertorCommand = new JPanel();
		convertorCommand.setLayout(new BoxLayout(convertorCommand, BoxLayout.X_AXIS));
		signavioCheck = new JCheckBox("Signavio File?");
		signavioCheck.setSelected(true);
		JButton convert = new JButton("Convert");
		convert.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (inputFile == null || outputFile == null)
					return;
				
				status.setText("Converting...");
				
				if (signavioCheck.isSelected()) {
					try {
						inputFile = SignavioParser.process(inputFile);
					} catch (Exception exp) {
						exp.printStackTrace();
					}
				} 
				
				Transformer.getInstance().decentralize(inputFile.getAbsolutePath(), outputFile.getAbsolutePath());
				
				// Remove tmp file after use
				if (signavioCheck.isSelected())
					inputFile.delete();
				// re-initialise
				input.setText("");
				inputFile = null;
				output.setText("");
				outputFile = null;
				
				status.setText("Done!");
			}
		});
		
		convertorCommand.add(signavioCheck);
		convertorCommand.add(Box.createHorizontalGlue());
		convertorCommand.add(convert);
		convertorCommand.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));
				
		status.setPreferredSize(new Dimension(100,20));
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		this.add(inputFileChooser);
		this.add(outputFileChooser);
		this.add(convertorCommand);
		this.add(status);
		
		
		
	}
	
	private JPanel createFileChooserArea(String instructions, JTextField pathField, ActionListener action){ 
		JPanel fileChooser = new JPanel();
		fileChooser.setLayout(new BoxLayout(fileChooser, BoxLayout.X_AXIS));
		
		JLabel instructionsLabel = new JLabel(instructions);
		instructionsLabel.setPreferredSize(new Dimension(80, 20));
				
		pathField.setPreferredSize(new Dimension(300,20));
		JButton browse = new JButton("Browse");
		browse.addActionListener(action);
		
		fileChooser.add(instructionsLabel);
		fileChooser.add(pathField);
		fileChooser.add(browse);
		fileChooser.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		
		return fileChooser;
	}
	
	private static void createAndShowGUI() {
        // Create and set up the window.
        JFrame frame = new JFrame("Decentralized Transformer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create and set up the content pane.
        JComponent newContentPane = new MainWindow();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
	
	public static void startup() {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
	
	public static void main(String args[]) {
		startup();
	}
}
