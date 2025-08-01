package comodo2.templates.fprime;

import com.google.common.collect.Iterables;
import comodo2.queries.QStateMachine;
import comodo2.queries.QState;
import comodo2.queries.QTransition;
import comodo2.queries.QEvent;
import comodo2.queries.QSignal;
import java.util.TreeSet;
import javax.inject.Inject;
import org.eclipse.uml2.uml.Signal;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Element;
import org.eclipse.xtend2.lib.StringConcatenation;

/**
 * F Prime Signal Parser and Command Generator
 * Maps UML Signals to F Prime Commands with proper command handlers
 * and integrates with state machine event-driven transitions.
 */
public class FPrimeSignal {

	@Inject
	private QStateMachine mQStateMachine;

	@Inject
	private QState mQState;

	@Inject
	private QTransition mQTransition;

	@Inject
	private QEvent mQEvent;

	@Inject
	private QSignal mQSignal;

	/**
	 * Generate FPP command definitions for all signals used in the state machine
	 */
	public String generateFPPSignalCommands(StateMachine stateMachine) {
		StringConcatenation str = new StringConcatenation();
		
		// Collect all unique signals from state machine transitions
		TreeSet<String> signalNames = collectSignalNames(stateMachine);
		
		if (!signalNames.isEmpty()) {
			str.append("  # Commands derived from UML Signals");
			str.newLine();
			
			for (String signalName : signalNames) {
				Signal signal = findSignalByName(stateMachine, signalName);
				if (signal != null) {
					str.append(generateFPPCommandForSignal(signal));
				}
			}
		}
		
		return str.toString();
	}

	/**
	 * Generate F Prime command handler declarations for signals
	 */
	public String generateSignalCommandHandlerDeclarations(StateMachine stateMachine) {
		StringConcatenation str = new StringConcatenation();
		
		TreeSet<String> signalNames = collectSignalNames(stateMachine);
		
		if (!signalNames.isEmpty()) {
			str.append("      // Signal command handler declarations");
			str.newLine();
			
			for (String signalName : signalNames) {
				Signal signal = findSignalByName(stateMachine, signalName);
				if (signal != null) {
					str.append(generateCommandHandlerDeclaration(signal));
				}
			}
		}
		
		return str.toString();
	}

	/**
	 * Generate F Prime command handler implementations for signals
	 */
	public String generateSignalCommandHandlerImplementations(String componentName, StateMachine stateMachine) {
		StringConcatenation str = new StringConcatenation();
		
		TreeSet<String> signalNames = collectSignalNames(stateMachine);
		
		if (!signalNames.isEmpty()) {
			str.append("  // Signal command handler implementations");
			str.newLine();
			str.newLine();
			
			for (String signalName : signalNames) {
				Signal signal = findSignalByName(stateMachine, signalName);
				if (signal != null) {
					str.append(generateCommandHandlerImplementation(componentName, signal, stateMachine));
					str.newLine();
				}
			}
		}
		
		return str.toString();
	}

	/**
	 * Collect all unique signal names used in state machine transitions
	 */
	private TreeSet<String> collectSignalNames(StateMachine stateMachine) {
		TreeSet<String> signalNames = new TreeSet<String>();
		
		// Check all transitions in all states for signal events
		for (final State state : Iterables.<State>filter(stateMachine.allOwnedElements(), State.class)) {
			if (mQState.isTopState(state)) {
				for (final Transition transition : state.getOutgoings()) {
					if (mQTransition.hasSignalEvent(transition)) {
						String signalName = mQTransition.getFirstEventName(transition);
						if (signalName != null && !signalName.isEmpty()) {
							signalNames.add(signalName);
						}
					}
				}
			}
		}
		
		return signalNames;
	}

