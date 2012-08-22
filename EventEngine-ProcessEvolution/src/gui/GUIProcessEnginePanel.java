package gui;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

public class GUIProcessEnginePanel extends JPanel {
	
	private JPanel _processInstances;
	private JScrollPane _informationPanel;
	private JLabel _processInformation;
	private JLabel _processInstanceRepresentation;	
	
	private String _name;
		
	private boolean _userScrolling = false; 
	
	public GUIProcessEnginePanel() {
		initComponents("Process Engine");    
	}
	
	public GUIProcessEnginePanel(String name) {
		initComponents(name);
	}
	
	public void initComponents(String name) {
		_processInstances = new JPanel();
		_informationPanel = new JScrollPane();
		_processInformation = new JLabel();
		_processInstanceRepresentation = new JLabel();
		
		Border paddingBorder = BorderFactory.createEmptyBorder(10,10,10,10);
		
		//_processInstanceRepresentation.setPreferredSize(new Dimension(400,200));
		_processInstanceRepresentation.setBorder(paddingBorder);
		
		// Set Scrollable
        JScrollPane instanceScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,         	
        	JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        instanceScroll.setEnabled(true);
        instanceScroll.setPreferredSize(new Dimension(400,200));
        instanceScroll.setViewportView(_processInstanceRepresentation);
        
        // Autoscroll
        instanceScroll.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {   
        	public void adjustmentValueChanged(AdjustmentEvent e) {   
        		// Don't scroll if the user wants to scroll
        		if(!_userScrolling) {
        			e.getAdjustable().setValue(e.getAdjustable().getMaximum());   
        		}
        	}}); 
        
        instanceScroll.getVerticalScrollBar().addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
				_userScrolling = false;
				
			}
			
			@Override
			public void mousePressed(MouseEvent arg0) {
				// Stop auto scroll if user uses the scrollbar
				_userScrolling = true;
			}
			
			@Override
			public void mouseExited(MouseEvent arg0) {
							
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {
								
			}
			
			@Override
			public void mouseClicked(MouseEvent arg0) {
								
			}
		});

        
        _processInformation.setBorder(paddingBorder);
        
        _informationPanel.setEnabled(true);
        _informationPanel.setPreferredSize(new Dimension(100,100));
        _informationPanel.setViewportView(_processInformation);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(_informationPanel);
//        _informationPanel.revalidate();
        this.add(instanceScroll);
        this.setBorder(BorderFactory.createTitledBorder(name)); 
	}
	
	public void setProcessInstances(String processInstanceRepresentation) {
		_processInstanceRepresentation.setText(processInstanceRepresentation);
		_processInstanceRepresentation.revalidate();
		
		
		// TODO try to autoscroll
//		Rectangle r = new Rectangle(1,_processInstanceRepresentation.getHeight()-1,1,1);
//		_processInstanceRepresentation.scrollRectToVisible(r);
		
	}  
	
	public void setInformationPanel(String information) {
		_processInformation.setText(information);
		_processInformation.revalidate();
	}
}







