package comodo2.templates.fprime;

import com.google.common.collect.Iterables;
import comodo2.queries.QState;
import comodo2.queries.QStateMachine;
import comodo2.queries.QTransition;
import comodo2.utils.StateComparator;
import java.util.TreeSet;
import javax.inject.Inject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.xtend2.lib.StringConcatenation;

/**
 * F Prime Implementation Generator
 * Generates only the UML-derived implementation logic that extends
 * F Prime's auto-generated base classes. This class focuses on the
 * rich semantic content from UML XMI models rather than boilerplate.
 */
public class FPrimeImplementation {

	@Inject
	private QStateMachine mQStateMachine;

	@Inject
	private QState mQState;

	@Inject
	private QTransition mQTransition;

	@Inject
	private FPrimeActivity mActivityGenerator;

	@Inject
	private FPrimeStateMachine mStateMachineGenerator;

	@Inject
	private FPrimeSignal mSignalGenerator;

	/**
	 * Generate component implementation header (.hpp) that extends F Prime base
	 */
	public String generateImplementationHeader(Class componentClass, StateMachine stateMachine, Resource input) {
		StringConcatenation str = new StringConcatenation();
		String componentName = componentClass.getName();
		
		str.append("#ifndef " + componentName.toUpperCase() + "_IMPL_HPP");
		str.newLine();
		str.append("#define " + componentName.toUpperCase() + "_IMPL_HPP");
		str.newLine();
		str.newLine();

		str.append("// F Prime component implementation extending auto-generated base");
		str.newLine();
		str.append("// Generated from UML 5.x XMI: " + componentClass.getName());
		str.newLine();
		str.append("// StateMachine: " + stateMachine.getName());
		str.newLine();
		str.newLine();

		str.append("#include \"" + componentName + "ComponentBase.hpp\"");
		str.newLine();
		str.append("#include \"" + componentName + "ComponentAc.hpp\"");
		str.newLine();
		str.newLine();

		// Add external block type definitions for F Prime compatibility
		str.append(mActivityGenerator.generateExternalBlockTypes(stateMachine));
		str.newLine();

		str.append("namespace Components {");
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

		// UML StateMachine implementation methods
		str.append("      // UML StateMachine implementation");
		str.newLine();
		str.append("      void initializeStateMachine();");
		str.newLine();
		str.append("      void processStateMachine();");
		str.newLine();
		str.append("      void schedIn_handler(const NATIVE_INT_TYPE portNum, NATIVE_UINT_TYPE context);");
		str.newLine();
		str.newLine();

		// Command handlers (implementing pure virtual from base)
		str.append("      // Command handlers (implementing F Prime base class interface)");
		str.newLine();
		str.append("      void START_STATE_MACHINE_cmdHandler(");
		str.newLine();
		str.append("          const FwOpcodeType opCode,");
		str.newLine();
		str.append("          const U32 cmdSeq");
		str.newLine();
		str.append("      ) override;");
		str.newLine();
		str.append("      void STOP_STATE_MACHINE_cmdHandler(");
		str.newLine();
		str.append("          const FwOpcodeType opCode,");
		str.newLine();
		str.append("          const U32 cmdSeq");
		str.newLine();
		str.append("      ) override;");
		str.newLine();
		str.newLine();

		// UML Event-based command handlers
		str.append(generateEventHandlerDeclarations(stateMachine));
		str.newLine();

		// UML Signal-based command handlers
		str.append(mSignalGenerator.generateSignalCommandHandlerDeclarations(stateMachine));
		str.newLine();

		str.append("    private:");
		str.newLine();
		str.append("      // UML StateMachine state enumeration");
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

		// UML-derived helper methods
		str.append("      // UML StateMachine helper methods");
		str.newLine();
		str.append("      void transitionToState(StateMachineStates newState);");
		str.newLine();
		str.append("      const char* getStateName(StateMachineStates state);");
		str.newLine();
		str.append("      bool canTransition(StateMachineStates fromState, StateMachineStates toState);");
		str.newLine();
		str.newLine();

		// UML Activity method declarations
		str.append("      // UML Activity implementations");
		str.newLine();
		str.append(mActivityGenerator.generateActivityDeclarations(stateMachine));
		str.newLine();

		str.append("  };");
		str.newLine();
		str.newLine();
		str.append("} // end namespace Components");
		str.newLine();
		str.newLine();
		str.append("#endif // " + componentName.toUpperCase() + "_IMPL_HPP");
		str.newLine();

		return str.toString();
	}

