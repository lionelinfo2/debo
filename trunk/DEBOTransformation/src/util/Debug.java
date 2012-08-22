package util;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public class Debug {

	public static boolean debugOn = true;

	static public void print (String debug) {
		if (debugOn) {System.out.println(debug);}
	}


	static public void print(Collection list) {
		if (debugOn) {
			Iterator iter = list.iterator();

			while (iter.hasNext()) {
				Object object = iter.next();
				if (object instanceof EObject)
					printObject(0, (EObject)object, null, true);
			}
		}
	}

	static public void printObject(
			int tabIndex,
			EObject EObject,
			EReference referenceObj,
			boolean printReferences) {
		if (tabIndex != 0) {
			System.out.println();
			for (int i = 0; i < tabIndex; i++)
				System.out.print("\t");
		}
		ENamedElement nameObj =
			(referenceObj == null)
			? (ENamedElement) EObject.eClass()
					: referenceObj;
			System.out.println(nameObj.getName() + ": " );

			printAllAttributes(tabIndex + 1, EObject);      
			if (printReferences)
				printAllReferences(tabIndex, EObject);
	}

	static private void printAllAttributes(int tabIndex, EObject EObject) {
		EClass eMetaObject = EObject.eClass();
		if (eMetaObject == null)
			return;

		Collection attrs = eMetaObject.getEAllAttributes();
		if (attrs == null)
			return;
		Iterator iAttr = attrs.iterator();

		while (iAttr.hasNext()) {
			EAttribute eAttr = (EAttribute) iAttr.next();
			printAttribute(tabIndex, EObject, eAttr);
		}
	}

	static private void printAttribute(
			int tabIndex,
			EObject EObject,
			EAttribute eAttr) {
		if (!EObject.eIsSet(eAttr)) {
			return;
		}

		Object value = EObject.eGet(eAttr);

		if (eAttr.isVolatile() || (value == null))
			return;

		String valueS = "";
		if (eAttr.isMany()) {
			Iterator vals = ((Collection) value).iterator();
			while (vals.hasNext()) {
				if (valueS.length() > 0)
					valueS += ", ";
				valueS += vals.next().toString();
			}
		}
		else
			valueS = value.toString();
		for (int i = 0; i < tabIndex; i++)
			System.out.print("\t");

		System.out.println(eAttr.getName() + ": " + valueS);
		return;
	}

	static private void printAllReferences(int tabIndex, EObject EObject) {
		EClass eMetaObject = EObject.eClass();
		if (eMetaObject == null)
			return;

		Collection refs = eMetaObject.getEAllReferences();

		if (refs == null)
			return;
		Iterator iRef = refs.iterator();

		while (iRef.hasNext()) {
			EReference ref = (EReference) iRef.next();
			printReference(tabIndex, EObject, ref);
		}
	}

	static private void printReference(
			int tabIndex,
			EObject EObject,
			EReference ref) {
		Object value = EObject.eGet(ref);
		if (ref.isVolatile() || (value == null))
			return;

		if (ref.isMany()) {
			Iterator vals = ((Collection) value).iterator();
			while (vals.hasNext()) {
				EObject eValue = (EObject)vals.next();
				if (eValue==null)
					return;
				boolean printNestedReferences =
					eValue.eContainer() == EObject;
				printObject(tabIndex + 1, eValue, ref, printNestedReferences);
			}
		}
		else {
			EObject eValue = (EObject)value;
			boolean printNestedReferences = eValue.eContainer() == EObject;
			printObject(tabIndex + 1, eValue, ref, printNestedReferences);
		}
	}
}
