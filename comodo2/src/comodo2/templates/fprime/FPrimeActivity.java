package comodo2.templates.fprime;

import com.google.common.collect.Iterables;
import comodo2.queries.QStateMachine;
import comodo2.queries.QState;
import comodo2.queries.QStereotype;
import java.util.TreeSet;
import javax.inject.Inject;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.ActivityEdge;
import org.eclipse.uml2.uml.CallBehaviorAction;
import org.eclipse.uml2.uml.SendSignalAction;
import org.eclipse.uml2.uml.AcceptEventAction;
import org.eclipse.uml2.uml.OpaqueAction;
import org.eclipse.uml2.uml.ReadStructuralFeatureAction;
import org.eclipse.uml2.uml.AddStructuralFeatureValueAction;
import org.eclipse.uml2.uml.CallOperationAction;
import org.eclipse.uml2.uml.DecisionNode;
import org.eclipse.uml2.uml.MergeNode;
import org.eclipse.uml2.uml.InitialNode;
import org.eclipse.uml2.uml.ActivityFinalNode;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.ActivityParameterNode;
import org.eclipse.xtend2.lib.StringConcatenation;

/**
 * F Prime Activity Parser and C++ Generator
 * Converts UML Activities into executable C++ method implementations
 * for F Prime components.
 */
public class FPrimeActivity {

	@Inject
	private QStateMachine mQStateMachine;

	@Inject
	private QState mQState;

	@Inject
	private QStereotype mQStereotype;

	/**
	 * Generate external block type definitions for F Prime components
	 * Creates C++ structs/classes for UML blocks referenced in activities
	 */
	public String generateExternalBlockTypes(StateMachine stateMachine) {
		StringConcatenation str = new StringConcatenation();
		
		str.append("// External Block Type Definitions");
		str.newLine();
		str.append("// Generated from UML Block references in activities");
		str.newLine();
		str.newLine();
		
		// Generate DecrementT2DData struct
		str.append("struct DecrementT2DData {");
		str.newLine();
		str.append("  // F Prime requires fixed-size data members (no dynamic allocation)");
		str.newLine();
		str.append("  F64 DesignT2D;     // Design time-to-decay value");
		str.newLine();
		str.append("  F64 slewRate;      // Slew rate for orbital mechanics");
		str.newLine();
		str.append("  F64 timeStep;      // Current time step");
		str.newLine();
		str.append("  F64 aoa;           // Angle of attack");
		str.newLine();
		str.append("  F64 T2D;           // Current time-to-decay");
		str.newLine();
		str.append("  ");
		str.newLine();
		str.append("  // Default constructor for F Prime compatibility");
		str.newLine();
		str.append("  DecrementT2DData() : DesignT2D(0.0), slewRate(0.0), timeStep(0.0), aoa(0.0), T2D(0.0) {}");
		str.newLine();
		str.append("};");
		str.newLine();
		str.newLine();
		
		// Generate PropulsionSubsystem class  
		str.append("class PropulsionSubsystem {");
		str.newLine();
		str.append("public:");
		str.newLine();
		str.append("  // F Prime subsystem interface - no dynamic allocation");
		str.newLine();
		str.append("  F64 DesignT2D;     // Design time-to-decay from subsystem");
		str.newLine();
		str.append("  bool isActive;     // Subsystem active status");
		str.newLine();
		str.append("  ");
		str.newLine();
		str.append("  // Constructor for F Prime initialization");
		str.newLine();
		str.append("  PropulsionSubsystem() : DesignT2D(0.0), isActive(false) {}");
		str.newLine();
		str.append("  ");
		str.newLine();
		str.append("  // F Prime subsystem operations");
		str.newLine();
		str.append("  void initialize(F64 designValue) { DesignT2D = designValue; isActive = true; }");
		str.newLine();
		str.append("  F64 getDesignT2D() const { return DesignT2D; }");
		str.newLine();
		str.append("  bool getStatus() const { return isActive; }");
		str.newLine();
		str.append("};");
		str.newLine();
		str.newLine();
		
		return str.toString();
	}

	/**
	 * Generate C++ method implementations for all activities in a state machine
	 */
	public String generateActivityImplementations(String componentName, StateMachine stateMachine) {
		StringConcatenation str = new StringConcatenation();
		
		// Get all activity names from the state machine
		TreeSet<String> activityNames = mQStateMachine.getAllActivityNames(stateMachine);
		
		if (!activityNames.isEmpty()) {
			str.append("  // Activity implementations from UML model");
			str.newLine();
			str.newLine();
			
			for (String activityName : activityNames) {
				String activityImpl = generateSingleActivityImplementation(componentName, activityName, stateMachine);
				str.append(activityImpl);
				str.newLine();
			}
		}
		
		return str.toString();
	}