	/**
	 * Generate component implementation source (.cpp) with UML-derived logic
	 */
	public String generateImplementationSource(Class componentClass, StateMachine stateMachine, Resource input) {
		StringConcatenation str = new StringConcatenation();
		String componentName = componentClass.getName();
		
		str.append("#include \"" + componentName + "ComponentImpl.hpp\"");
		str.newLine();
		str.append("#include <iostream>");
		str.newLine();
		str.newLine();

		str.append("// F Prime component implementation with UML-derived logic");
		str.newLine();
		str.append("// Generated from UML 5.x XMI: " + componentClass.getName());
		str.newLine();
		str.append("// StateMachine: " + stateMachine.getName());
		str.newLine();
		str.newLine();

		str.append("namespace Components {");
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
		str.append("    std::cout << \"UML-derived F Prime component created: \" << compName << std::endl;");
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

		// UML StateMachine initialization
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
		str.append("    std::cout << \"UML StateMachine initialized to: " + getInitialStateName(stateMachine) + "\" << std::endl;");
		str.newLine();
		str.append("  }");
		str.newLine();
		str.newLine();

		// Schedule input handler (F Prime active component interface)
		str.append("  void " + componentName + "ComponentImpl::schedIn_handler(const NATIVE_INT_TYPE portNum, NATIVE_UINT_TYPE context)");
		str.newLine();
		str.append("  {");
		str.newLine();
		str.append("    // F Prime scheduler called - process UML StateMachine");
		str.newLine();
		str.append("    processStateMachine();");
		str.newLine();
		str.append("  }");
		str.newLine();
		str.newLine();

		// UML StateMachine processing with rich UML logic
		str.append(mStateMachineGenerator.generateStateMachineImplementation(componentName, stateMachine));
		str.newLine();

		// Standard command handlers (implementing pure virtual from base)
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
		str.append("    std::cout << \"Starting UML StateMachine via F Prime command\" << std::endl;");
		str.newLine();
		str.append("    initializeStateMachine();");
		str.newLine();
		str.append("    this->cmdResponse_out(opCode, cmdSeq, Fw::CmdResponse_OK);");
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
		str.append("    std::cout << \"Stopping UML StateMachine via F Prime command\" << std::endl;");
		str.newLine();
		str.append("    m_stateMachineActive = false;");
		str.newLine();
		str.append("    this->log_ACTIVITY_HI_StateTransition(\"ACTIVE\", \"STOPPED\");");
		str.newLine();
		str.append("    this->cmdResponse_out(opCode, cmdSeq, Fw::CmdResponse_OK);");
		str.newLine();
		str.append("  }");
		str.newLine();
		str.newLine();

		// UML Event-based command handlers
		str.append(mStateMachineGenerator.generateEventHandlers(componentName, stateMachine));
		str.newLine();

		// UML Signal-based command handlers
		str.append(mSignalGenerator.generateSignalCommandHandlerImplementations(componentName, stateMachine));
		str.newLine();

		// UML StateMachine helper methods with enhanced logic
		str.append(generateEnhancedTransitionHelper(componentName, stateMachine));
		str.newLine();

		// UML Activity implementations (the rich semantic content)
		str.append(mActivityGenerator.generateActivityImplementations(componentName, stateMachine));
		str.newLine();

		str.append("} // end namespace Components");
		str.newLine();

		return str.toString();
	}

