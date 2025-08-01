package comodo2.templates.fprime;

import com.google.common.collect.Iterables;
import comodo2.queries.QInterface;
import comodo2.queries.QStereotype;
import java.util.TreeSet;
import javax.inject.Inject;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Reception;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.xtend2.lib.StringConcatenation;

/**
 * F Prime Interface Parser and Port Generator
 * Maps UML Interfaces to F Prime component ports with proper typing
 * and integrates with F Prime component interface definitions.
 */
public class FPrimeInterface {

	@Inject
	private QInterface mQInterface;

	@Inject
	private QStereotype mQStereotype;

	/**
	 * Generate F Prime port definitions from UML Interfaces in the model
	 */
	public String generateFPPInterfacePorts(Resource input) {
		StringConcatenation str = new StringConcatenation();
		
		// Collect all UML Interfaces that should be generated
		TreeSet<String> interfaceNames = collectInterfaces(input);
		
		if (!interfaceNames.isEmpty()) {
			str.append("  # Ports derived from UML Interfaces");
			str.newLine();
			
			for (String interfaceName : interfaceNames) {
				Interface umlInterface = findInterfaceByName(input, interfaceName);
				if (umlInterface != null) {
					str.append(generatePortsForInterface(umlInterface));
				}
			}
		}
		
		return str.toString();
	}

	/**
	 * Generate F Prime port method declarations for UML Interface operations
	 */
	public String generateInterfacePortDeclarations(Resource input) {
		StringConcatenation str = new StringConcatenation();
		
		TreeSet<String> interfaceNames = collectInterfaces(input);
		
		if (!interfaceNames.isEmpty()) {
			str.append("      // UML Interface port handler declarations");
			str.newLine();
			
			for (String interfaceName : interfaceNames) {
				Interface umlInterface = findInterfaceByName(input, interfaceName);
				if (umlInterface != null) {
					str.append(generatePortHandlerDeclarations(umlInterface));
				}
			}
		}
		
		return str.toString();
	}

	/**
	 * Generate F Prime port handler implementations for UML Interface operations
	 */
	public String generateInterfacePortImplementations(String componentName, Resource input) {
		StringConcatenation str = new StringConcatenation();
		
		TreeSet<String> interfaceNames = collectInterfaces(input);
		
		if (!interfaceNames.isEmpty()) {
			str.append("  // UML Interface port handler implementations");
			str.newLine();
			str.newLine();
			
			for (String interfaceName : interfaceNames) {
				Interface umlInterface = findInterfaceByName(input, interfaceName);
				if (umlInterface != null) {
					str.append(generatePortHandlerImplementations(componentName, umlInterface));
				}
			}
		}
		
		return str.toString();
	}

	/**
	 * Collect all UML Interface names that should be processed
	 */
	private TreeSet<String> collectInterfaces(Resource input) {
		TreeSet<String> interfaceNames = new TreeSet<String>();
		
		// Check UML Interfaces
		final TreeIterator<EObject> allContents = input.getAllContents();
		while (allContents.hasNext()) {
			EObject obj = allContents.next();
			if (obj instanceof Interface) {
				Interface iface = (Interface) obj;
				if (mQInterface.isToBeGenerated(iface)) {
					interfaceNames.add(iface.getName());
				}
			} else if (obj instanceof Class) {
				Class clazz = (Class) obj;
				if (mQInterface.isToBeGenerated(clazz)) {
					interfaceNames.add(clazz.getName());
				}
			}
		}
		
		return interfaceNames;
	}

	/**
	 * Find a UML Interface by name in the resource
	 */
	private Interface findInterfaceByName(Resource input, String interfaceName) {
		final TreeIterator<EObject> allContents = input.getAllContents();
		while (allContents.hasNext()) {
			EObject obj = allContents.next();
			if (obj instanceof Interface) {
				Interface iface = (Interface) obj;
				if (iface.getName().equals(interfaceName) && mQInterface.isToBeGenerated(iface)) {
					return iface;
				}
			}
		}
		return null;
	}

	/**
	 * Find a UML Class (SysML Block interface) by name in the resource
	 */
	private Class findClassInterfaceByName(Resource input, String interfaceName) {
		final TreeIterator<EObject> allContents = input.getAllContents();
		while (allContents.hasNext()) {
			EObject obj = allContents.next();
			if (obj instanceof Class) {
				Class clazz = (Class) obj;
				if (clazz.getName().equals(interfaceName) && mQInterface.isToBeGenerated(clazz)) {
					return clazz;
				}
			}
		}
		return null;
	}

