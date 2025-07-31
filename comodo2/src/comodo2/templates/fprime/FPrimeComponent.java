package comodo2.templates.fprime;

import com.google.common.collect.Iterables;
import comodo2.engine.Main;
import comodo2.queries.QClass;
import comodo2.queries.QState;
import comodo2.queries.QStateMachine;
import comodo2.queries.QStereotype;
import comodo2.queries.QTransition;
import comodo2.utils.FilesHelper;
import comodo2.utils.StateComparator;
import comodo2.utils.TransitionComparator;
import java.util.TreeSet;
import javax.inject.Inject;
import org.eclipse.uml2.uml.Element;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.IGenerator;

/**
 * F Prime Component Generator
 * Generates F Prime-compatible C++ components from UML Classes and SysML Blocks
 * with embedded state machine and activity logic.
 */
public class FPrimeComponent implements IGenerator {

	private static final Logger mLogger = Logger.getLogger(Main.class);

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
	private FilesHelper mFilesHelper;

	@Inject
	private FPrimeStateMachine mStateMachineGenerator;

	/**
	 * Generate F Prime components from UML Classes and SysML Blocks
	 * that have cmdoComponent stereotype and associated StateMachines.
	 */
	@Override
	public void doGenerate(final Resource input, final IFileSystemAccess fsa) {
		final TreeIterator<EObject> allContents = input.getAllContents();
		while (allContents.hasNext()) {
			EObject e = allContents.next();
			if (e instanceof org.eclipse.uml2.uml.Class) {
				org.eclipse.uml2.uml.Class c = (org.eclipse.uml2.uml.Class)e; 
				// Use Element interface for unified UML Class and SysML Block support
				if ((mQClass.isToBeGenerated((Element)c) && mQClass.hasStateMachines((Element)c))) {
					for (final StateMachine sm : mQClass.getStateMachines((Element)c)) {
						generateFPrimeComponent(c, sm, fsa);
					}
				}				
			}
		}
	}

	/**
	 * Generate complete F Prime component files (.fpp, .hpp, .cpp, CMakeLists.txt)
	 */
	private void generateFPrimeComponent(org.eclipse.uml2.uml.Class componentClass, StateMachine stateMachine, IFileSystemAccess fsa) {
		String componentName = componentClass.getName();
		
		mLogger.info("Generating F Prime component: " + componentName);
		
		// Generate FPP interface definition
		String fppContent = generateFPP(componentClass, stateMachine);
		fsa.generateFile(mFilesHelper.toFPrimeFppFilePath(componentName), fppContent);
		
		// Generate C++ header file
		String hppContent = generateComponentHeader(componentClass, stateMachine);
		fsa.generateFile(mFilesHelper.toFPrimeHeaderFilePath(componentName), hppContent);
		
		// Generate C++ implementation file
		String cppContent = generateComponentImplementation(componentClass, stateMachine);
		fsa.generateFile(mFilesHelper.toFPrimeSourceFilePath(componentName), cppContent);
		
		// Generate CMake integration
		String cmakeContent = generateCMakeFile(componentClass);
		fsa.generateFile(mFilesHelper.toFPrimeCMakeFilePath(componentName), cmakeContent);
	}

