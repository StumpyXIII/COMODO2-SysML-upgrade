#include "SlewUseCaseBlockComponentImpl.hpp"
#include <iostream>

// F Prime component implementation with UML-derived logic
// Generated from UML 5.x XMI: SlewUseCaseBlock
// StateMachine: maxSlew

namespace Components {

  SlewUseCaseBlockComponentImpl::SlewUseCaseBlockComponentImpl(const char* const compName) :
    SlewUseCaseBlockComponentBase(compName),
    m_currentState(STATE_STATE),
    m_stateMachineActive(false)
  {
    std::cout << "UML-derived F Prime component created: " << compName << std::endl;
  }

  SlewUseCaseBlockComponentImpl::~SlewUseCaseBlockComponentImpl()
  {
  }

  void SlewUseCaseBlockComponentImpl::initializeStateMachine()
  {
    m_currentState = STATE_STATE;
    m_stateMachineActive = true;
    this->log_ACTIVITY_HI_StateTransition("INIT", "STATE");
    this->tlmWrite_CurrentState("STATE");
    std::cout << "UML StateMachine initialized to: STATE" << std::endl;
  }

  void SlewUseCaseBlockComponentImpl::schedIn_handler(const NATIVE_INT_TYPE portNum, NATIVE_UINT_TYPE context)
  {
    // F Prime scheduler called - process UML StateMachine
    processStateMachine();
  }

  void SlewUseCaseBlockComponentImpl::processStateMachine()
  {
    if (!m_stateMachineActive) {
      return;
    }

    // State machine implementation generated from UML model
    switch (m_currentState) {
      case STATE_STATE:
        // State: STATE
        break;

      default:
        FW_ASSERT(0, m_currentState);
        break;
    }
  }

  void SlewUseCaseBlockComponentImpl::START_STATE_MACHINE_cmdHandler(
      const FwOpcodeType opCode,
      const U32 cmdSeq
  )
  {
    std::cout << "Starting UML StateMachine via F Prime command" << std::endl;
    initializeStateMachine();
    this->cmdResponse_out(opCode, cmdSeq, Fw::CmdResponse_OK);
  }

  void SlewUseCaseBlockComponentImpl::STOP_STATE_MACHINE_cmdHandler(
      const FwOpcodeType opCode,
      const U32 cmdSeq
  )
  {
    std::cout << "Stopping UML StateMachine via F Prime command" << std::endl;
    m_stateMachineActive = false;
    this->log_ACTIVITY_HI_StateTransition("ACTIVE", "STOPPED");
    this->cmdResponse_out(opCode, cmdSeq, Fw::CmdResponse_OK);
  }



  void SlewUseCaseBlockComponentImpl::transitionToState(StateMachineStates newState)
  {
    if (m_currentState != newState && canTransition(m_currentState, newState)) {
      StateMachineStates oldState = m_currentState;
      m_currentState = newState;

      // UML-derived state transition logging
      this->log_ACTIVITY_HI_StateTransition(
        getStateName(oldState), 
        getStateName(newState)
      );

      // Update F Prime telemetry
      this->tlmWrite_CurrentState(getStateName(newState));

      std::cout << "UML State Transition: " << getStateName(oldState) 
                << " -> " << getStateName(newState) << std::endl;
    }
  }

  const char* SlewUseCaseBlockComponentImpl::getStateName(StateMachineStates state)
  {
    switch (state) {
      case STATE_STATE: return "STATE";
      default: return "UNKNOWN";
    }
  }

  bool SlewUseCaseBlockComponentImpl::canTransition(StateMachineStates fromState, StateMachineStates toState)
  {
    // UML StateMachine transition validation logic
    // In a full implementation, this would check UML transition guards
    return true;  // For now, allow all transitions
  }

  // Activity implementations from UML model

  void SlewUseCaseBlockComponentImpl::execute_ON_CMD()
  {
    // Activity: ON_CMD
    // Generated from UML Activity: ON_CMD

    // Call behavior: 
    execute_getDesignT2D();
  }


} // end namespace Components
