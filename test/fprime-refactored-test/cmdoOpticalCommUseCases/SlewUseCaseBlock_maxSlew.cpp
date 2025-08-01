#include "SlewUseCaseBlock.hpp"
#include <Fw/Types/Assert.hpp>

namespace Components {

  SlewUseCaseBlockComponentImpl::SlewUseCaseBlockComponentImpl(const char* const compName) :
    SlewUseCaseBlockComponentBase(compName),
    m_currentState(STATE_STATE),
    m_stateMachineActive(false)
  {
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
    initializeStateMachine();
    this->cmdResponse_out(opCode, cmdSeq, Fw::CmdResponse::OK);
  }

  void SlewUseCaseBlockComponentImpl::STOP_STATE_MACHINE_cmdHandler(
      const FwOpcodeType opCode,
      const U32 cmdSeq
  )
  {
    m_stateMachineActive = false;
    this->log_ACTIVITY_HI_StateTransition("ACTIVE", "STOPPED");
    this->cmdResponse_out(opCode, cmdSeq, Fw::CmdResponse::OK);
  }



  void SlewUseCaseBlockComponentImpl::transitionToState(StateMachineStates newState)
  {
    if (m_currentState != newState) {
      StateMachineStates oldState = m_currentState;
      m_currentState = newState;

      // Log state transition
      this->log_ACTIVITY_HI_StateTransition(
        getStateName(oldState), 
        getStateName(newState)
      );

      // Update telemetry
      this->tlmWrite_CurrentState(getStateName(newState));
    }
  }

  const char* SlewUseCaseBlockComponentImpl::getStateName(StateMachineStates state)
  {
    switch (state) {
      default: return "UNKNOWN";
    }
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
