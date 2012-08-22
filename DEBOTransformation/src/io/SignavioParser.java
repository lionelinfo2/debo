package io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class SignavioParser {

	private static String tmpDir = ".\\tmp\\";
	private static String extension = ".xml";
	
	public static File process(String fileName) throws IOException, JDOMException {
		return process(new File(fileName));
	}
	
	/**
	 * Process a signavio file so that it can be read by the bpmn2 ecore model
	 * - Remove the xsi:schemalocation element (nullpointer)
	 * - Remove the signavio:signavioMetaData element (Feature not found)
	 * - change formalExpression in regular Expression with condition in documentation text
	 *		(somehow EMF doesn't read the body of a formal expression)
	 * 
	 * @param signavioFile
	 * @return The 'cleaned' file, saved to a temporary directory
	 * @throws IOException 
	 * @throws JDOMException 
	 */
	public static File process(File signavioFile) throws IOException, JDOMException {
		
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(signavioFile);
		
		// Remove xsi:schemaLocation
		doc.getRootElement().removeAttribute("schemaLocation", doc.getRootElement().getNamespace("xsi"));
		
		// Remove signavio:signavioMetaData
		// Iterator it = doc.getRootElement().getDescendants(new ElementFilter("signavioMetaData", doc.getRootElement().getNamespace("signavio")));
		Iterator it = doc.getRootElement().getDescendants(new ElementFilter("extensionElements"));
		while(it.hasNext()) {			
			Element el = (Element) it.next();
			it.remove();
		}		
		
		// Change any FormalExpression into a regular Expression with 
		// Expression.Document = FormalExpression.Body
		it = doc.getRootElement().getDescendants(new ElementFilter("conditionExpression"));
		// List necessary to avoid concurrentModification exceptions
		HashMap<Element, Element> mapExpressionDocumentation = new HashMap<Element, Element>(); 
		while(it.hasNext()) {
			Element el = (Element) it.next();
			// Formal expression found
			if (el.getAttributeValue("type", doc.getRootElement().getNamespace("xsi")).equals("tFormalExpression")) {
				// Change to regular Expression with the condition in the documentation element
				// <bpmn2:sequenceFlow id="1">
			    //   <bpmn2:conditionExpression xsi:type="bpmn2:tExpression" id="2">
				//		<bpmn2:documentation id="3">X</bpmn2:documentation>
				//	 </bpmn2:conditionExpression>
			    // </bpmn2:sequenceFlow>
				el.getAttribute("type", doc.getRootElement().getNamespace("xsi")).setValue("tExpression");
				el.removeAttribute("language");
				String content = el.getText();
				el.removeContent();
				
				Element docEl = new Element("documentation", doc.getRootElement().getNamespace());
				docEl.setAttribute("id", String.valueOf(UUID.randomUUID()));
				docEl.setText(content);
				
				// Store to change everything outside while loop
				mapExpressionDocumentation.put(el, docEl);
			}
		}
		
		// Store documentation object in correct Expression element
		for (Element key: mapExpressionDocumentation.keySet()) {
			key.addContent(mapExpressionDocumentation.get(key));
		}
		
		
		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
		File outputFile = new File(tmpDir + String.valueOf(UUID.randomUUID()) + extension);
		FileWriter writer = new FileWriter(outputFile);
		out.output(doc, writer);
//		out.output(doc, System.out);
		
		writer.flush();
		writer.close();
		
		return outputFile;
	}
	
	/**
	 * Test
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			SignavioParser.process(new File("SimpleGateways.bpmn"));
			
		} catch (IOException e) {
			
			e.printStackTrace();
		} catch (JDOMException e) {
			
			e.printStackTrace();
		}
	}
	
}