	/**
	 * Generate FPP (F Prime Prime) component interface definition
	 */
	private String generateFPP(org.eclipse.uml2.uml.Class componentClass, StateMachine stateMachine) {
		StringConcatenation str = new StringConcatenation();
		
		str.append("# Auto-generated F Prime component interface");
		str.newLine();
		str.append("# Generated from UML/SysML model: " + componentClass.getName());
		str.newLine();
		str.newLine();
		
		// Determine component type (active for state machines)
		str.append("active component " + componentClass.getName() + " {");
		str.newLine();
		str.newLine();
		
		// Standard F Prime ports
		str.append("  # Standard F Prime component ports");
		str.newLine();
		str.append("  sync input port schedIn: Svc.Sched");
		str.newLine();
		str.append("  output port cmdRegOut: Fw.CmdReg");
		str.newLine();
		str.append("  output port cmdResponseOut: Fw.CmdResponse");
		str.newLine();
		str.append("  output port eventOut: Fw.LogEvent");
		str.newLine();
		str.append("  output port tlmOut: Fw.Tlm");
		str.newLine();
		str.newLine();
		
		// Commands from UML StateMachine events
		str.append(mStateMachineGenerator.generateFPPCommands(stateMachine));
		str.newLine();
		str.append("  # Standard state machine control commands");
		str.newLine();
		str.append("  async command START_STATE_MACHINE");
		str.newLine();
		str.append("  async command STOP_STATE_MACHINE");
		str.newLine();
		str.newLine();
		
		// Telemetry for state machine
		str.append("  # Telemetry for state machine monitoring");
		str.newLine();
		str.append("  telemetry CurrentState: string");
		str.newLine();
		str.newLine();
		
		// Events
		str.append("  # Events for state machine transitions");
		str.newLine();
		str.append("  event StateTransition(from: string, to: string) severity activity high");
		str.newLine();
		
		str.append("}");
		str.newLine();
		
		return str.toString();
	}

	/**
	 * Generate C++ component header file
	 */
	private String generateComponentHeader(org.eclipse.uml2.uml.Class componentClass, StateMachine stateMachine) {
		StringConcatenation str = new StringConcatenation();
		String componentName = componentClass.getName();
		
		str.append("#ifndef " + componentName.toUpperCase() + "_HPP");
		str.newLine();
		str.append("#define " + componentName.toUpperCase() + "_HPP");
		str.newLine();
		str.newLine();
		
		str.append("#include \"" + componentName + "ComponentAc.hpp\"");
		str.newLine();
		str.newLine();
		
		str.append("namespace " + getNamespace(componentClass) + " {");
		str.newLine();
		str.newLine();
		
		str.append("  class " + componentName + "ComponentImpl :");
		str.newLine();
		str.append("    public " + componentName + "ComponentBase");
		str.newLine();
		str.append("  {");
		str.newLine();
		str.newLine();
		
		str.append("    public:");
		str.newLine();
		str.append("      " + componentName + "ComponentImpl(const char* const compName);");
		str.newLine();
		str.append("      ~" + componentName + "ComponentImpl();");
		str.newLine();
		str.newLine();
		
		// State machine methods
		str.append("      // State machine implementation");
		str.newLine();
		str.append("      void initializeStateMachine();");
		str.newLine();
		str.append("      void processStateMachine();");
		str.newLine();
		str.newLine();
		
		// Command handlers
		str.append("      // Command handlers");
		str.newLine();
		str.append("      void START_STATE_MACHINE_cmdHandler(");
		str.newLine();
		str.append("          const FwOpcodeType opCode,");
		str.newLine();
		str.append("          const U32 cmdSeq");
		str.newLine();
		str.append("      );");
		str.newLine();
		str.append("      void STOP_STATE_MACHINE_cmdHandler(");
		str.newLine();
		str.append("          const FwOpcodeType opCode,");
		str.newLine();
		str.append("          const U32 cmdSeq");
		str.newLine();
		str.append("      );");
		str.newLine();
		str.newLine();
		
		// Private members
		str.append("    private:");
		str.newLine();
		str.append("      // State machine state enumeration");
		str.newLine();
		str.append("      enum StateMachineStates {");
		str.newLine();
		
		// Generate state enumeration from UML StateMachine
		TreeSet<State> sortedStates = new TreeSet<State>(new StateComparator());
		for (final State s : Iterables.<State>filter(stateMachine.allOwnedElements(), State.class)) {
			if (mQState.isTopState(s)) {
				sortedStates.add(s);
			}
		}
		
		boolean first = true;
		for (final State s : sortedStates) {
			if (!first) str.append(",");
			str.newLine();
			String stateName = getValidStateName(s);
			str.append("        STATE_" + stateName.toUpperCase());
			first = false;
		}
		str.newLine();
		str.append("      };");
		str.newLine();
		str.newLine();
		
		str.append("      StateMachineStates m_currentState;");
		str.newLine();
		str.append("      bool m_stateMachineActive;");
		str.newLine();
		str.newLine();
		
		// State machine helper methods
		str.append("      void transitionToState(StateMachineStates newState);");
		str.newLine();
		str.append("      const char* getStateName(StateMachineStates state);");
		str.newLine();
		str.newLine();
		
		// Activity method declarations
		str.append("      // Activity implementations from UML model");
		str.newLine();
		str.append("      void executeActivities();");
		str.newLine();
		
		str.append("  };");
		str.newLine();
		str.newLine();
		str.append("} // end namespace " + getNamespace(componentClass));
		str.newLine();
		str.newLine();
		str.append("#endif // " + componentName.toUpperCase() + "_HPP");
		str.newLine();
		
		return str.toString();
	}

