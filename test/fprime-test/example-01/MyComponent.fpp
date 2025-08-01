# Auto-generated F Prime component interface
# Generated from UML/SysML model: MyComponent

active component MyComponent {

  # Standard F Prime component ports
  sync input port schedIn: Svc.Sched
  output port cmdRegOut: Fw.CmdReg
  output port cmdResponseOut: Fw.CmdResponse
  output port eventOut: Fw.LogEvent
  output port tlmOut: Fw.Tlm

  # Commands derived from UML model
  async command START_STATE_MACHINE
  async command STOP_STATE_MACHINE

  # Telemetry for state machine monitoring
  telemetry CurrentState: string

  # Events for state machine transitions
  event StateTransition(from: string, to: string) severity activity high
}
