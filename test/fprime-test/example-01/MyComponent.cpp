#include "MyComponent.hpp"
#include <Fw/Types/Assert.hpp>

namespace Components {

  MyComponentComponentImpl::MyComponentComponentImpl(const char* const compName) :
    MyComponentComponentBase(compName),
    m_currentState(STATE_T1),
    m_stateMachineActive(false)
  {
  }

  MyComponentComponentImpl::~MyComponentComponentImpl()
  {
  }

  void MyComponentComponentImpl::initializeStateMachine()
  {
    m_currentState = STATE_T1;
    m_stateMachineActive = true;
    this->log_ACTIVITY_HI_StateTransition("INIT", "T1");
    this->tlmWrite_CurrentState("T1");
  }

  void MyComponentComponentImpl::processStateMachine()
  {
    if (!m_stateMachineActive) {
      return;
    }

    // TODO: Implement state machine logic from UML model
    switch (m_currentState) {
      case STATE_:
        // State: 
        executeActivities();
        break;

      case STATE_T1:
        // State: T1
        executeActivities();
        break;

      case STATE_T2:
        // State: T2
        executeActivities();
        break;

      case STATE_T3:
        // State: T3
        executeActivities();
        break;

      default:
        FW_ASSERT(0);
        break;
    }
  }

  void MyComponentComponentImpl::START_STATE_MACHINE_cmdHandler(
      const FwOpcodeType opCode,
      const U32 cmdSeq
  )
  {
    initializeStateMachine();
    this->cmdResponse_out(opCode, cmdSeq, Fw::CmdResponse::OK);
  }

  void MyComponentComponentImpl::STOP_STATE_MACHINE_cmdHandler(
      const FwOpcodeType opCode,
      const U32 cmdSeq
  )
  {
    m_stateMachineActive = false;
    this->log_ACTIVITY_HI_StateTransition("ACTIVE", "STOPPED");
    this->cmdResponse_out(opCode, cmdSeq, Fw::CmdResponse::OK);
  }

  void MyComponentComponentImpl::executeActivities()
  {
    // TODO: Implement UML Activity logic here
  }

} // end namespace Components
