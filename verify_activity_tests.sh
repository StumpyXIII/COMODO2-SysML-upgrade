#!/bin/bash

# Test Verification Script for UML Activity Parsing
# Verifies that all expected activity parsing functionality works correctly

echo "=== UML Activity Parsing Test Verification ==="
echo

TEST_DIR="test/fprime-test-unique/cmdoOpticalCommUseCases"
PASSED=0
FAILED=0

# Function to run test and report result
run_test() {
    local test_name="$1"
    local command="$2"
    local expected="$3"
    
    echo "Testing: $test_name"
    
    if eval "$command" > /dev/null 2>&1; then
        echo "  ✅ PASSED: $test_name"
        ((PASSED++))
    else
        echo "  ❌ FAILED: $test_name"
        echo "     Expected: $expected"
        ((FAILED++))
    fi
}

# Test 1: Check activity declaration exists in header
run_test "Activity Declaration in Header" \
    "grep -q 'void execute_ON_CMD();' $TEST_DIR/SlewUseCaseBlock_maxSlew.hpp" \
    "execute_ON_CMD declaration"

# Test 2: Check activity implementation exists in source  
run_test "Activity Implementation in Source" \
    "grep -q 'void SlewUseCaseBlockComponentImpl::execute_ON_CMD()' $TEST_DIR/SlewUseCaseBlock_maxSlew.cpp" \
    "execute_ON_CMD implementation"

# Test 3: Check call behavior action is translated
run_test "Call Behavior Action Translation" \
    "grep -q 'execute_getDesignT2D();' $TEST_DIR/SlewUseCaseBlock_maxSlew.cpp" \
    "call to execute_getDesignT2D"

# Test 4: Check unique file naming (both components exist)
run_test "Unique Component Files Exist" \
    "[ -f '$TEST_DIR/SlewUseCaseBlock_maxSlew.hpp' ] && [ -f '$TEST_DIR/SlewUseCaseBlock_OpticalCommUseCases.hpp' ]" \
    "both component files"

# Test 5: Check activity comments are included
run_test "Activity Comments Included" \
    "grep -q 'Activity implementations from UML model' $TEST_DIR/SlewUseCaseBlock_maxSlew.cpp" \
    "activity implementation comments"

# Test 6: Check no activities in second component
run_test "No Activities in Second Component" \
    "! grep -q 'execute_ON_CMD' $TEST_DIR/SlewUseCaseBlock_OpticalCommUseCases.cpp" \
    "no activities in OpticalCommUseCases component"

# Test 7: Check proper C++ class structure
run_test "Proper C++ Class Structure" \
    "grep -A2 -B2 'Activity method declarations' $TEST_DIR/SlewUseCaseBlock_maxSlew.hpp | grep -q 'void execute_ON_CMD'" \
    "activity declaration in proper location"

# Test 8: Check file compilation readiness (basic syntax)
run_test "C++ Header Syntax Check" \
    "grep -q '#ifndef.*_HPP' $TEST_DIR/SlewUseCaseBlock_maxSlew.hpp && grep -q '#endif' $TEST_DIR/SlewUseCaseBlock_maxSlew.hpp" \
    "proper header guards"

echo
echo "=== Test Summary ==="
echo "✅ Passed: $PASSED"
echo "❌ Failed: $FAILED"
echo "Total:  $((PASSED + FAILED))"

if [ $FAILED -eq 0 ]; then
    echo
    echo "🎉 ALL TESTS PASSED! UML Activity parsing is working correctly."
    exit 0
else
    echo
    echo "⚠️  Some tests failed. Check the implementation."
    exit 1
fi