	/**
	 * Generate C++ component implementation file
	 */
	private String generateComponentImplementation(org.eclipse.uml2.uml.Class componentClass, StateMachine stateMachine) {
		StringConcatenation str = new StringConcatenation();
		String componentName = componentClass.getName();
		
		str.append("#include \"" + componentName + ".hpp\"");
		str.newLine();
		str.append("#include <Fw/Types/Assert.hpp>");
		str.newLine();
		str.newLine();
		
		str.append("namespace " + getNamespace(componentClass) + " {");
		str.newLine();
		str.newLine();
		
		// Constructor
		str.append("  " + componentName + "ComponentImpl::" + componentName + "ComponentImpl(const char* const compName) :");
		str.newLine();
		str.append("    " + componentName + "ComponentBase(compName),");
		str.newLine();
		str.append("    m_currentState(STATE_" + getInitialStateName(stateMachine).toUpperCase() + "),");
		str.newLine();
		str.append("    m_stateMachineActive(false)");
		str.newLine();
		str.append("  {");
		str.newLine();
		str.append("  }");
		str.newLine();
		str.newLine();
		
		// Destructor
		str.append("  " + componentName + "ComponentImpl::~" + componentName + "ComponentImpl()");
		str.newLine();
		str.append("  {");
		str.newLine();
		str.append("  }");
		str.newLine();
		str.newLine();
		
		// State machine initialization
		str.append("  void " + componentName + "ComponentImpl::initializeStateMachine()");
		str.newLine();
		str.append("  {");
		str.newLine();
		str.append("    m_currentState = STATE_" + getInitialStateName(stateMachine).toUpperCase() + ";");
		str.newLine();
		str.append("    m_stateMachineActive = true;");
		str.newLine();
		str.append("    this->log_ACTIVITY_HI_StateTransition(\"INIT\", \"" + getInitialStateName(stateMachine) + "\");");
		str.newLine();
		str.append("    this->tlmWrite_CurrentState(\"" + getInitialStateName(stateMachine) + "\");");
		str.newLine();
		str.append("  }");
		str.newLine();
		str.newLine();
		
		// Enhanced state machine processing with UML logic
		str.append(mStateMachineGenerator.generateStateMachineImplementation(componentName, stateMachine));
		str.newLine();
		
		// Command handlers
		str.append("  void " + componentName + "ComponentImpl::START_STATE_MACHINE_cmdHandler(");
		str.newLine();
		str.append("      const FwOpcodeType opCode,");
		str.newLine();
		str.append("      const U32 cmdSeq");
		str.newLine();
		str.append("  )");
		str.newLine();
		str.append("  {");
		str.newLine();
		str.append("    initializeStateMachine();");
		str.newLine();
		str.append("    this->cmdResponse_out(opCode, cmdSeq, Fw::CmdResponse::OK);");
		str.newLine();
		str.append("  }");
		str.newLine();
		str.newLine();
		
		str.append("  void " + componentName + "ComponentImpl::STOP_STATE_MACHINE_cmdHandler(");
		str.newLine();
		str.append("      const FwOpcodeType opCode,");
		str.newLine();
		str.append("      const U32 cmdSeq");
		str.newLine();
		str.append("  )");
		str.newLine();
		str.append("  {");
		str.newLine();
		str.append("    m_stateMachineActive = false;");
		str.newLine();
		str.append("    this->log_ACTIVITY_HI_StateTransition(\"ACTIVE\", \"STOPPED\");");
		str.newLine();
		str.append("    this->cmdResponse_out(opCode, cmdSeq, Fw::CmdResponse::OK);");
		str.newLine();
		str.append("  }");
		str.newLine();
		str.newLine();
		
		// UML Event-based command handlers
		str.append(mStateMachineGenerator.generateEventHandlers(componentName, stateMachine));
		str.newLine();
		
		// State transition helper methods
		str.append(mStateMachineGenerator.generateTransitionHelper(componentName));
		str.newLine();
		
		// Activity execution placeholder
		str.append("  void " + componentName + "ComponentImpl::executeActivities()");
		str.newLine();
		str.append("  {");
		str.newLine();
		str.append("    // TODO: Implement UML Activity logic here");
		str.newLine();
		str.append("  }");
		str.newLine();
		str.newLine();
		
		str.append("} // end namespace " + getNamespace(componentClass));
		str.newLine();
		
		return str.toString();
	}