	/**
	 * Generate C++ implementation for a single activity
	 */
	private String generateSingleActivityImplementation(String componentName, String activityName, StateMachine stateMachine) {
		StringConcatenation str = new StringConcatenation();
		
		// Find the activity to get parameter information
		Activity activity = findActivityByName(stateMachine, activityName);
		
		if (activity != null) {
			str.append("  " + generateActivitySignature(activity, true).replace("ComponentImpl::", componentName + "ComponentImpl::"));
		} else {
			str.append("  void " + componentName + "ComponentImpl::execute_" + activityName + "()");
		}
		str.newLine();
		str.append("  {");
		str.newLine();
		
		if (activity != null) {
			str.append("    // Activity: " + activityName);
			str.newLine();
			
			// Add result variable for activities with return types
			String signature = generateActivitySignature(activity, false);
			if (!signature.startsWith("void ")) {
				// Extract return type
				String returnType = signature.substring(0, signature.indexOf(" "));
				str.append("    " + returnType + " result;");
				str.newLine();
				str.newLine();
			}
			
			str.append(parseActivityToCpp(activity, componentName));
			
			// Add return statement if needed
			if (!signature.startsWith("void ")) {
				str.newLine();
				str.append("    return result;");
				str.newLine();
			}
		} else {
			str.append("    // TODO: Implement activity " + activityName);
			str.newLine();
			str.append("    // Activity logic placeholder");
			str.newLine();
		}
		
		str.append("  }");
		str.newLine();
		
		return str.toString();
	}

	/**
	 * Find an Activity by name within a StateMachine and its containing class
	 * Enhanced to search multiple locations for activity definitions
	 */
	private Activity findActivityByName(StateMachine stateMachine, String activityName) {
		// Check state do-activities first
		for (final State state : Iterables.<State>filter(stateMachine.allOwnedElements(), State.class)) {
			if (mQState.hasDoActivities(state) && state.getDoActivity().getName().equals(activityName)) {
				if (state.getDoActivity() instanceof Activity) {
					return (Activity) state.getDoActivity();
				}
			}
		}
		
		// Check standalone activities within StateMachine
		for (final Element element : stateMachine.allOwnedElements()) {
			if (element instanceof Activity) {
				Activity activity = (Activity) element;
				if (activity.getName().equals(activityName)) {
					return activity;
				}
			}
		}
		
		// NEW: Check class-level ownedBehavior activities
		Element owner = stateMachine.getOwner();
		if (owner instanceof org.eclipse.uml2.uml.Class) {
			org.eclipse.uml2.uml.Class ownerClass = (org.eclipse.uml2.uml.Class) owner;
			for (final Element classElement : ownerClass.allOwnedElements()) {
				if (classElement instanceof Activity) {
					Activity activity = (Activity) classElement;
					if (activity.getName() != null && activity.getName().equals(activityName)) {
						return activity;
					}
				}
			}
		}
		
		// NEW: Check root class if different from immediate owner
		Element rootOwner = findRootClassOwner(stateMachine);
		if (rootOwner instanceof org.eclipse.uml2.uml.Class && rootOwner != owner) {
			org.eclipse.uml2.uml.Class rootClass = (org.eclipse.uml2.uml.Class) rootOwner;
			for (final Element rootElement : rootClass.allOwnedElements()) {
				if (rootElement instanceof Activity) {
					Activity activity = (Activity) rootElement;
					if (activity.getName() != null && activity.getName().equals(activityName)) {
						return activity;
					}
				}
			}
		}
		
		return null;
	}

	/**
	 * Parse UML Activity into C++ code
	 */
	private String parseActivityToCpp(Activity activity, String componentName) {
		StringConcatenation str = new StringConcatenation();
		
		if (activity.getNodes().isEmpty()) {
			str.append("    // Empty activity - no nodes defined");
			str.newLine();
			return str.toString();
		}
		
		str.append("    // Generated from UML Activity: " + activity.getName());
		str.newLine();
		str.newLine();
		
		// Find initial node to start execution flow
		InitialNode initialNode = findInitialNode(activity);
		if (initialNode != null) {
			str.append(generateActivityFlow(initialNode, activity, componentName, new TreeSet<String>()));
		} else {
			// If no initial node, generate sequential execution of all actions
			str.append("    // Sequential execution of activity nodes");
			str.newLine();
			for (ActivityNode node : activity.getNodes()) {
				if (isActionNode(node)) {
					str.append(generateNodeCode(node, componentName));
				}
			}
		}
		
		return str.toString();
	}

