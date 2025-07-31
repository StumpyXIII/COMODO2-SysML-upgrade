package comodo2.templates.fprime;

import com.google.common.collect.Iterables;
import comodo2.queries.QState;
import comodo2.queries.QStateMachine;
import comodo2.queries.QTransition;
import comodo2.queries.QEvent;
import comodo2.utils.StateComparator;
import comodo2.utils.TransitionComparator;
import java.util.TreeSet;
import javax.inject.Inject;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.xtend2.lib.StringConcatenation;

/**
 * F Prime State Machine Logic Generator
 * Generates detailed state machine implementation from UML StateMachine
 */
public class FPrimeStateMachine {

	@Inject
	private QStateMachine mQStateMachine;

	@Inject
	private QState mQState;

	@Inject
	private QTransition mQTransition;

	@Inject
	private QEvent mQEvent;

	/**
	 * Generate F Prime state machine processing logic
	 */
	public String generateStateMachineImplementation(String componentName, StateMachine stateMachine) {
		StringConcatenation str = new StringConcatenation();
		
		str.append("  void " + componentName + "ComponentImpl::processStateMachine()");
		str.newLine();
		str.append("  {");
		str.newLine();
		str.append("    if (!m_stateMachineActive) {");
		str.newLine();
		str.append("      return;");
		str.newLine();
		str.append("    }");
		str.newLine();
		str.newLine();
		
		str.append("    // State machine implementation generated from UML model");
		str.newLine();
		str.append("    switch (m_currentState) {");
		str.newLine();
		
		// Generate state cases with actual transition logic
		TreeSet<State> sortedStates = new TreeSet<State>(new StateComparator());
		for (final State s : Iterables.<State>filter(stateMachine.allOwnedElements(), State.class)) {
			if (mQState.isTopState(s)) {
				sortedStates.add(s);
			}
		}
		
		for (final State s : sortedStates) {
			str.append(generateStateCase(s));
		}
		
		str.append("      default:");
		str.newLine();
		str.append("        FW_ASSERT(0, m_currentState);");
		str.newLine();
		str.append("        break;");
		str.newLine();
		str.append("    }");
		str.newLine();
		str.append("  }");
		str.newLine();
		
		return str.toString();
	}

	/**
	 * Generate individual state case with transitions
	 */
	private String generateStateCase(State state) {
		StringConcatenation str = new StringConcatenation();
		
		String stateName = getValidStateName(state);
		str.append("      case STATE_" + stateName.toUpperCase() + ":");
		str.newLine();
		str.append("        // State: " + stateName);
		str.newLine();
		
		// Generate entry actions
		if (state.getEntry() != null) {
			str.append("        // Entry action: " + state.getEntry().getName());
			str.newLine();
			str.append("        execute_" + state.getEntry().getName() + "();");
			str.newLine();
		}
		
		// Generate do activity
		if (state.getDoActivity() != null && state.getDoActivity().getName() != null && !state.getDoActivity().getName().isEmpty()) {
			str.append("        // Do activity: " + state.getDoActivity().getName());
			str.newLine();
			str.append("        execute_" + state.getDoActivity().getName() + "();");
			str.newLine();
		}
		
		// Generate transitions
		if (!state.getOutgoings().isEmpty()) {
			str.append("        // Process transitions");
			str.newLine();
			str.append(generateStateTransitions(state));
		}
		
		str.append("        break;");
		str.newLine();
		str.newLine();
		
		return str.toString();
	}

	/**
	 * Generate transition logic for a state
	 */
	private String generateStateTransitions(State state) {
		StringConcatenation str = new StringConcatenation();
		
		// Sort transitions for consistent output
		TreeSet<Transition> sortedTransitions = new TreeSet<Transition>(new TransitionComparator());
		for (final Transition t : state.getOutgoings()) {
			if (!mQTransition.isMalformed(t)) {
				sortedTransitions.add(t);
			}
		}
		
		boolean hasEventTransitions = false;
		boolean hasGuardTransitions = false;
		
		// Check what types of transitions we have
		for (final Transition t : sortedTransitions) {
			if (mQTransition.hasEvent(t)) {
				hasEventTransitions = true;
			}
			if (mQTransition.hasGuard(t)) {
				hasGuardTransitions = true;
			}
		}
		
		// Generate event-based transitions
		if (hasEventTransitions) {
			str.append("        // Event-based transitions (processed by command handlers)");
			str.newLine();
			for (final Transition t : sortedTransitions) {
				if (mQTransition.hasEvent(t)) {
					String eventName = mQTransition.getFirstEventName(t);
					if (eventName != null && !eventName.isEmpty()) {
						str.append("        // Event: " + eventName + " -> " + getTargetStateName(t));
						str.newLine();
					}
				}
			}
		}
		
		// Generate guard-based transitions (automatic)
		if (hasGuardTransitions) {
			str.append("        // Guard-based transitions (automatic)");
			str.newLine();
			for (final Transition t : sortedTransitions) {
				if (mQTransition.hasGuard(t) && !mQTransition.hasEvent(t)) {
					String guardCondition = mQTransition.getGuardName(t);
					String targetState = getTargetStateName(t);
					
					if (guardCondition != null && !guardCondition.isEmpty() && 
						targetState != null && !targetState.isEmpty()) {
						
						str.append("        if (" + guardCondition + ") {");
						str.newLine();
						str.append("          transitionToState(STATE_" + targetState.toUpperCase() + ");");
						str.newLine();
						str.append("          return; // Exit after transition");
						str.newLine();
						str.append("        }");
						str.newLine();
					}
				}
			}
		}
		
		return str.toString();
	}