	/**
	 * Generate enhanced state transition helper with UML semantics
	 */
	private String generateEnhancedTransitionHelper(String componentName, StateMachine stateMachine) {
		StringConcatenation str = new StringConcatenation();
		
		str.append("  void " + componentName + "ComponentImpl::transitionToState(StateMachineStates newState)");
		str.newLine();
		str.append("  {");
		str.newLine();
		str.append("    if (m_currentState != newState && canTransition(m_currentState, newState)) {");
		str.newLine();
		str.append("      StateMachineStates oldState = m_currentState;");
		str.newLine();
		str.append("      m_currentState = newState;");
		str.newLine();
		str.newLine();
		str.append("      // UML-derived state transition logging");
		str.newLine();
		str.append("      this->log_ACTIVITY_HI_StateTransition(");
		str.newLine();
		str.append("        getStateName(oldState), ");
		str.newLine();
		str.append("        getStateName(newState)");
		str.newLine();
		str.append("      );");
		str.newLine();
		str.newLine();
		str.append("      // Update F Prime telemetry");
		str.newLine();
		str.append("      this->tlmWrite_CurrentState(getStateName(newState));");
		str.newLine();
		str.newLine();
		str.append("      std::cout << \"UML State Transition: \" << getStateName(oldState) ");
		str.newLine();
		str.append("                << \" -> \" << getStateName(newState) << std::endl;");
		str.newLine();
		str.append("    }");
		str.newLine();
		str.append("  }");
		str.newLine();
		str.newLine();

		// State name mapping
		str.append("  const char* " + componentName + "ComponentImpl::getStateName(StateMachineStates state)");
		str.newLine();
		str.append("  {");
		str.newLine();
		str.append("    switch (state) {");
		str.newLine();

		// Generate case statements for each UML state
		for (final State s : Iterables.<State>filter(stateMachine.allOwnedElements(), State.class)) {
			if (mQState.isTopState(s)) {
				String stateName = getValidStateName(s);
				str.append("      case STATE_" + stateName.toUpperCase() + ": return \"" + stateName + "\";");
				str.newLine();
			}
		}

		str.append("      default: return \"UNKNOWN\";");
		str.newLine();
		str.append("    }");
		str.newLine();
		str.append("  }");
		str.newLine();
		str.newLine();

		// UML-based transition validation
		str.append("  bool " + componentName + "ComponentImpl::canTransition(StateMachineStates fromState, StateMachineStates toState)");
		str.newLine();
		str.append("  {");
		str.newLine();
		str.append("    // UML StateMachine transition validation logic");
		str.newLine();
		str.append("    // In a full implementation, this would check UML transition guards");
		str.newLine();
		str.append("    return true;  // For now, allow all transitions");
		str.newLine();
		str.append("  }");
		str.newLine();

		return str.toString();
	}

	/**
	 * Get the initial state name from the UML StateMachine with enhanced detection
	 */
	private String getInitialStateName(StateMachine stateMachine) {
		// Use the same enhanced initial state detection as before
		String initialState = mQStateMachine.getInitialStateName(stateMachine);
		
		if (initialState != null && !initialState.trim().isEmpty()) {
			return initialState.trim();
		}
		
		// Enhanced fallback: Look for states that might be initial
		for (final State s : Iterables.<State>filter(stateMachine.allOwnedElements(), State.class)) {
			if (mQState.isTopState(s)) {
				String stateName = getValidStateName(s);
				
				// Prefer states with names indicating they're initial
				if (stateName.toLowerCase().contains("init") || 
				    stateName.toLowerCase().contains("start") ||
				    stateName.toLowerCase().contains("begin")) {
					return stateName;
				}
			}
		}
		
		// Final fallback to first valid top-level state
		for (final State s : Iterables.<State>filter(stateMachine.allOwnedElements(), State.class)) {
			if (mQState.isTopState(s)) {
				return getValidStateName(s);
			}
		}
		
		return "UNKNOWN";
	}

	/**
	 * Generate UML Event-based command handler declarations
	 */
	private String generateEventHandlerDeclarations(StateMachine stateMachine) {
		StringConcatenation str = new StringConcatenation();
		
		// Collect all unique events from transitions
		TreeSet<String> eventNames = new TreeSet<String>();
		for (final State s : Iterables.<State>filter(stateMachine.allOwnedElements(), State.class)) {
			if (mQState.isTopState(s)) {
				for (final Transition t : s.getOutgoings()) {
					if (mQTransition.hasEvent(t)) {
						String eventName = mQTransition.getFirstEventName(t);
						if (eventName != null && !eventName.isEmpty()) {
							eventNames.add(eventName);
						}
					}
				}
			}
		}
		
		if (!eventNames.isEmpty()) {
			str.append("      // UML Event-based command handlers");
			str.newLine();
			
			for (String eventName : eventNames) {
				str.append("      void " + eventName + "_cmdHandler(");
				str.newLine();
				str.append("          const FwOpcodeType opCode,");
				str.newLine();
				str.append("          const U32 cmdSeq");
				str.newLine();
				str.append("      ) override;");
				str.newLine();
			}
		}
		
		return str.toString();
	}

	/**
	 * Get a valid state name, handling null/empty names
	 */
	private String getValidStateName(State state) {
		String name = state.getName();
		if (name == null || name.trim().isEmpty()) {
			return "UNNAMED_STATE_" + Math.abs(state.hashCode());
		}
		return name.trim();
	}
}