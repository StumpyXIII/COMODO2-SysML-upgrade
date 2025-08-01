#include "SlewUseCaseBlock_maxSlewComponentBase.hpp"
#include <iostream>

// Mock F Prime component base implementation (generated when F Prime toolchain unavailable)

namespace Components {

  SlewUseCaseBlock_maxSlewComponentBase::SlewUseCaseBlock_maxSlewComponentBase(const char* const compName) :
    Fw::ActiveComponentBase(compName)
  {
    std::cout << "Mock F Prime component created: " << compName << std::endl;
  }

  SlewUseCaseBlock_maxSlewComponentBase::~SlewUseCaseBlock_maxSlewComponentBase()
  {
  }

  void SlewUseCaseBlock_maxSlewComponentBase::cmdResponse_out(FwOpcodeType opCode, U32 cmdSeq, Fw::CmdResponse response)
  {
    mockCmdResponse(opCode, cmdSeq, response);
  }

  void SlewUseCaseBlock_maxSlewComponentBase::tlmWrite_CurrentState(const char* val)
  {
    mockTlmWrite("CurrentState", val);
  }

  void SlewUseCaseBlock_maxSlewComponentBase::log_ACTIVITY_HI_StateTransition(const char* from, const char* to)
  {
    mockLogEvent("StateTransition", from, to);
  }

  void SlewUseCaseBlock_maxSlewComponentBase::mockCmdResponse(FwOpcodeType opCode, U32 cmdSeq, Fw::CmdResponse response)
  {
    std::cout << "Mock CMD Response: opCode=0x" << std::hex << opCode
              << " cmdSeq=" << std::dec << cmdSeq << " response=" << response << std::endl;
  }

  void SlewUseCaseBlock_maxSlewComponentBase::mockTlmWrite(const char* channel, const char* val)
  {
    std::cout << "Mock TLM: " << channel << " = " << val << std::endl;
  }

  void SlewUseCaseBlock_maxSlewComponentBase::mockLogEvent(const char* event, const char* arg1, const char* arg2)
  {
    std::cout << "Mock EVENT: " << event << "(" << arg1 << ", " << arg2 << ")" << std::endl;
  }

} // end namespace Components
