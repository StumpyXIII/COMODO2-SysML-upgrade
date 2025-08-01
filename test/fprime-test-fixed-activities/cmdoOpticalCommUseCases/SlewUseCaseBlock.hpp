#ifndef SLEWUSECASEBLOCK_HPP
#define SLEWUSECASEBLOCK_HPP

#include "SlewUseCaseBlockComponentAc.hpp"

namespace Components {

  class SlewUseCaseBlockComponentImpl :
    public SlewUseCaseBlockComponentBase
  {

    public:
      SlewUseCaseBlockComponentImpl(const char* const compName);
      ~SlewUseCaseBlockComponentImpl();

      // State machine implementation
      void initializeStateMachine();
      void processStateMachine();

      // Command handlers
      void START_STATE_MACHINE_cmdHandler(
          const FwOpcodeType opCode,
          const U32 cmdSeq
      );
      void STOP_STATE_MACHINE_cmdHandler(
          const FwOpcodeType opCode,
          const U32 cmdSeq
      );

    private:
      // State machine state enumeration
      enum StateMachineStates {

        STATE_ON
      };

      StateMachineStates m_currentState;
      bool m_stateMachineActive;

      void transitionToState(StateMachineStates newState);
      const char* getStateName(StateMachineStates state);


  };

} // end namespace Components

#endif // SLEWUSECASEBLOCK_HPP
