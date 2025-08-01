# Auto-generated CMakeLists.txt for F Prime component
# Generated from UML/SysML model: SlewUseCaseBlock

set(SOURCE_FILES
  "${CMAKE_CURRENT_LIST_DIR}/SlewUseCaseBlock.fpp"
  "${CMAKE_CURRENT_LIST_DIR}/SlewUseCaseBlock.cpp"
)

register_fprime_module()

set(MOD_DEPS
  Fw/Cmd
  Fw/Log
  Fw/Tlm
  Svc/Sched
)

register_fprime_implementation()
