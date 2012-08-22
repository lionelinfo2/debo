package ui;

import io.FileChooser;
import io.FileExtension;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

import javax.sound.midi.ControllerEventListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import core.ChangeController;

import siena.Filter;
import siena.HierarchicalDispatcher;
import siena.Notification;
import siena.Op;
import siena.SienaException;
import siena.comm.InvalidSenderException;
import siena.comm.PacketSenderException;
import tests.Logger;

public class MainWindow extends JPanel {
	
	private String bpmnFileDefault = "PizzaDelivery-dist.bpmn";
	
//	private String currentProcessDescription = null;
//	private String changedProcessDescription = null;
	
	// Text fields for the change management
	private JTextField txtCurrentProcessDescription = new JTextField();
	private JTextField txtChangedProcessDescription = new JTextField();
	
	// Text field for the event engine (runtime)
	private JTextField txtBpmnFile = new JTextField("");
		
	private JPanel runtimePanel = new JPanel();
	private JPanel changePanel = new JPanel();
	
	private JTextField _txtServerAddr;
	
	private ChangeController _controller = new ChangeController();
	
	
	public MainWindow () {
		super(new BorderLayout());
		this.initComponents();
	}
	
	private void initComponents () {
						
		this.initRuntimePanel();
		this.initChangePanel();
		
//		this.setPreferredSize(new Dimension(500,500));
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder(
                10, //top
                10,     //left
                10, //bottom
                10));   //right
		this.add(runtimePanel);
		this.add(Box.createHorizontalStrut(10));
		this.add(changePanel);
		
		
		
	}
	
	private JPanel createFileChooserArea(String instructions, JTextField pathField, ActionListener action){ 
		JPanel fileChooser = new JPanel();
		//fileChooser.setLayout(new BoxLayout(fileChooser, BoxLayout.X_AXIS));
		
		JLabel instructionsLabel = new JLabel(instructions);
		instructionsLabel.setPreferredSize(new Dimension(80, 20));
				
		pathField.setPreferredSize(new Dimension(150,20));
		JButton browse = new JButton("Browse");
		browse.addActionListener(action);
		
		fileChooser.add(instructionsLabel);
		fileChooser.add(pathField);
		fileChooser.add(browse);
		//fileChooser.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		
		return fileChooser;
	}
	
	public void initChangePanel() {
		
		// Current File Chooser
		JPanel currentFileChooser = this.createFileChooserArea("Current:  ", txtCurrentProcessDescription, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File file = FileChooser.open(null, FileExtension.BPMN);
				if (file == null)
					return;
				txtCurrentProcessDescription.setText(file.getAbsolutePath());
			}
		});
		
		// Output File Chooser
		JPanel changedFileChooser = this.createFileChooserArea("Changed: ", txtChangedProcessDescription, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File file = FileChooser.open(null, FileExtension.BPMN);
				if (file == null)
					return;
				txtChangedProcessDescription.setText(file.getAbsolutePath());
			}
		});		
		
		JPanel changeCommand = new JPanel();
		changeCommand.setLayout(new BoxLayout(changeCommand, BoxLayout.X_AXIS));
		final JCheckBox signavioCheck = new JCheckBox("Signavio File?");
		signavioCheck.setSelected(true);
		JButton redeployButton = new JButton("Redeploy");
		redeployButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_controller.setHierarchicalDispatcher(getHierarchicalDispatcher());
				_controller.redeploy(txtCurrentProcessDescription.getText(), txtChangedProcessDescription.getText(), signavioCheck.isSelected());
			}

			
		});
		changeCommand.add(signavioCheck);
		changeCommand.add(redeployButton);
		
		
		JButton startChangeControler = new JButton("Start Change Controller");
		startChangeControler.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (_controller == null) {
					createController();
				}
			}
		});
		
		JPanel pauzePanel = new JPanel();
				
		JLabel fragmentLabel = new JLabel("Fragment to suspend (ALL for all):");
		fragmentLabel.setPreferredSize(new Dimension(200, 20));
		final JTextField txtFragment = new JTextField("ALL");
		txtFragment.setPreferredSize(new Dimension(150, 20));
		
		pauzePanel.add(fragmentLabel);
		pauzePanel.add(txtFragment);
		
		JButton pauzeButton = new JButton("Suspend");
		pauzeButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (_controller.getHierarchicalDispatcher() == null) {
					_controller.setHierarchicalDispatcher(getHierarchicalDispatcher());
				}
				_controller.publishAction(txtFragment.getText(), "suspend");
			}

			
		});
		
		JPanel resumePanel = new JPanel();
		fragmentLabel = new JLabel("Fragment to resume (ALL for all):");
		fragmentLabel.setPreferredSize(new Dimension(200, 20));
		final JTextField txtFragmentResume = new JTextField("ALL");
		txtFragmentResume.setPreferredSize(new Dimension(150, 20));
		
		resumePanel.add(fragmentLabel);
		resumePanel.add(txtFragmentResume);
		
		JButton resumeButton = new JButton("Resume");
		resumeButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (_controller.getHierarchicalDispatcher() == null) {
					_controller.setHierarchicalDispatcher(getHierarchicalDispatcher());
				}
				_controller.publishAction(txtFragmentResume.getText(), "resume");
			}
		});
		
		JButton testButton = new JButton("Test Button");
		testButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (_controller.getHierarchicalDispatcher() == null) {
					_controller.setHierarchicalDispatcher(getHierarchicalDispatcher());
				}
				//_controller.publishAction("BakePizza", "getRunningProcessIDs");
				Notification n = new Notification();
				n.putAttribute("id", "CTRL");
				n.putAttribute("fragment","new");
				n.putAttribute("task", "Test");
				n.putAttribute("action", "redeploy");
				n.putAttribute("eventRule", "[(SignalStartProcess)]");
				n.putAttribute("PIIDS", "[10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20]");
				n.putAttribute("dataGen", false);
				_controller.publish(n);
				
				
				//_controller.publishRedeploy("BakePizza", "[20, 21, 23, 24]", "[(Test1 AND Test2) OR (Test3 AND Cond(0--100)]");
				
			}
		});
		
		changePanel.setLayout(new BoxLayout(changePanel, BoxLayout.Y_AXIS));
		changePanel.setBorder(BorderFactory.createTitledBorder("Change Management"));
		
		changePanel.add(currentFileChooser);
		changePanel.add(changedFileChooser);
		changePanel.add(changeCommand);
		
		changePanel.add(Box.createVerticalStrut(5));
		changePanel.add(new JSeparator(SwingConstants.HORIZONTAL));
