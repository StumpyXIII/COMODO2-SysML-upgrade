package comodo2.templates.fprime;

import com.google.common.collect.Iterables;
import comodo2.queries.QClass;
import comodo2.queries.QInterface;
import comodo2.queries.QState;
import comodo2.queries.QStateMachine;
import comodo2.queries.QStereotype;
import comodo2.queries.QTransition;
import java.util.TreeSet;
import javax.inject.Inject;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Reception;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.xtend2.lib.StringConcatenation;

/**
 * F Prime XML Generator
 * Converts UML 5.x XMI models to F Prime XML component descriptors
 * that can be processed by F Prime's native fpp-to-cpp toolchain.
 */
public class FPrimeXMLGenerator {

	@Inject
	private QStateMachine mQStateMachine;

	@Inject
	private QState mQState;

	@Inject
	private QTransition mQTransition;

	@Inject
	private QClass mQClass;

	@Inject
	private QStereotype mQStereotype;

	@Inject
	private QInterface mQInterface;

	/**
	 * Generate F Prime component XML descriptor from UML XMI
	 */
	public String generateComponentXML(Class componentClass, StateMachine stateMachine, Resource input) {
		StringConcatenation str = new StringConcatenation();
		String componentName = componentClass.getName();
		
		str.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		str.newLine();
		str.append("<!-- Auto-generated F Prime component XML from UML 5.x XMI -->");
		str.newLine();
		str.append("<!-- Source: " + componentClass.getName() + " StateMachine: " + stateMachine.getName() + " -->");
		str.newLine();
		str.newLine();

		// Component definition
		str.append("<component");
		str.append(" name=\"" + componentName + "\"");
		str.append(" kind=\"active\"");  // State machines are active components
		str.append(" namespace=\"Components\"");
		str.append(">");
		str.newLine();
		str.newLine();

		// Import standard F Prime types
		str.append("  <import_dictionary>Fw/Cfg/ConfigCheck.hpp</import_dictionary>");
		str.newLine();
		str.append("  <import_dictionary>Fw/Types/BasicTypes.hpp</import_dictionary>");
		str.newLine();
		str.append("  <import_dictionary>Fw/Types/StringType.hpp</import_dictionary>");
		str.newLine();
		str.newLine();

		// Generate ports from UML model
		str.append("  <ports>");
		str.newLine();
		
		// Standard F Prime ports
		str.append(generateStandardPorts());
		str.newLine();
		
		// Ports from UML Interfaces
		str.append(generateInterfacePorts(input));
		str.newLine();
		
		str.append("  </ports>");
		str.newLine();
		str.newLine();

		// Generate commands from UML StateMachine events and Signals
		str.append("  <commands>");
		str.newLine();
		str.append(generateStateMachineCommands(stateMachine));
		str.newLine();
		str.append(generateSignalCommands(stateMachine));
		str.newLine();
		str.append(generateStandardCommands());
		str.newLine();
		str.append("  </commands>");
		str.newLine();
		str.newLine();

		// Generate telemetry
		str.append("  <telemetry>");
		str.newLine();
		str.append("    <channel name=\"CurrentState\" data_type=\"string\" id=\"0\" abbrev=\"State\"/>");
		str.newLine();
		str.append("  </telemetry>");
		str.newLine();
		str.newLine();

		// Generate events
		str.append("  <events>");
		str.newLine();
		str.append("    <event name=\"StateTransition\" id=\"0\" severity=\"ACTIVITY_HI\" throttle=\"10\" format_string=\"State machine transitioned from %s to %s\">");
		str.newLine();
		str.append("      <comment>State machine transitioned from {0} to {1}</comment>");
		str.newLine();
		str.append("      <args>");
		str.newLine();
		str.append("        <arg name=\"from\" type=\"string\"/>");
		str.newLine();
		str.append("        <arg name=\"to\" type=\"string\"/>");
		str.newLine();
		str.append("      </args>");
		str.newLine();
		str.append("    </event>");
		str.newLine();
		str.append("  </events>");
		str.newLine();
		str.newLine();

		str.append("</component>");
		str.newLine();

		return str.toString();
	}

