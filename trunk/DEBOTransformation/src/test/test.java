package test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.Documentation;
import org.eclipse.bpmn2.Expression;
import org.eclipse.bpmn2.ExtensionAttributeDefinition;
import org.eclipse.bpmn2.ExtensionAttributeValue;
import org.eclipse.bpmn2.FormalExpression;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.ScriptTask;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.UserTask;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;

import util.EMFHelper;

public class test {

	
	public static void main (String args[]) {
		EMFHelper helper = new EMFHelper();
		
		Definitions model = Bpmn2Factory.eINSTANCE.createDefinitions();
		model.setExporter("Exporter");
		model.setExporterVersion("1");
		model.setName("Name");
		model.setTargetNamespace("tns1");
		
		final String scriptId = "st1";
        final String scriptContent = "Script content";

        Process p = Bpmn2Factory.eINSTANCE.createProcess();
        p.setName("Name");
        ScriptTask st = Bpmn2Factory.eINSTANCE.createScriptTask();
        st.setId(scriptId);
        st.setName("Name");
        st.setScript(scriptContent);
        st.setScriptFormat("Script format");
        p.getFlowElements().add(st);
        model.getRootElements().add(p);
        
        SequenceFlow sf = Bpmn2Factory.eINSTANCE.createSequenceFlow();
        
        Expression fe = Bpmn2Factory.eINSTANCE.createExpression();
        //fe.setBody("test");
        
        sf.setConditionExpression(fe);
        
        p.getFlowElements().add(sf);
        
        Documentation doc = Bpmn2Factory.eINSTANCE.createDocumentation();
        doc.setText("testy");
        
        fe.getDocumentation().add(doc);
        
        UserTask ut = Bpmn2Factory.eINSTANCE.createUserTask();
        
        
        try {
			helper.save("test.xml", model);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
		
	}
}
