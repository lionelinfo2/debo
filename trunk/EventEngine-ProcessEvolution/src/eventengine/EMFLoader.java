
package eventengine;

import java.io.File;

import org.eclipse.bpmn2.Bpmn2Package;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.util.Bpmn2ResourceFactoryImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

/**
 * Load a bpmn2 XMI file and return the definitions object (root object)
 *
 */
public class EMFLoader {

	public Definitions load(String fileURI) {
		// Create a resource set to hold the resources.
        //
        ResourceSet resourceSet = new ResourceSetImpl();

        // Register the appropriate resource factory to handle all file extensions.
        //
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put(Resource.Factory.Registry.DEFAULT_EXTENSION, new Bpmn2ResourceFactoryImpl());

        // Register the package to ensure it is available during loading.
        //
        resourceSet.getPackageRegistry().put(Bpmn2Package.eNS_URI, Bpmn2Package.eINSTANCE);
        
        File file = new File(fileURI);
        URI uri = file.isFile() ? URI.createFileURI(file.getAbsolutePath()) : URI
                .createURI(fileURI);
    
        try {
            // Demand load resource for this file.
            //
            Resource resource = resourceSet.getResource(uri, true);
            System.out.println("Loaded " + uri);
            
            return this.getRootDefinitionElement(resource); 
            
        } catch (RuntimeException exception) {
            System.out.println("Problem loading " + uri);
            exception.printStackTrace();
        }
        
        return null;
	}
	
	
	public Definitions getRootDefinitionElement(Resource res) {
        EObject root = res.getContents().get(0);
        if (root instanceof DocumentRoot)
            return ((DocumentRoot) root).getDefinitions();
        return (Definitions) root;
    }
}