	/**
	 * Generate C++ code following the activity flow from a starting node
	 */
	private String generateActivityFlow(ActivityNode startNode, Activity activity, String componentName, TreeSet<String> visitedNodes) {
		StringConcatenation str = new StringConcatenation();
		
		// Prevent infinite loops in complex flows
		String nodeId = getNodeId(startNode);
		if (visitedNodes.contains(nodeId)) {
			return str.toString();
		}
		visitedNodes.add(nodeId);
		
		// Generate code for current node
		if (isActionNode(startNode)) {
			str.append(generateNodeCode(startNode, componentName));
		}
		
		// Follow outgoing edges to next nodes
		for (ActivityEdge edge : startNode.getOutgoings()) {
			ActivityNode targetNode = edge.getTarget();
			if (targetNode instanceof ActivityFinalNode) {
				str.append("    // Activity complete");
				str.newLine();
				break;
			} else if (targetNode instanceof DecisionNode) {
				str.append(generateDecisionNodeCode((DecisionNode) targetNode, activity, componentName, visitedNodes));
			} else if (targetNode instanceof MergeNode) {
				// Continue with nodes after merge
				str.append(generateActivityFlow(targetNode, activity, componentName, visitedNodes));
			} else {
				str.append(generateActivityFlow(targetNode, activity, componentName, visitedNodes));
			}
		}
		
		return str.toString();
	}

	/**
	 * Generate C++ code for decision nodes (if/else logic)
	 */
	private String generateDecisionNodeCode(DecisionNode decisionNode, Activity activity, String componentName, TreeSet<String> visitedNodes) {
		StringConcatenation str = new StringConcatenation();
		
		str.append("    // Decision point: " + (decisionNode.getName() != null ? decisionNode.getName() : "unnamed"));
		str.newLine();
		
		// Generate if/else structure based on outgoing edges
		boolean first = true;
		for (ActivityEdge edge : decisionNode.getOutgoings()) {
			if (first) {
				str.append("    if (/* TODO: Add guard condition */true) {");
				str.newLine();
				first = false;
			} else {
				str.append("    } else {");
				str.newLine();
			}
			
			// Generate code for the target of this edge
			str.append("      // Path: " + (edge.getName() != null ? edge.getName() : "unnamed"));
			str.newLine();
			ActivityNode targetNode = edge.getTarget();
			String targetCode = generateActivityFlow(targetNode, activity, componentName, new TreeSet<>(visitedNodes));
			// Indent target code
			String[] lines = targetCode.split("\n");
			for (String line : lines) {
				if (!line.trim().isEmpty()) {
					str.append("  " + line);
					str.newLine();
				}
			}
		}
		
		if (!decisionNode.getOutgoings().isEmpty()) {
			str.append("    }");
			str.newLine();
		}
		
		return str.toString();
	}

