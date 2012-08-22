package util;

import java.io.File;
import java.io.IOException;

import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.Bpmn2Package;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.ScriptTask;
import org.eclipse.bpmn2.util.Bpmn2ResourceFactoryImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.omg.stub.java.rmi._Remote_Stub;

public class EMFHelper {
	
	ResourceSet _resSet;
	
	public EMFHelper() {
		// Create a resource set to hold the resources.
        //
        _resSet = new ResourceSetImpl();

        // Register the appropriate resource factory to handle all file extensions.
        //
        _resSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put(Resource.Factory.Registry.DEFAULT_EXTENSION, new Bpmn2ResourceFactoryImpl());

        // Register the package to ensure it is available during loading.
        //
        _resSet.getPackageRegistry().put(Bpmn2Package.eNS_URI, Bpmn2Package.eINSTANCE);
	}
	
	/**
	 * Load a .bpm file resource
	 * @param fileURI
	 * @return the loaded EMF resource
	 */
	public Resource load(String fileURI) {
		File file = new File(fileURI);
		return load(file);
	}
	
	public Resource load(File file) {
		URI uri = file.isFile() ? URI.createFileURI(file.getAbsolutePath()) : URI
				.createURI(file.getAbsolutePath());

		Resource resource = null;

		try {
			// Demand load resource for this file.
			//
			resource = _resSet.getResource(uri, true);

			// EcoreUtil.copy(eObject);

			// DEBUG
			//	            Debug.print(resource.getContents());
			//	            Debug.print(((DocumentRoot) resource.getContents().get(0)).getDefinitions().getRootElements()); 


		} catch (Exception exp) {
			exp.printStackTrace();
		}

		return resource;
	}
	
	/**
	 * 
	 */
	public void save(String fileName, Definitions model) throws IOException {
//		Definitions model = Bpmn2Factory.eINSTANCE.createDefinitions();
//		model.setExporter("Exporter");
//		model.setExporterVersion("1");
//		model.setName("Name");
//		model.setTargetNamespace("tns1");
//		
//		final String scriptId = "st1";
//        final String scriptContent = "Script content";
//
//        Process p = Bpmn2Factory.eINSTANCE.createProcess();
//        p.setName("Name");
//        ScriptTask st = Bpmn2Factory.eINSTANCE.createScriptTask();
//        st.setId(scriptId);
//        st.setName("Name");
//        st.setScript(scriptContent);
//        st.setScriptFormat("Script format");
//        p.getFlowElements().add(st);
//        model.getRootElements().add(p);
        
//        String name = "test-scriptconent";
        URI fileUri = URI.createFileURI(fileName);
           
        Resource res = _resSet.createResource(fileUri);
        res.getContents().add(model);
		res.save(null);      
	}
	
	public Definitions getRootDefinitionElement(Resource res) {
        EObject root = res.getContents().get(0);
        if (root instanceof DocumentRoot)
            return ((DocumentRoot) root).getDefinitions();
        return (Definitions) root;
    }
}
