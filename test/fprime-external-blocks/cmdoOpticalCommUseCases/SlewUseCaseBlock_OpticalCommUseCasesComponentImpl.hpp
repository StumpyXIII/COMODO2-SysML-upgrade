#ifndef SLEWUSECASEBLOCK_IMPL_HPP
#define SLEWUSECASEBLOCK_IMPL_HPP

// F Prime component implementation extending auto-generated base
// Generated from UML 5.x XMI: SlewUseCaseBlock
// StateMachine: OpticalCommUseCases

#include "SlewUseCaseBlockComponentBase.hpp"
#include "SlewUseCaseBlockComponentAc.hpp"

// External Block Type Definitions
// Generated from UML Block references in activities

struct DecrementT2DData {
  // F Prime requires fixed-size data members (no dynamic allocation)
  F64 DesignT2D;     // Design time-to-decay value
  F64 slewRate;      // Slew rate for orbital mechanics
  F64 timeStep;      // Current time step
  F64 aoa;           // Angle of attack
  F64 T2D;           // Current time-to-decay
  
  // Default constructor for F Prime compatibility
  DecrementT2DData() : DesignT2D(0.0), slewRate(0.0), timeStep(0.0), aoa(0.0), T2D(0.0) {}
};

class PropulsionSubsystem {
public:
  // F Prime subsystem interface - no dynamic allocation
  F64 DesignT2D;     // Design time-to-decay from subsystem
  bool isActive;     // Subsystem active status
  
  // Constructor for F Prime initialization
  PropulsionSubsystem() : DesignT2D(0.0), isActive(false) {}
  
  // F Prime subsystem operations
  void initialize(F64 designValue) { DesignT2D = designValue; isActive = true; }
  F64 getDesignT2D() const { return DesignT2D; }
  bool getStatus() const { return isActive; }
};


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
      // Activity method declarations from UML model
      void execute_CubeSat Activity A();
      void execute_CubeSat Subsystem Activity A();
      DecrementT2DData execute_DecrementT2D(const DecrementT2DData& input);
      void execute_Enterprise Activity A();
      void execute_Ground Segment Activity A();
      void execute_Ground Subsystem Activity A();
      void execute_ON_CMD();
      void execute_SlewForOpticalCommPayload(const PropulsionSubsystem& PropulsionSubystemObject);
      void execute_Validation Activity 1();
      void execute_Validation Activity 2();
      void execute_Verification Activity 1();
      void execute_Verification Activity 2();
      void execute_getDesignT2D(const PropulsionSubsystem& inputObject);

  };

} // end namespace Components

#endif // SLEWUSECASEBLOCK_IMPL_HPP
