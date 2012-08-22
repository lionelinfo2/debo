package gui;

import javax.swing.BorderFactory;

import eventengine.Event;
import eventengine.EventEngine;
import eventengine.ProcessInstance;

public class GUIController {
	EventEngine _engine;
	GUIEventEngine _gui;
	GUIProcessEnginePanel _enginePanel;
	
	public GUIController(EventEngine engine, GUIEventEngine gui) {
		_gui = gui;
		_engine = engine;
		
		engine.setController(this);
		this.initialize();
	}
	
	public GUIController(EventEngine engine) {
		GUIEventEngine gui = new GUIEventEngine();
		_gui = gui;
		_engine = engine;
		
		engine.setController(this);
		this.initialize();
	}

	public void printDebug(String debug) {
		_gui.printDebug(debug);
		
		// update labels of process instances
	}
	
	public void updateInstanceInformation() {
//		_enginePanel.setProcessInstances(getProcessInstanceRepresentation());
	}
	
	public EventEngine getEngine() {
		return _engine;
	}
	
	private synchronized String getProcessInstanceRepresentation() {
		String rep = "<html>";
		
		synchronized (getEngine().getProcessInstances()) {
			for (ProcessInstance processInstance: getEngine().getProcessInstances()) {
				rep = rep + "Instance: " + processInstance.getInstanceId() + " | " + processInstance.getEventRule().toHTML() + "<br/>";
			}
		}		
		rep = rep + "</html>";
		
		return rep;
	}
	
	private String getProcessRepresentation() {
		return getEngine().toString();
	}
	
	public void setEngineTitle(String title) {
		_enginePanel.setBorder(BorderFactory.createTitledBorder(title)); 
		_enginePanel.revalidate();
	}
	
	public void initialize() {
		// Create panel to hold the information of the process engine
		_enginePanel = new GUIProcessEnginePanel("Engine " + _engine.getName());
		_gui.addProcessInstancePanel(_enginePanel);
		
		this.initiateInformationPanel();		
	}
	
	public void initiateInformationPanel() {
		_enginePanel.setInformationPanel(getProcessRepresentation());
	}
	
}