	/**
	 * Find a Signal object by name within the state machine context
	 */
	private Signal findSignalByName(StateMachine stateMachine, String signalName) {
		// Check all transitions to find the actual Signal object
		for (final State state : Iterables.<State>filter(stateMachine.allOwnedElements(), State.class)) {
			if (mQState.isTopState(state)) {
				for (final Transition transition : state.getOutgoings()) {
					if (mQTransition.hasSignalEvent(transition)) {
						Signal signal = mQTransition.getFirstEvent(transition);
						if (signal != null && signal.getName().equals(signalName)) {
							return signal;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Generate FPP command definition for a single signal
	 */
	private String generateFPPCommandForSignal(Signal signal) {
		StringConcatenation str = new StringConcatenation();
		
		String commandName = mQSignal.nameWithoutPrefix(signal);
		
		str.append("  async command " + commandName);
		
		// Add parameters if the signal has them
		if (mQSignal.hasParam(signal)) {
			str.append("(");
			// For now, use a generic parameter type - could be enhanced to parse actual types
			str.append("param: U32");
			str.append(")");
		}
		
		str.newLine();
		
		return str.toString();
	}

	/**
	 * Generate command handler method declaration for a signal
	 */
	private String generateCommandHandlerDeclaration(Signal signal) {
		StringConcatenation str = new StringConcatenation();
		
		String commandName = mQSignal.nameWithoutPrefix(signal);
		
		str.append("      void " + commandName + "_cmdHandler(");
		str.newLine();
		str.append("          const FwOpcodeType opCode,");
		str.newLine();
		str.append("          const U32 cmdSeq");
		
		// Add parameter if signal has parameters
		if (mQSignal.hasParam(signal)) {
			str.append(",");
			str.newLine();
			str.append("          const U32 param");
		}
		
		str.newLine();
		str.append("      );");
		str.newLine();
		
		return str.toString();
	}

	/**
	 * Generate command handler implementation for a signal
	 */
	private String generateCommandHandlerImplementation(String componentName, Signal signal, StateMachine stateMachine) {
		StringConcatenation str = new StringConcatenation();
		
		String commandName = mQSignal.nameWithoutPrefix(signal);
		String signalName = signal.getName();
		
		str.append("  void " + componentName + "ComponentImpl::" + commandName + "_cmdHandler(");
		str.newLine();
		str.append("      const FwOpcodeType opCode,");
		str.newLine();
		str.append("      const U32 cmdSeq");
		
		if (mQSignal.hasParam(signal)) {
			str.append(",");
			str.newLine();
			str.append("      const U32 param");
		}
		
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
		
		str.append("    // Process signal: " + signalName);
		str.newLine();
		str.append("    switch (m_currentState) {");
		str.newLine();
		
		// Generate state-specific transition logic for this signal
		for (final State state : Iterables.<State>filter(stateMachine.allOwnedElements(), State.class)) {
			if (mQState.isTopState(state)) {
				boolean hasTransitionForSignal = false;
				
				// Check if this state has transitions triggered by this signal
				for (final Transition transition : state.getOutgoings()) {
					if (mQTransition.hasSignalEvent(transition)) {
						String transitionSignalName = mQTransition.getFirstEventName(transition);
						if (signalName.equals(transitionSignalName)) {
							hasTransitionForSignal = true;
							break;
						}
					}
				}
				
				if (hasTransitionForSignal) {
					str.append("      case STATE_" + state.getName().toUpperCase() + ":");
					str.newLine();
					
					// Generate transition logic
					for (final Transition transition : state.getOutgoings()) {
						if (mQTransition.hasSignalEvent(transition)) {
							String transitionSignalName = mQTransition.getFirstEventName(transition);
							if (signalName.equals(transitionSignalName)) {
								String targetStateName = mQTransition.getTargetName(transition);
								if (targetStateName != null && !targetStateName.isEmpty()) {
									
									// Check for guard conditions
									if (mQTransition.hasGuard(transition)) {
										String guardCondition = mQTransition.getGuardName(transition);
										str.append("        if (" + guardCondition + ") {");
										str.newLine();
										str.append("          transitionToState(STATE_" + targetStateName.toUpperCase() + ");");
										str.newLine();
										str.append("          this->log_ACTIVITY_HI_StateTransition(\"" + state.getName() + "\", \"" + targetStateName + "\");");
										str.newLine();
										str.append("        }");
										str.newLine();
									} else {
										str.append("        transitionToState(STATE_" + targetStateName.toUpperCase() + ");");
										str.newLine();
										str.append("        this->log_ACTIVITY_HI_StateTransition(\"" + state.getName() + "\", \"" + targetStateName + "\");");
										str.newLine();
									}
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
		str.append("        // Signal not handled in current state");
		str.newLine();
		str.append("        this->log_WARNING_LO_StateTransition(\"Signal " + signalName + " not handled in current state\", m_currentState);");
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
		
		return str.toString();
	}

	/**
	 * Check if the state machine uses any signals
	 */
	public boolean hasSignals(StateMachine stateMachine) {
		return !collectSignalNames(stateMachine).isEmpty();
	}
}