#ifndef MYCOMPONENT_HPP
#define MYCOMPONENT_HPP

#include "MyComponentComponentAc.hpp"

namespace Components {

  class MyComponentComponentImpl :
    public MyComponentComponentBase
  {

    public:
      MyComponentComponentImpl(const char* const compName);
      ~MyComponentComponentImpl();

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

        STATE_,
        STATE_T1,
        STATE_T2,
        STATE_T3
      };

      StateMachineStates m_currentState;
      bool m_stateMachineActive;

      // Activity implementations from UML model
      void executeActivities();
  };

} // end namespace Components

#endif // MYCOMPONENT_HPP