	/**
	 * Generate standard F Prime component ports
	 */
	private String generateStandardPorts() {
		StringConcatenation str = new StringConcatenation();
		
		str.append("    <!-- Standard F Prime component ports -->");
		str.newLine();
		str.append("    <port name=\"schedIn\" data_type=\"Svc::Sched\" kind=\"sync_input\" max_number=\"1\"/>");
		str.newLine();
		str.append("    <port name=\"cmdRegOut\" data_type=\"Fw::CmdReg\" kind=\"output\" max_number=\"1\"/>");
		str.newLine();
		str.append("    <port name=\"cmdResponseOut\" data_type=\"Fw::CmdResponse\" kind=\"output\" max_number=\"1\"/>");
		str.newLine();
		str.append("    <port name=\"eventOut\" data_type=\"Fw::LogEvent\" kind=\"output\" max_number=\"1\"/>");
		str.newLine();
		str.append("    <port name=\"tlmOut\" data_type=\"Fw::Tlm\" kind=\"output\" max_number=\"1\"/>");
		str.newLine();

		return str.toString();
	}

	/**
	 * Generate ports from UML Interfaces in the XMI model
	 */
	private String generateInterfacePorts(Resource input) {
		StringConcatenation str = new StringConcatenation();
		TreeSet<String> interfaceNames = collectInterfaces(input);
		
		if (!interfaceNames.isEmpty()) {
			str.append("    <!-- Ports derived from UML Interfaces -->");
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
	 * Generate F Prime port XML for a single UML Interface
	 */
	private String generatePortsForInterface(Interface umlInterface) {
		StringConcatenation str = new StringConcatenation();
		String interfaceName = umlInterface.getName();
		
		// Generate sync input ports for operations that the component provides
		for (final Operation op : umlInterface.getOwnedOperations()) {
			str.append("    <port name=\"" + interfaceName.toLowerCase() + "_" + op.getName() + "\"");
			str.append(" data_type=\"" + generatePortType(op) + "\"");
			str.append(" kind=\"sync_input\"");
			str.append(" max_number=\"1\"/>");
			str.newLine();
		}
		
		// Generate output ports for operations that the component requires
		if (mQInterface.hasRequests(umlInterface)) {
			for (final Reception reception : umlInterface.getOwnedReceptions()) {
				if (mQStereotype.isComodoCommand((Element) reception.getSignal())) {
					str.append("    <port name=\"" + interfaceName.toLowerCase() + "_req_" + reception.getName() + "\"");
					str.append(" data_type=\"Fw::Cmd\"");
					str.append(" kind=\"output\"");  
					str.append(" max_number=\"1\"/>");
					str.newLine();
				}
			}
		}
		
		return str.toString();
	}

	/**
	 * Generate commands from UML StateMachine events
	 */
	private String generateStateMachineCommands(StateMachine stateMachine) {
		StringConcatenation str = new StringConcatenation();
		TreeSet<String> eventNames = collectEventNames(stateMachine);
		
		if (!eventNames.isEmpty()) {
			str.append("    <!-- Commands derived from UML StateMachine events -->");
			str.newLine();
			
			int opcodeCounter = 0x100;
			for (String eventName : eventNames) {
				str.append("    <command name=\"" + eventName.toUpperCase() + "\" kind=\"async\" opcode=\"0x" + 
					Integer.toHexString(opcodeCounter++).toUpperCase() + "\" mnemonic=\"" + eventName + "\"/>");
				str.newLine();
			}
		}
		
		return str.toString();
	}

	/**
	 * Generate commands from UML Signals
	 */
	private String generateSignalCommands(StateMachine stateMachine) {
		StringConcatenation str = new StringConcatenation();
		TreeSet<String> signalNames = collectSignalNames(stateMachine);
		
		if (!signalNames.isEmpty()) {
			str.append("    <!-- Commands derived from UML Signals -->");
			str.newLine();
			
			int opcodeCounter = 0x200;
			for (String signalName : signalNames) {
				str.append("    <command name=\"" + signalName.toUpperCase() + "\" kind=\"async\" opcode=\"0x" + 
					Integer.toHexString(opcodeCounter++).toUpperCase() + "\" mnemonic=\"" + signalName + "\"/>");
				str.newLine();
			}
		}
		
		return str.toString();
	}

	/**
	 * Generate standard state machine control commands
	 */
	private String generateStandardCommands() {
		StringConcatenation str = new StringConcatenation();
		
		str.append("    <!-- Standard state machine control commands -->");
		str.newLine();
		str.append("    <command name=\"START_STATE_MACHINE\" kind=\"async\" opcode=\"0x00\" mnemonic=\"StartSM\"/>");
		str.newLine();
		str.append("    <command name=\"STOP_STATE_MACHINE\" kind=\"async\" opcode=\"0x01\" mnemonic=\"StopSM\"/>");
		str.newLine();
		
		return str.toString();
	}

	/**
	 * Collect all UML Interface names that should be processed
	 */
	private TreeSet<String> collectInterfaces(Resource input) {
		TreeSet<String> interfaceNames = new TreeSet<String>();
		
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
	 * Find a UML Interface by name in the XMI resource
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
	 * Collect all UML Event names used in StateMachine transitions
	 */
	private TreeSet<String> collectEventNames(StateMachine stateMachine) {
		TreeSet<String> eventNames = new TreeSet<String>();
		
		for (final State state : Iterables.<State>filter(stateMachine.allOwnedElements(), State.class)) {
			if (mQState.isTopState(state)) {
				for (final Transition transition : state.getOutgoings()) {
					if (mQTransition.hasEvent(transition)) {
						String eventName = mQTransition.getFirstEventName(transition);
						if (eventName != null && !eventName.trim().isEmpty()) {
							eventNames.add(eventName.trim());
						}
					}
				}
			}
		}
		
		return eventNames;
	}

	/**
	 * Collect all UML Signal names used in StateMachine transitions
	 */
	private TreeSet<String> collectSignalNames(StateMachine stateMachine) {
		TreeSet<String> signalNames = new TreeSet<String>();
		
		for (final State state : Iterables.<State>filter(stateMachine.allOwnedElements(), State.class)) {
			if (mQState.isTopState(state)) {
				for (final Transition transition : state.getOutgoings()) {
					if (mQTransition.hasSignalEvent(transition)) {
						String signalName = mQTransition.getFirstEventName(transition);
						if (signalName != null && !signalName.trim().isEmpty()) {
							signalNames.add(signalName.trim());
						}
					}
				}
			}
		}
		
		return signalNames;
	}

	/**
	 * Generate F Prime port type from UML Operation
	 */
	private String generatePortType(Operation op) {
		// For now, use a generic F Prime interface type
		// This could be enhanced to generate custom port types based on operation signature
		return "Fw::PrmGet";
	}

	/**
	 * Map UML types to F Prime XML types
	 */
	private String mapUMLTypeToFPrime(org.eclipse.uml2.uml.Type umlType) {
		if (umlType == null) {
			return "U32";
		}
		
		String typeName = umlType.getName();
		if (typeName == null) {
			return "U32";
		}
		
		switch (typeName.toLowerCase()) {
			case "integer":
			case "int":
				return "I32";
			case "boolean":
			case "bool":
				return "bool";
			case "string":
				return "string";
			case "real":
			case "double":
				return "F64";
			case "float":
				return "F32";
			default:
				return "U32";
		}
	}

	/**
	 * Generate F Prime topology XML for component instances and connections
	 */
	public String generateTopologyXML(Resource input, String topologyName) {
		StringConcatenation str = new StringConcatenation();
		
		str.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		str.newLine();
		str.append("<!-- Auto-generated F Prime topology XML from UML 5.x XMI -->");
		str.newLine();
		str.newLine();

		str.append("<topology name=\"" + topologyName + "\">");
		str.newLine();
		str.newLine();

		// Import component XML files
		str.append("  <import_component_type>");
		str.newLine();
		str.append("    <!-- Component imports will be added here -->");
		str.newLine();
		str.append("  </import_component_type>");
		str.newLine();
		str.newLine();

		// Component instances
		str.append("  <instances>");
		str.newLine();
		str.append("    <!-- Component instances will be added here -->");
		str.newLine();
		str.append("  </instances>");
		str.newLine();
		str.newLine();

		// Component connections
		str.append("  <connections>");
		str.newLine();
		str.append("    <!-- Component connections will be added here -->");
		str.newLine();
		str.append("  </connections>");
		str.newLine();
		str.newLine();

		str.append("</topology>");
		str.newLine();

		return str.toString();
	}
}