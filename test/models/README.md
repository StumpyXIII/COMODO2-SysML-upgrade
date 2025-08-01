# Test Models Directory

This directory contains test models for COMODO2 platform testing across different target code generation platforms.

## Directory Structure

```
test/models/
├── fprime-csrm/          # F Prime test models (CSRM - CubeSat System Reference Model)
│   ├── CSRM.uml          # Main SysML model with F Prime components
│   └── *.profile.uml     # Required UML/SysML profiles
├── uml-profiles/         # Common UML profiles (shared across tests)
│   └── *.profile.uml     # UML Standard profiles
└── README.md            # This file
```

## Test Models

### F Prime CSRM Model (`fprime-csrm/CSRM.uml`)
**Purpose**: Tests F Prime component generation with UML activities and external blocks  
**Platform**: FPRIME  
**Module**: cmdoOpticalCommUseCases  
**Components**: SlewUseCaseBlock  
**Features Tested**:
- UML Activity parsing and C++ method generation
- External block type generation (DecrementT2DData, PropulsionSubsystem)
- Mathematical formula extraction from OpaqueActions
- F Prime XML component generation
- Complete XMI → XML → FPP → C++ pipeline

**Key Activities**:
- `ON_CMD` - Main command handler activity
- `DecrementT2D` - Orbital mechanics calculation with formula: `T2D = DesignT2D - DesignT2D*aoa*.4/15`
- `getDesignT2D` - Design parameter retrieval
- `SlewForOpticalCommPayload` - Optical communication slewing logic

**Usage**:
```bash
comodo -i ./test/models/fprime-csrm/CSRM.uml -t fprime -m cmdoOpticalCommUseCases -o test/output/
```

## Profile Dependencies

The test models require various UML and SysML profiles:
- **SysML Profiles**: Block, Requirements, ModelElements
- **UML Standard Profiles**: MagicDraw profiles, Validation profiles  
- **CSRM Profiles**: Architecture Structures, Technical Measures, Validation & Verification
- **COMODO Profiles**: comodoProfile for component stereotypes

All required profiles are included in the respective model directories.

## Adding New Test Models

When adding new test models:
1. Create appropriate subdirectory under `test/models/`
2. Include all required profile dependencies
3. Update test configuration in `build.gradle`
4. Add documentation in this README
5. Create reference outputs in appropriate `test/[platform]-*/` directories

## Integration with Build System

These models are referenced in:
- `build.gradle` - Gradle test tasks
- `TEST_CASES_*.md` - Test case documentation  
- `verify_activity_tests.sh` - Manual test verification
- Various test automation scripts

All paths are relative to the `comodo2/` project root directory.