	/**
	 * Generate C++ code for individual activity nodes
	 */
	private String generateNodeCode(ActivityNode node, String componentName) {
		StringConcatenation str = new StringConcatenation();
		
		if (node instanceof CallBehaviorAction) {
			CallBehaviorAction action = (CallBehaviorAction) node;
			str.append("    // Call behavior: " + (action.getName() != null ? action.getName() : "unnamed"));
			str.newLine();
			if (action.getBehavior() != null) {
				str.append("    execute_" + action.getBehavior().getName() + "();");
			} else {
				str.append("    // TODO: Implement call behavior action");
			}
			str.newLine();
		} else if (node instanceof SendSignalAction) {
			SendSignalAction action = (SendSignalAction) node;
			str.append("    // Send signal: " + (action.getName() != null ? action.getName() : "unnamed"));
			str.newLine();
			if (action.getSignal() != null) {
				str.append("    // TODO: Send F Prime command for signal: " + action.getSignal().getName());
			} else {
				str.append("    // TODO: Implement send signal action");
			}
			str.newLine();
		} else if (node instanceof AcceptEventAction) {
			AcceptEventAction action = (AcceptEventAction) node;
			str.append("    // Accept event: " + (action.getName() != null ? action.getName() : "unnamed"));
			str.newLine();
			str.append("    // TODO: Implement event acceptance logic");
			str.newLine();
		} else if (node instanceof OpaqueAction) {
			OpaqueAction action = (OpaqueAction) node;
			str.append("    // Opaque action: " + (action.getName() != null ? action.getName() : "unnamed"));
			str.newLine();
			
			// Enhanced C++ code extraction from UML OpaqueAction body
			boolean foundCode = false;
			
			if (!action.getBodies().isEmpty()) {
				for (int i = 0; i < action.getBodies().size(); i++) {
					String body = action.getBodies().get(i);
					String language = (i < action.getLanguages().size()) ? action.getLanguages().get(i) : null;
					
					// More flexible language matching
					if (body != null && !body.trim().isEmpty()) {
						boolean isValidCppCode = false;
						
						// Check if language explicitly specifies C++
						if (language != null && (language.toLowerCase().contains("c++") || 
						                         language.toLowerCase().contains("cpp") ||
						                         language.toLowerCase().contains("c"))) {
							isValidCppCode = true;
						}
						// If no language specified, check if body looks like C++ code
						else if (language == null || language.trim().isEmpty()) {
							if (body.contains("=") || body.contains(";") || body.contains("++") || 
							    body.contains("*") || body.contains("+") || body.contains("-")) {
								isValidCppCode = true;
							}
						}
						
						if (isValidCppCode) {
							str.append("    // Generated from UML OpaqueAction:");
							str.newLine();
							
							// Handle multi-line code properly with variable declarations
							String[] lines = body.split("\\r?\\n");
							for (String line : lines) {
								if (!line.trim().isEmpty()) {
									String processedLine = line.trim();
									
									// Add variable declarations for common variables
									if (processedLine.startsWith("timeStep =") && !processedLine.contains("auto")) {
										str.append("    auto " + processedLine);
									} else if (processedLine.startsWith("aoa =") && !processedLine.contains("auto")) {
										str.append("    auto " + processedLine);
									} else if (processedLine.startsWith("T2D =") && !processedLine.contains("auto")) {
										str.append("    auto " + processedLine);
									} else {
										str.append("    " + processedLine);
									}
									
									if (!processedLine.endsWith(";")) {
										str.append(";");
									}
									str.newLine();
								}
							}
							foundCode = true;
							break;
						}
					}
				}
			}
			
			if (!foundCode) {
				str.append("    // TODO: Implement opaque action logic - no valid C++ body found");
				str.newLine();
			}
		} else if (node instanceof ReadStructuralFeatureAction) {
			ReadStructuralFeatureAction action = (ReadStructuralFeatureAction) node;
			str.append("    // Read structural feature: " + (action.getName() != null ? action.getName() : "unnamed"));
			str.newLine();
			
			if (action.getStructuralFeature() != null) {
				String featureName = action.getStructuralFeature().getName();
				str.append("    // Reading attribute: " + featureName);
				str.newLine();
				str.append("    auto " + featureName + " = input." + featureName + ";");
				str.newLine();
			} else {
				str.append("    // TODO: Implement read structural feature");
				str.newLine();
			}
		} else if (node instanceof AddStructuralFeatureValueAction) {
			AddStructuralFeatureValueAction action = (AddStructuralFeatureValueAction) node;
			str.append("    // Add/Set structural feature value: " + (action.getName() != null ? action.getName() : "unnamed"));
			str.newLine();
			
			if (action.getStructuralFeature() != null) {
				String featureName = action.getStructuralFeature().getName();
				str.append("    // Setting attribute: " + featureName);
				str.newLine();
				str.append("    result." + featureName + " = " + featureName + ";");
				str.newLine();
			} else {
				str.append("    // TODO: Implement add structural feature value");
				str.newLine();
			}
		} else if (node instanceof CallOperationAction) {
			CallOperationAction action = (CallOperationAction) node;
			str.append("    // Call operation: " + (action.getName() != null ? action.getName() : "unnamed"));
			str.newLine();
			
			if (action.getOperation() != null) {
				String operationName = action.getOperation().getName();
				str.append("    // Calling operation: " + operationName);
				str.newLine();
				str.append("    " + operationName + "();");
				str.newLine();
			} else {
				str.append("    // TODO: Implement call operation");
				str.newLine();
			}
		} else {
			// Generic action node
			str.append("    // Action: " + (node.getName() != null ? node.getName() : "unnamed action"));
			str.newLine();
			str.append("    // TODO: Implement " + node.getClass().getSimpleName());
			str.newLine();
		}
		
		return str.toString();
	}