	/**
	 * Generate F Prime port definitions for a single UML Interface
	 */
	private String generatePortsForInterface(Interface umlInterface) {
		StringConcatenation str = new StringConcatenation();
		
		String interfaceName = umlInterface.getName();
		
		// Generate input ports for operations that the component provides
		for (final Operation op : umlInterface.getOwnedOperations()) {
			str.append("  sync input port " + interfaceName.toLowerCase() + "_" + op.getName() + ": ");
			str.append(generatePortType(op));
			str.newLine();
		}
		
		// Generate output ports for operations that the component requires
		if (mQInterface.hasRequests(umlInterface)) {
			for (final Reception reception : umlInterface.getOwnedReceptions()) {
				if (mQStereotype.isComodoCommand((Element) reception.getSignal())) {
					str.append("  output port " + interfaceName.toLowerCase() + "_req_" + reception.getName() + ": ");
					str.append("Fw.Cmd");  // F Prime command port type
					str.newLine();
				}
			}
		}
		
		return str.toString();
	}

	/**
	 * Generate F Prime port handler method declarations for a UML Interface
	 */
	private String generatePortHandlerDeclarations(Interface umlInterface) {
		StringConcatenation str = new StringConcatenation();
		
		String interfaceName = umlInterface.getName();
		
		// Generate handler declarations for each operation
		for (final Operation op : umlInterface.getOwnedOperations()) {
			str.append("      void " + interfaceName.toLowerCase() + "_" + op.getName() + "_handler(");
			str.newLine();
			
			// Add parameters based on operation signature
			boolean hasParams = !op.getOwnedParameters().isEmpty();
			if (hasParams) {
				str.append("          const FwIndexType portNum");
				// Add operation parameters
				for (final Parameter param : op.getOwnedParameters()) {
					if (param.getDirection() == null || !param.getDirection().getName().equals("return")) {
						str.append(",");
						str.newLine();
						str.append("          const " + mapUMLTypeToFPrime(param.getType()) + " " + param.getName());
					}
				}
			} else {
				str.append("const FwIndexType portNum");
			}
			
			str.newLine();
			str.append("      );");
			str.newLine();
		}
		
		return str.toString();
	}

	/**
	 * Generate F Prime port handler implementations for a UML Interface
	 */
	private String generatePortHandlerImplementations(String componentName, Interface umlInterface) {
		StringConcatenation str = new StringConcatenation();
		
		String interfaceName = umlInterface.getName();
		
		// Generate handler implementations for each operation
		for (final Operation op : umlInterface.getOwnedOperations()) {
			str.append("  void " + componentName + "ComponentImpl::" + interfaceName.toLowerCase() + "_" + op.getName() + "_handler(");
			str.newLine();
			
			// Add parameters
			boolean hasParams = !op.getOwnedParameters().isEmpty();
			if (hasParams) {
				str.append("      const FwIndexType portNum");
				for (final Parameter param : op.getOwnedParameters()) {
					if (param.getDirection() == null || !param.getDirection().getName().equals("return")) {
						str.append(",");
						str.newLine();
						str.append("      const " + mapUMLTypeToFPrime(param.getType()) + " " + param.getName());
					}
				}
			} else {
				str.append("const FwIndexType portNum");
			}
			
			str.newLine();
			str.append("  )");
			str.newLine();
			str.append("  {");
			str.newLine();
			
			// Generate implementation body
			str.append("    // Interface operation: " + interfaceName + "::" + op.getName());
			str.newLine();
			str.append("    // TODO: Implement " + op.getName() + " operation logic");
			str.newLine();
			
			// Add return value handling if operation has return type
			for (final Parameter param : op.getOwnedParameters()) {
				if (param.getDirection() != null && param.getDirection().getName().equals("return")) {
					str.append("    // Return type: " + mapUMLTypeToFPrime(param.getType()));
					str.newLine();
					break;
				}
			}
			
			str.append("  }");
			str.newLine();
			str.newLine();
		}
		
		return str.toString();
	}

	/**
	 * Generate F Prime port type from UML Operation
	 */
	private String generatePortType(Operation op) {
		// For now, use a generic F Prime interface type
		// This could be enhanced to generate custom port types
		return "Fw.PrmGet";  // Generic F Prime port type
	}

	/**
	 * Map UML types to F Prime types
	 */
	private String mapUMLTypeToFPrime(org.eclipse.uml2.uml.Type umlType) {
		if (umlType == null) {
			return "U32";  // Default F Prime type
		}
		
		String typeName = umlType.getName();
		if (typeName == null) {
			return "U32";
		}
		
		// Map common UML types to F Prime types
		switch (typeName.toLowerCase()) {
			case "integer":
			case "int":
				return "I32";
			case "boolean":
			case "bool":
				return "bool";
			case "string":
				return "Fw::String";
			case "real":
			case "double":
				return "F64";
			case "float":
				return "F32";
			default:
				return "U32";  // Default fallback
		}
	}

	/**
	 * Check if the model contains any UML Interfaces
	 */
	public boolean hasInterfaces(Resource input) {
		return !collectInterfaces(input).isEmpty();
	}
}