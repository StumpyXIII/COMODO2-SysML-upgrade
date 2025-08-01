# Auto-generated F Prime component interface
# Generated from UML/SysML model: SlewUseCaseBlock

active component SlewUseCaseBlock {

  # Standard F Prime component ports
  sync input port schedIn: Svc.Sched
  output port cmdRegOut: Fw.CmdReg
  output port cmdResponseOut: Fw.CmdResponse
  output port eventOut: Fw.LogEvent
  output port tlmOut: Fw.Tlm

  # Commands derived from UML StateMachine events


  # Standard state machine control commands
  async command START_STATE_MACHINE
  async command STOP_STATE_MACHINE

  # Telemetry for state machine monitoring
  telemetry CurrentState: string

  # Events for state machine transitions
  event StateTransition(from: string, to: string) severity activity high
}
