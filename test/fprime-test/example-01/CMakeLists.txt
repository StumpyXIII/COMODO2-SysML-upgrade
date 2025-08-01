# Auto-generated CMakeLists.txt for F Prime component
# Generated from UML/SysML model: MyComponent

set(SOURCE_FILES
  "${CMAKE_CURRENT_LIST_DIR}/MyComponent.fpp"
  "${CMAKE_CURRENT_LIST_DIR}/MyComponent.cpp"
)

register_fprime_module()

set(MOD_DEPS
  Fw/Cmd
  Fw/Log
  Fw/Tlm
  Svc/Sched
)

register_fprime_implementation()
