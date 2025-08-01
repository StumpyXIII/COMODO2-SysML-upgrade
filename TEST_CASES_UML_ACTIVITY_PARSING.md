# Test Cases for UML Activity Parsing in F Prime Generation

## Test Suite: UML Activity to C++ Code Generation

### Test Case 1: Basic Activity Detection
**Objective**: Verify that UML Activities are correctly identified and parsed from StateMachines

**Test Data**: CSRM.uml model with cmdoOpticalCommUseCases module
- Component: SlewUseCaseBlock  
- StateMachine: maxSlew
- Expected Activity: ON_CMD

**Test Steps**:
```bash
comodo -i ./test/models/fprime-csrm/CSRM.uml -t fprime -m cmdoOpticalCommUseCases -o test/fprime-activity-test1/
```

**Expected Results**:
- Debug output shows: "FPrimeActivity: Found 1 activities in SlewUseCaseBlock"
- Debug output shows: "Activity: ON_CMD"
- Generated files: `SlewUseCaseBlock_maxSlew.*`

**Verification Commands**:
```bash
# Check activity declaration in header
grep -n "execute_ON_CMD" test/fprime-activity-test1/cmdoOpticalCommUseCases/SlewUseCaseBlock_maxSlew.hpp

# Check activity implementation in source
grep -A10 "execute_ON_CMD" test/fprime-activity-test1/cmdoOpticalCommUseCases/SlewUseCaseBlock_maxSlew.cpp
```

**Status**: ✅ PASSED (verified in development)

---

### Test Case 2: Call Behavior Action Parsing
**Objective**: Verify that CallBehaviorAction nodes are correctly translated to C++ method calls

**Test Data**: Same as Test Case 1
- Activity: ON_CMD contains CallBehaviorAction to "getDesignT2D"

**Expected Results**:
- Generated C++ implementation contains: `execute_getDesignT2D();`
- Debug output shows: "Call behavior: " followed by behavior name

**Verification**:
```bash
grep -A5 -B5 "execute_getDesignT2D" test/fprime-activity-test1/cmdoOpticalCommUseCases/SlewUseCaseBlock_maxSlew.cpp
```

**Status**: ✅ PASSED (verified in development)

---

### Test Case 3: Multiple Components with Same Name
**Objective**: Verify unique naming prevents file overwrites when multiple components share names

**Test Data**: CSRM.uml generates two SlewUseCaseBlock components
- SlewUseCaseBlock (StateMachine: maxSlew) - Has activities
- SlewUseCaseBlock (StateMachine: OpticalCommUseCases) - No activities

**Expected Results**:
- Two separate file sets generated:
  - `SlewUseCaseBlock_maxSlew.*` (with activities)
  - `SlewUseCaseBlock_OpticalCommUseCases.*` (without activities)
- No file overwrites occur

**Verification**:
```bash
ls -la test/fprime-activity-test1/cmdoOpticalCommUseCases/SlewUseCaseBlock_*
```

**Status**: ✅ PASSED (verified in development)

---

### Test Case 4: Empty Activity Handling
**Objective**: Verify graceful handling of components with no activities

**Test Data**: SlewUseCaseBlock with OpticalCommUseCases StateMachine (0 activities)

**Expected Results**:
- Debug output shows: "No activities found for SlewUseCaseBlock"
- No activity declarations in header file
- No activity implementations in source file
- Files still generate correctly with basic F Prime structure

**Verification**:
```bash
# Should return no results
grep "execute_" test/fprime-activity-test1/cmdoOpticalCommUseCases/SlewUseCaseBlock_OpticalCommUseCases.hpp
grep "execute_" test/fprime-activity-test1/cmdoOpticalCommUseCases/SlewUseCaseBlock_OpticalCommUseCases.cpp
```

**Status**: ✅ PASSED (verified in development)

---

### Test Case 5: Activity Method Declaration Integration
**Objective**: Verify activity method declarations are properly integrated into C++ class structure

**Expected Results**:
- Activity declarations appear in private section of class
- Proper C++ syntax with correct indentation
- No syntax errors in generated header

**Verification**:
```bash
# Check class structure around activity declarations
grep -A3 -B3 "Activity method declarations" test/fprime-activity-test1/cmdoOpticalCommUseCases/SlewUseCaseBlock_maxSlew.hpp
```