	/**
	 * Generate CMakeLists.txt for F Prime component
	 */
	private String generateCMakeFile(org.eclipse.uml2.uml.Class componentClass) {
		StringConcatenation str = new StringConcatenation();
		String componentName = componentClass.getName();
		
		str.append("# Auto-generated CMakeLists.txt for F Prime component");
		str.newLine();
		str.append("# Generated from UML/SysML model: " + componentName);
		str.newLine();
		str.newLine();
		
		str.append("set(SOURCE_FILES");
		str.newLine();
		str.append("  \"${CMAKE_CURRENT_LIST_DIR}/" + componentName + ".fpp\"");
		str.newLine();
		str.append("  \"${CMAKE_CURRENT_LIST_DIR}/" + componentName + ".cpp\"");
		str.newLine();
		str.append(")");
		str.newLine();
		str.newLine();
		
		str.append("register_fprime_module()");
		str.newLine();
		str.newLine();
		
		str.append("set(MOD_DEPS");
		str.newLine();
		str.append("  Fw/Cmd");
		str.newLine();
		str.append("  Fw/Log");
		str.newLine();
		str.append("  Fw/Tlm");
		str.newLine();
		str.append("  Svc/Sched");
		str.newLine();
		str.append(")");
		str.newLine();
		str.newLine();
		
		str.append("register_fprime_implementation()");
		str.newLine();
		
		return str.toString();
	}

	/**
	 * Get the initial state name from the state machine
	 */
	private String getInitialStateName(StateMachine stateMachine) {
		String initialState = mQStateMachine.getInitialStateName(stateMachine);
		if (initialState == null || initialState.isEmpty()) {
			// Fallback to first top-level state
			for (final State s : Iterables.<State>filter(stateMachine.allOwnedElements(), State.class)) {
				if (mQState.isTopState(s)) {
					return s.getName();
				}
			}
			return "UNKNOWN";
		}
		return initialState;
	}

	/**
	 * Get a valid state name, handling null/empty names
	 */
	private String getValidStateName(State state) {
		String name = state.getName();
		if (name == null || name.trim().isEmpty()) {
			return "UNNAMED_STATE_" + state.hashCode();
		}
		return name.trim();
	}

	/**
	 * Get the namespace for the component based on its package structure
	 */
	private String getNamespace(org.eclipse.uml2.uml.Class componentClass) {
		// For now, use a simple namespace. Could be enhanced to use UML package structure
		return "Components";
	}
}