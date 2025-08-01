#ifndef SLEWUSECASEBLOCK_IMPL_HPP
#define SLEWUSECASEBLOCK_IMPL_HPP

// F Prime component implementation extending auto-generated base
// Generated from UML 5.x XMI: SlewUseCaseBlock
// StateMachine: maxSlew

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

        STATE_STATE
      };

      StateMachineStates m_currentState;
      bool m_stateMachineActive;

      // UML StateMachine helper methods
      void transitionToState(StateMachineStates newState);
      const char* getStateName(StateMachineStates state);
      bool canTransition(StateMachineStates fromState, StateMachineStates toState);

      // UML Activity implementations
      // Activity method declarations from UML model
      void execute_CubeSat Activity A();
      void execute_CubeSat Subsystem Activity A();
      DecrementT2DData execute_DecrementT2D(const DecrementT2DData& input);
      void execute_Enterprise Activity A();
      void execute_Ground Segment Activity A();
      void execute_Ground Subsystem Activity A();
      void execute_ON_CMD();
      void execute_SlewForOpticalCommPayload(const Propulsion Subsystem& PropulsionSubystemObject);
      void execute_Validation Activity 1();
      void execute_Validation Activity 2();
      void execute_Verification Activity 1();
      void execute_Verification Activity 2();
      void execute_getDesignT2D(const Propulsion Subsystem& inputObject);

  };

} // end namespace Components

#endif // SLEWUSECASEBLOCK_IMPL_HPP