**Status**: ✅ PASSED (verified in development)

---

### Test Case 6: Activity Implementation Integration  
**Objective**: Verify activity implementations are properly integrated into C++ implementation file

**Expected Results**:
- Activity implementations appear at end of file before namespace closing
- Proper C++ method signature and implementation structure
- Includes descriptive comments from UML model

**Verification**:
```bash
# Check implementation structure
grep -A10 -B2 "Activity implementations from UML model" test/fprime-activity-test1/cmdoOpticalCommUseCases/SlewUseCaseBlock_maxSlew.cpp
```

**Status**: ✅ PASSED (verified in development) 

---

### Test Case 7: Complex Activity Flow (Future Test)
**Objective**: Test complex activity flows with decision nodes, merge nodes, etc.

**Test Data**: Would need UML model with:
- InitialNode → DecisionNode → Multiple paths → MergeNode → ActivityFinalNode
- Guard conditions on decision paths
- Multiple action nodes in sequence

**Expected Results**:
- Generated C++ includes if/else logic for decision nodes
- Sequential execution for action chains  
- Proper flow control structure

**Status**: 🚧 NOT IMPLEMENTED (requires test model with complex activities)

---

### Test Case 8: SendSignalAction Integration (Future Test)
**Objective**: Test SendSignalAction nodes map to F Prime commands

**Test Data**: Would need UML model with SendSignalAction nodes

**Expected Results**:
- SendSignalAction generates F Prime command calls
- Integrates with F Prime command/response pattern

**Status**: 🚧 NOT IMPLEMENTED (part of next todo item - UML Signals to F Prime commands)

---

### Test Case 9: Error Handling - Malformed Activities
**Objective**: Verify graceful handling of malformed or incomplete activities

**Test Scenarios**:
- Activity with null/empty name
- Activity with no nodes
- Activity with disconnected nodes
- Activity with cycles

**Expected Results**:
- No crashes or exceptions
- Graceful fallback behavior
- Appropriate debug/warning messages

**Status**: 🔄 NEEDS TESTING (requires malformed test models)

---

### Test Case 10: Performance Test - Large Models
**Objective**: Verify performance with models containing many activities

**Test Data**: Model with 50+ activities across multiple components

**Expected Results**:
- Generation completes in reasonable time (< 30 seconds)
- All activities are correctly processed
- No memory issues or slowdowns

**Status**: 🔄 NEEDS TESTING (requires large test model)

---

## Test Execution Instructions

### Running All Basic Tests
```bash
# Run comprehensive test
comodo -i ./test/models/fprime-csrm/CSRM.uml -t fprime -m cmdoOpticalCommUseCases -o test/fprime-comprehensive-test/ 2>&1 | tee test_results.log

# Verify all expected outputs
bash verify_activity_tests.sh
```

### Test Environment Setup
- Clean build: `ant clean build install`  
- Test output directory: `test/fprime-activity-tests/`
- Debug output enabled for verification

### Success Criteria
- ✅ All basic tests (1-6) must pass
- 🚧 Future tests (7-8) are implementation targets
- 🔄 Error handling tests (9-10) need test data creation

---

## Current Test Status Summary

| Test Case | Status | Notes |
|-----------|--------|-------|
| 1. Basic Activity Detection | ✅ PASSED | Core functionality verified |
| 2. Call Behavior Action | ✅ PASSED | Behavior calls working |  
| 3. Multiple Components | ✅ PASSED | Unique naming prevents overwrites |
| 4. Empty Activity Handling | ✅ PASSED | Graceful degradation |
| 5. Declaration Integration | ✅ PASSED | Proper C++ class structure |
| 6. Implementation Integration | ✅ PASSED | Complete method generation |
| 7. Complex Activity Flow | 🚧 TODO | Needs complex test model |
| 8. SendSignalAction | 🚧 TODO | Part of next development phase |
| 9. Error Handling | 🔄 NEEDS TESTING | Requires malformed models |
| 10. Performance | 🔄 NEEDS TESTING | Requires large models |

**Overall Status**: **6/6 Core Tests PASSED** ✅  
**Next Phase**: UML Signals to F Prime Commands (Todo Item #3)