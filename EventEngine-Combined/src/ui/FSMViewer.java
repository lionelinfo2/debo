package ui;

import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import ER2FSM.fsm.FSM;
import ER2FSM.fsm.State;
import ER2FSM.fsm.Transition;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class FSMViewer extends JFrame {

	private FSM _fsm;
	
	public FSMViewer(FSM fsm) {
		super("FSMViewer");
		
		_fsm = fsm;
		initialize();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(500, 500);
		this.setVisible(true);
	}

	private void initialize() {
		
		mxGraph graph = buildGraph();
		mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
		layout.execute(graph.getDefaultParent());

		mxGraphComponent graphComponent = new mxGraphComponent(graph);
		graphComponent.setConnectable(false);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(graphComponent);

		getContentPane().add(scrollPane);
	}
	
	private mxGraph buildGraph() {
		mxGraph graph = new mxGraph();
		Object defaultParent = graph.getDefaultParent();

		/** begin **/
		graph.getModel().beginUpdate();
		
		// First create vertices
		HashMap<State, Object> stateVertexLink = new HashMap<State, Object>();
		
//		Object startState = graph.insertVertex(defaultParent, null, _fsm._startState.toString(), 20, 20, _fsm._startState.toString().length()*5, 30);
//		stateVertexLink.put(_fsm._startState, startState);
		
		for (State s: _fsm._states) {
			Object vertex;
			if (s.endState) {
				vertex = graph.insertVertex(defaultParent, null, s.toString(), 20, 20, s.toString().length()*5, 30,"fillColor=green");
			} else {
				vertex = graph.insertVertex(defaultParent, null, s.toString(), 20, 20, s.toString().length()*5, 30);
			}
			stateVertexLink.put(s, vertex);
		}
		
		// Create edges, connecting vertices
//		for (Transition t: _fsm._startState.getOutgoing()) {
//			// for each outgoing transition create an edge
//			graph.insertEdge(defaultParent, null, t.getAction(), 
//					stateVertexLink.get(_fsm._startState), 
//					stateVertexLink.get(t.getTarget()));
//		}
		
		for (State s: _fsm._states) {
			for (Transition t: s.getOutgoing()) {
				graph.insertEdge(defaultParent, null, t.getAction(), 
						stateVertexLink.get(s), 
						stateVertexLink.get(t.getTarget()));
			}
		}
		
		/** end **/
		graph.getModel().endUpdate();
		
		return graph;
	}
		
}