	/**
	 * Generate event handler methods for command-triggered transitions
	 */
	public String generateEventHandlers(String componentName, StateMachine stateMachine) {
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
		
		// Generate command handlers for each event
		for (String eventName : eventNames) {
			str.append("  void " + componentName + "ComponentImpl::" + eventName + "_cmdHandler(");
			str.newLine();
			str.append("      const FwOpcodeType opCode,");
			str.newLine();
			str.append("      const U32 cmdSeq");
			str.newLine();
			str.append("  )");
			str.newLine();
			str.append("  {");
			str.newLine();
			str.append("    if (!m_stateMachineActive) {");
			str.newLine();
			str.append("      this->cmdResponse_out(opCode, cmdSeq, Fw::CmdResponse::EXECUTION_ERROR);");
			str.newLine();
			str.append("      return;");
			str.newLine();
			str.append("    }");
			str.newLine();
			str.newLine();
			
			// Generate transition logic for this event
			str.append("    // Process " + eventName + " event transitions");
			str.newLine();
			str.append("    switch (m_currentState) {");
			str.newLine();
			
			for (final State s : Iterables.<State>filter(stateMachine.allOwnedElements(), State.class)) {
				if (mQState.isTopState(s)) {
					boolean hasTransitionForEvent = false;
					for (final Transition t : s.getOutgoings()) {
						if (mQTransition.hasEvent(t) && eventName.equals(mQTransition.getFirstEventName(t))) {
							hasTransitionForEvent = true;
							break;
						}
					}
					
					if (hasTransitionForEvent) {
						String stateName = getValidStateName(s);
						str.append("      case STATE_" + stateName.toUpperCase() + ":");
						str.newLine();
						
						for (final Transition t : s.getOutgoings()) {
							if (mQTransition.hasEvent(t) && eventName.equals(mQTransition.getFirstEventName(t))) {
								String targetState = getTargetStateName(t);
								if (targetState != null && !targetState.isEmpty()) {
									// Check if transition has guard
									if (mQTransition.hasGuard(t)) {
										String guardCondition = mQTransition.getGuardName(t);
										str.append("        if (" + guardCondition + ") {");
										str.newLine();
										str.append("          transitionToState(STATE_" + targetState.toUpperCase() + ");");
										str.newLine();
										str.append("        }");
										str.newLine();
									} else {
										str.append("        transitionToState(STATE_" + targetState.toUpperCase() + ");");
										str.newLine();
									}
								}
							}
						}
						str.append("        break;");
						str.newLine();
					}
				}
			}
			
			str.append("      default:");
			str.newLine();
			str.append("        // Event not handled in current state");
			str.newLine();
			str.append("        break;");
			str.newLine();
			str.append("    }");
			str.newLine();
			str.newLine();
			str.append("    this->cmdResponse_out(opCode, cmdSeq, Fw::CmdResponse::OK);");
			str.newLine();
			str.append("  }");
			str.newLine();
			str.newLine();
		}
		
		return str.toString();
	}

	/**
	 * Generate state transition helper method
	 */
	public String generateTransitionHelper(String componentName) {
		StringConcatenation str = new StringConcatenation();
		
		str.append("  void " + componentName + "ComponentImpl::transitionToState(StateMachineStates newState)");
		str.newLine();
		str.append("  {");
		str.newLine();
		str.append("    if (m_currentState != newState) {");
		str.newLine();
		str.append("      StateMachineStates oldState = m_currentState;");
		str.newLine();
		str.append("      m_currentState = newState;");
		str.newLine();
		str.newLine();
		str.append("      // Log state transition");
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
		str.append("      // Update telemetry");
		str.newLine();
		str.append("      this->tlmWrite_CurrentState(getStateName(newState));");
		str.newLine();
		str.append("    }");
		str.newLine();
		str.append("  }");
		str.newLine();
		str.newLine();
		
		str.append("  const char* " + componentName + "ComponentImpl::getStateName(StateMachineStates state)");
		str.newLine();
		str.append("  {");
		str.newLine();
		str.append("    switch (state) {");
		str.newLine();
		str.append("      default: return \"UNKNOWN\";");
		str.newLine();
		str.append("    }");
		str.newLine();
		str.append("  }");
		str.newLine();
		
		return str.toString();
	}

	/**
	 * Get target state name from transition
	 */
	private String getTargetStateName(Transition transition) {
		String targetName = mQTransition.getTargetName(transition);
		if (targetName == null || targetName.trim().isEmpty()) {
			return "";
		}
		return targetName;
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
	 * Generate FPP commands for UML events
	 */
	public String generateFPPCommands(StateMachine stateMachine) {
		StringConcatenation str = new StringConcatenation();
		
		// Collect all unique events
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
		
		str.append("  # Commands derived from UML StateMachine events");
		str.newLine();
		for (String eventName : eventNames) {
			str.append("  async command " + eventName);
			str.newLine();
		}
		
		return str.toString();
	}
}