//		changePanel.add(Box.createVerticalStrut(5));
//		
//		changePanel.add(testButton);
		
//		changePanel.add(Box.createVerticalStrut(5));
//		changePanel.add(new JSeparator(SwingConstants.HORIZONTAL));
//		changePanel.add(Box.createVerticalStrut(5));
//		
//		changePanel.add(startChangeControler);
		
		changePanel.add(Box.createVerticalStrut(5));
		changePanel.add(new JSeparator(SwingConstants.HORIZONTAL));
		changePanel.add(Box.createVerticalStrut(5));
		
		changePanel.add(pauzePanel);
		//changePanel.add(Box.createVerticalStrut(5));
		changePanel.add(pauzeButton);
		
		changePanel.add(Box.createVerticalStrut(5));
		changePanel.add(new JSeparator(SwingConstants.HORIZONTAL));
		changePanel.add(Box.createVerticalStrut(5));
		
		changePanel.add(resumePanel);
		//changePanel.add(Box.createVerticalStrut(5));
		changePanel.add(resumeButton);
	}
	
	private void initRuntimePanel() {
		JButton startTransformation = new JButton("Transformer");
		startTransformation.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				/*** Start Transformer ***/
				// Windows only solution!
				String command = "cmd /c start java -classpath \"jars/DEBOTransformation.jar;libs/jdom.jar;libs/xmi.jar;libs/ecore.jar;libs/common.jar;libs/bpmn2.jar\" main.DEBOTransformation";

				try {
					Process pcTransformer = Runtime.getRuntime().exec(command);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		
		JPanel sienaPanel = new JPanel();
		//sienaPanel.setLayout(new BoxLayout(sienaPanel, BoxLayout.X_AXIS));
		
		JLabel srvrAddrLabel = new JLabel("Server Address:");
		srvrAddrLabel.setPreferredSize(new Dimension(130, 20));
		_txtServerAddr = new JTextField("localhost");
		_txtServerAddr.setPreferredSize(new Dimension(150, 20));
		
		sienaPanel.add(srvrAddrLabel);
		sienaPanel.add(_txtServerAddr);
		
		JButton startSiena = new JButton("Start pub/sub Server");
		startSiena.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {


				/*** Start Siena Server ***/
				_controller.startSiena();
			}
		});

		JPanel eventEnginePanel = new JPanel();
		//eventEnginePanel.setLayout(new BoxLayout(eventEnginePanel, BoxLayout.X_AXIS));
		
		JLabel bpmnFileLabel = new JLabel("Process Fragment:");
		bpmnFileLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		bpmnFileLabel.setPreferredSize(new Dimension(110, 20));
		txtBpmnFile.setPreferredSize(new Dimension(150, 20));
		
		JButton browse = new JButton("Browse");
		browse.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				File bpmnFile = FileChooser.open(null, FileExtension.BPMN);
				if (bpmnFile == null)
					return;
				txtBpmnFile.setText(bpmnFile.getAbsolutePath());
			}
		});
		
		eventEnginePanel.add(bpmnFileLabel);
		eventEnginePanel.add(txtBpmnFile);
		eventEnginePanel.add(browse);
		
		JPanel taskTimePanel = new JPanel();
		JLabel taskTimeLabel = new JLabel("Task Time:");
		taskTimeLabel.setPreferredSize(new Dimension(70, 20));
		final JTextField txtTaskTime = new JTextField("3000");
		txtTaskTime.setPreferredSize(new Dimension(150, 20));
		taskTimePanel.add(taskTimeLabel);
		taskTimePanel.add(txtTaskTime);
		
		JButton startEventEngine = new JButton("Start Event Engine(s)");
		startEventEngine.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				/*** Start EventEngine ***/
				String sienaAddress = getSienaAddress();

				if (txtBpmnFile.getText().equals("")) {
					System.out.println("YES");
					txtBpmnFile.setText(bpmnFileDefault);
				}

				Process pc = _controller.startEventEngine(txtBpmnFile.getText(), sienaAddress, txtTaskTime.getText());
			}
		});

		
		JPanel rpmPanel = new JPanel();
		//rpmPanel.setLayout(new BoxLayout(rpmPanel, BoxLayout.X_AXIS));
		
		JLabel rpmLabel = new JLabel("RPM:");
		rpmLabel.setPreferredSize(new Dimension(50, 20));
		final JTextField txtRpm = new JTextField("50");
		txtRpm.setPreferredSize(new Dimension(150, 20));
		rpmPanel.add(rpmLabel);
		rpmPanel.add(txtRpm);
		
		JPanel timePanel = new JPanel();
		//timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.X_AXIS));
		
		JLabel timeLabel = new JLabel("Time:");
		timeLabel.setPreferredSize(new Dimension(50, 20));
		final JTextField txtTime = new JTextField("5");
		txtTime.setPreferredSize(new Dimension(150, 20));
		timePanel.add(timeLabel);
		timePanel.add(txtTime);
				
		JButton startTrigger = new JButton("Start Client Triggering");
		startTrigger.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				/** Start TriggerEventEngine ***/
				InetAddress thisIp = null;
				String sienaAddress = null;
				try {
					sienaAddress = getSienaAddress();
					
					int rpm = Integer.parseInt(txtRpm.getText());
					int minutes = Integer.parseInt(txtTime.getText());

					Process pc = _controller.startClientTriggering(rpm, minutes, sienaAddress); 	
						
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		
		runtimePanel.setLayout(new BoxLayout(runtimePanel, BoxLayout.Y_AXIS));
//		runtimePanel.setPreferredSize(new Dimension(200,100));
		runtimePanel.setBorder(BorderFactory.createTitledBorder("Runtime"));
		
		runtimePanel.add(startTransformation);
		
		runtimePanel.add(Box.createVerticalStrut(10));
		runtimePanel.add(new JSeparator(SwingConstants.HORIZONTAL));
		runtimePanel.add(Box.createVerticalStrut(10));
		
		runtimePanel.add(sienaPanel);
		runtimePanel.add(Box.createVerticalStrut(5));
		runtimePanel.add(startSiena);		
		
		runtimePanel.add(Box.createVerticalStrut(10));
		runtimePanel.add(new JSeparator(SwingConstants.HORIZONTAL));
		runtimePanel.add(Box.createVerticalStrut(10));

		runtimePanel.add(eventEnginePanel);
		runtimePanel.add(taskTimePanel);
		runtimePanel.add(Box.createVerticalStrut(5));
		runtimePanel.add(startEventEngine);

		runtimePanel.add(Box.createVerticalStrut(10));
		runtimePanel.add(new JSeparator(SwingConstants.HORIZONTAL));
		runtimePanel.add(Box.createVerticalStrut(10));
		
		runtimePanel.add(rpmPanel);
		//runtimePanel.add(Box.createVerticalStrut(5));
		runtimePanel.add(timePanel);
		//runtimePanel.add(Box.createVerticalStrut(5));
		runtimePanel.add(startTrigger);
		
		
			
	}
	
	private HierarchicalDispatcher getHierarchicalDispatcher() {
		HierarchicalDispatcher siena = null;
		try {
			siena = new HierarchicalDispatcher();

			InetAddress thisIp;
			String sienaAddress;

			if (_txtServerAddr.getText().equals("localhost")) {
				thisIp = InetAddress.getLocalHost();
				sienaAddress = "udp:" + thisIp.getHostAddress() + ":2348";
			} else {
				sienaAddress = _txtServerAddr.getText();
			}

			siena.setMaster(sienaAddress);
					
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return siena;
	}
	
	private void createController() {
		try {
			HierarchicalDispatcher siena;

			siena = new HierarchicalDispatcher();

			InetAddress thisIp;
			String sienaAddress;

			if (_txtServerAddr.getText().equals("localhost")) {
				thisIp = InetAddress.getLocalHost();
				sienaAddress = "udp:" + thisIp.getHostAddress() + ":2348";
			} else {
				sienaAddress = _txtServerAddr.getText();
			}

			siena.setMaster(sienaAddress);
			
			// Create and start the controller (listener for CTRL events)
			_controller = new ChangeController(siena);
								
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}
	
	private String getSienaAddress() {
		InetAddress thisIp;
		String sienaAddress = "";

		try {
			if (_txtServerAddr.getText().equals("localhost")) {
				thisIp = InetAddress.getLocalHost();
				sienaAddress = "udp:" + thisIp.getHostAddress() + ":2348";
			} else {
				sienaAddress = _txtServerAddr.getText();
			}
		}catch (Exception exp) {
			exp.printStackTrace();
		}

		return sienaAddress;
	}

	
	private static void createAndShowGUI() {
        // Create and set up the window.
        JFrame frame = new JFrame("Controller");
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
