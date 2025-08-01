#ifndef SLEWUSECASEBLOCK_IMPL_HPP
#define SLEWUSECASEBLOCK_IMPL_HPP

// F Prime component implementation extending auto-generated base
// Generated from UML 5.x XMI: SlewUseCaseBlock
// StateMachine: OpticalCommUseCases

#include "SlewUseCaseBlockComponentBase.hpp"
#include "SlewUseCaseBlockComponentAc.hpp"

namespace Components {

  class SlewUseCaseBlockComponentImpl :
    public SlewUseCaseBlockComponentBase
  {

    public:
      SlewUseCaseBlockComponentImpl(const char* const compName);
      ~SlewUseCaseBlockComponentImpl();

      // UML StateMachine implementation
      void initializeStateMachine();
      void processStateMachine();
      void schedIn_handler(const NATIVE_INT_TYPE portNum, NATIVE_UINT_TYPE context);

      // Command handlers (implementing F Prime base class interface)
      void START_STATE_MACHINE_cmdHandler(
          const FwOpcodeType opCode,
          const U32 cmdSeq
      ) override;
      void STOP_STATE_MACHINE_cmdHandler(
          const FwOpcodeType opCode,
          const U32 cmdSeq
      ) override;



    private:
      // UML StateMachine state enumeration
      enum StateMachineStates {

        STATE_ON
      };

      StateMachineStates m_currentState;
      bool m_stateMachineActive;

      // UML StateMachine helper methods
      void transitionToState(StateMachineStates newState);
      const char* getStateName(StateMachineStates state);
      bool canTransition(StateMachineStates fromState, StateMachineStates toState);

      // UML Activity implementations

  };

} // end namespace Components

#endif // SLEWUSECASEBLOCK_IMPL_HPP
