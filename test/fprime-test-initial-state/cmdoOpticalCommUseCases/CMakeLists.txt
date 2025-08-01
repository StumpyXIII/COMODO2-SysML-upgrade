# Auto-generated CMakeLists.txt for F Prime component
# Generated from UML/SysML model: SlewUseCaseBlock_OpticalCommUseCases

set(SOURCE_FILES
  "${CMAKE_CURRENT_LIST_DIR}/SlewUseCaseBlock_OpticalCommUseCases.fpp"
  "${CMAKE_CURRENT_LIST_DIR}/SlewUseCaseBlock_OpticalCommUseCases.cpp"
)

register_fprime_module()

set(MOD_DEPS
  Fw/Cmd
  Fw/Log
  Fw/Tlm
  Svc/Sched
)

register_fprime_implementation()
