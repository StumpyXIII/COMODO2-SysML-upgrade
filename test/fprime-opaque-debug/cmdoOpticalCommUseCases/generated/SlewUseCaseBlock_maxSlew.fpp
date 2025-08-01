module Components {

  active component SlewUseCaseBlock {

    @ FPP from XML: original path was Fw/Cfg/ConfigCheck.hpp
    include "ConfigCheck.hpp"

    @ FPP from XML: original path was Fw/Types/BasicTypes.hpp
    include "BasicTypes.hpp"

    @ FPP from XML: original path was Fw/Types/StringType.hpp
    include "StringType.hpp"

    sync input port schedIn: [1] Svc.Sched

    output port cmdRegOut: [1] Fw.CmdReg

    output port cmdResponseOut: [1] Fw.CmdResponse

    output port eventOut: [1] Fw.LogEvent

    output port tlmOut: [1] Fw.Tlm

    async command StartSM \
      opcode 0x00

    async command StopSM \
      opcode 0x01

    @ State machine transitioned from {0} to {1}
    event StateTransition(
                           from: string
                           to: string
                         ) \
      severity activity high \
      id 0 \
      format "State machine transitioned from {} to {}" \
      throttle 10

    telemetry CurrentState: string id 0

  }

}