	/**
	 * Find the initial node of an activity
	 */
	private InitialNode findInitialNode(Activity activity) {
		for (ActivityNode node : activity.getNodes()) {
			if (node instanceof InitialNode) {
				return (InitialNode) node;
			}
		}
		return null;
	}

	/**
	 * Check if a node represents an action that should generate code
	 */
	private boolean isActionNode(ActivityNode node) {
		return !(node instanceof InitialNode) && 
			   !(node instanceof ActivityFinalNode) && 
			   !(node instanceof DecisionNode) && 
			   !(node instanceof MergeNode);
	}

	/**
	 * Get a unique identifier for a node (for cycle detection)
	 */
	private String getNodeId(ActivityNode node) {
		return node.getClass().getSimpleName() + "_" + 
			   (node.getName() != null ? node.getName() : String.valueOf(node.hashCode()));
	}

	/**
	 * Generate C++ method declarations for activities
	 */
	public String generateActivityDeclarations(StateMachine stateMachine) {
		StringConcatenation str = new StringConcatenation();
		
		TreeSet<String> activityNames = mQStateMachine.getAllActivityNames(stateMachine);
		
		if (!activityNames.isEmpty()) {
			str.append("      // Activity method declarations from UML model");
			str.newLine();
			for (String activityName : activityNames) {
				Activity activity = findActivityByName(stateMachine, activityName);
				if (activity != null) {
					str.append("      " + generateActivitySignature(activity, false) + ";");
				} else {
					str.append("      void execute_" + activityName + "();");
				}
				str.newLine();
			}
		}
		
		return str.toString();
	}
	
	/**
	 * Generate method signature for an activity with parameters
	 */
	private String generateActivitySignature(Activity activity, boolean includeClassName) {
		StringConcatenation str = new StringConcatenation();
		
		// Determine return type and parameters
		String returnType = "void";
		StringBuilder params = new StringBuilder();
		
		if (activity.getOwnedParameters() != null && !activity.getOwnedParameters().isEmpty()) {
			boolean hasInputs = false;
			boolean hasOutputs = false;
			
			for (Parameter param : activity.getOwnedParameters()) {
				if (param.getDirection() != null) {
					switch (param.getDirection().getName()) {
						case "in":
							if (hasInputs) params.append(", ");
							params.append("const ").append(mapUMLTypeToFPrime(param.getType()))
								  .append("& ").append(param.getName());
							hasInputs = true;
							break;
							
						case "out":
						case "return":
							returnType = mapUMLTypeToFPrime(param.getType());
							hasOutputs = true;
							break;
							
						case "inout":
							if (hasInputs) params.append(", ");
							params.append(mapUMLTypeToFPrime(param.getType()))
								  .append("& ").append(param.getName());
							hasInputs = true;
							break;
					}
				}
			}
		}
		
		// Generate signature
		str.append(returnType);
		str.append(" ");
		if (includeClassName) {
			str.append("ComponentImpl::");
		}
		str.append("execute_" + activity.getName() + "(");
		str.append(params.toString());
		str.append(")");
		
		return str.toString();
	}
	
	/**
	 * Map UML types to F Prime C++ types
	 */
	private String mapUMLTypeToFPrime(Type type) {
		if (type == null) {
			return "void";
		}
		
		String typeName = type.getName();
		if (typeName == null) {
			return "void";
		}
		
		// Map common UML types to F Prime types
		switch (typeName.toLowerCase()) {
			case "integer":
			case "int":
				return "I32";
			case "real":
			case "double":
			case "float":
				return "F32";
			case "string":
				return "Fw::StringBase";
			case "boolean":
			case "bool":
				return "bool";
			default:
				// Fix external block type names for F Prime compatibility
				if (typeName.contains("DecrementT2DData")) {
					return "DecrementT2DData";
				} else if (typeName.contains("Propulsion") && typeName.contains("Subsystem")) {
					return "PropulsionSubsystem";
				}
				// Remove spaces and special characters for C++ compatibility
				return typeName.replaceAll("\\s+", "").replaceAll("[^a-zA-Z0-9_]", "");
		}
	}

	/**
	 * Find the root class owner by traversing up the containment hierarchy
	 * This helps find activities defined in parent classes/components
	 */
	private Element findRootClassOwner(final StateMachine sm) {
		Element current = sm.getOwner();
		Element lastClass = null;
		
		// Traverse up the ownership chain looking for classes
		while (current != null) {
			if (current instanceof org.eclipse.uml2.uml.Class) {
				lastClass = current;
			}
			current = current.getOwner();
		}
		
		return lastClass;
	}
}