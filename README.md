# ⚠️ **WARNING: THIS UPGRADE WAS VIBECODED - USE AT RISK** ⚠️

<div style="color: red; background-color: #ffe6e6; border: 2px solid red; padding: 15px; margin: 10px 0; border-radius: 5px;">
<strong>🚨 DANGER ZONE 🚨</strong><br/>
This version contains significant changes that were implemented through AI-assisted development ("vibecoding"). 
While comprehensive automated testing has been implemented and all tests pass, this should be considered 
experimental code. Use with extreme caution in production environments.
</div>

COMODO2
=======
COMODO2 is a Java tool that allows to transform a UML/SysML model into code for different software platforms.

Installation from OpenMBEE Git repository using ant
---------------------------------------------------
To install COMODO2 from OpenMBEE GIT repository:

    $ git clone https://github.com/Open-MBEE/Comodo.git comodo2  
    $ cd comodo2
    $ ant build
    $ ant install

JARs (including comodo2.jar) are installed by ant in `$PREFIX/lib` directory.

A wrapper script, comodo, that sets the correct classpath is installed in `$PREFIX/bin` directory.

Installation from ESO Git repository using waf
----------------------------------------------
To install COMODO2 from ESO GIT repository (it requires waf and wtools):

    $ git clone https://gitlab.eso.org/ifw/comodo2.git comodo2  
    $ cd comodo2
    $ waf configure
    $ waf install


JARs (including comodo2.jar) are installed by waf in `$PREFIX/lib` directory.

A wrapper script, comodo, that sets the correct classpath is installed in `$PREFIX/bin` directory.

Execution
---------
COMODO2 can be executed by invoking a wrapper script which takes care to set the proper path for the required JAR files.

    $ comodo <parameters>

For example:

    $ comodo -i comodo2/test/hello/model/EELT_ICS_ApplicationFramework.uml -o ./gen -m "hellomalif hellomal" -t ELT-RAD -g ALL -a -d

The supported options are:

    -h, --help, Print help.
    -d, --debug, Enable printing of debug information.
    -i, --input-model, File path of the model to transform. Model should be in EMF XMI (.uml) format.
    -o, --output-path, Output directory path.
    -m, --modules, Specify the module(s) to generate.
    -t, --target-platform, Target platform [SCXML|ELT-RAD|ELT-MAL].		
    -c, --target-platform-config, Configuration parameters specific to a target platform.
    -g, --generation-mode, Generation mode [DEFAULT|UPDATE|ALL].
    -n, --no-backup, Disable automatic backup for generated files (i.e. generated files may overwrite existing files with the same name).
    -a, --avoid-fully-qualified, Avoid using fully qualified names.
     
Input Model
-----------
The input model should comply with COMODO profile and be stored in EMF XMI 5.x format.

For example, in MagicDrow, use the option: File -> Export to -> Eclipse UML2 XMI 5.x to export the model.

Target Platforms
----------------
The following target platforms are supported:

  - *SCXML* Transforms the input model into SCXML document(s). The generated SCXML document can be executed by an SCXML interpreted.
  - *ELT-MAL* Transforms the input model into XML/MAL ICD. The generated XML file can be transformed into code using CII tools.
  - *ELT-RAD* Transforms the input model into C++ code, SCXML, XML/MAL ICD, for the Rapid Application Development toolkit (RAD). The generated code can be compiled and executed on a machine installed with the ELT Development Environment and RAD.
  - *QPC-C* Transofrms the input model into C code representing the state machine logic and structure, using the [Quantum Framework (QP/C)](https://www.state-machine.com/qpc/). 
  - *QPC-QM* Transofrms the input model into a QM file for the [Quantum Modeler](https://www.state-machine.com/qm/). This also generates implementation files that are used for the C code QM will generate.
  - *FPRIME* Transforms the input model into F Prime framework components with complete C++ implementations. Extracts UML activity logic from OpaqueActions and generates self-contained F Prime modules with command handlers, telemetry, and event logging.
   
Generation Modes
----------------
The following generation modes are supported:

  - *ALL* To be used only the very first time, it generates all files including actions, do-activities, actionMgr. If -n option is used, generated files will overwrite existing files without backup.
  - *UPDATE* To be used when new actions or do activities are added, it generates new action classes, new do-activities classes, and regenerate the actionMgr. If -n option is used, the actionMgr.cpp file will be overwritten without backup.
  - *DEFAULT* This is the default mode. It generates only files that should never be edited by the developer. E.g. the SCXML file, MAL/ICD, .rad.ev, etc.

Generating C++ Code from Cameo/MagicDraw for F Prime
===================================================

COMODO2 supports generating complete F Prime framework components from UML/SysML models created in Cameo Systems Modeler or MagicDraw. This process transforms UML activity diagrams with embedded logic into production-ready C++ code.

## Design Flow

The F Prime code generation follows a specific pipeline:

1. **UML/SysML Model (Cameo)** → Export to Eclipse UML2 XMI 5.x format
2. **UML XMI** → Generate F Prime XML component definitions  
3. **F Prime XML** → Generate FPP (F Prime Protocol) files using `fpp-from-xml`
4. **FPP Files** → Generate C++ base classes using `fpp-to-cpp`
5. **UML Activities** → Extract logic and generate complete C++ implementations
6. **Integration** → Combine generated base classes with UML-derived implementations

## Model Requirements

Your UML/SysML model should contain:

- **State Machines**: Define component behavior and states
- **Activities**: Contain the actual business logic in OpaqueActions
- **External Blocks**: Define data structures (e.g., `<<Block>>` stereotyped classes)
- **Subsystems**: Define external interfaces (e.g., `<<Subsystem>>` stereotyped classes)

### Activity Logic Support

COMODO2 extracts actual implementation logic from UML activities, including:

- **OpaqueActions**: Mathematical formulas and business logic
- **ReadStructuralFeatureAction**: Reading object attributes 
- **WriteStructuralFeatureAction**: Setting object attributes
- **CallBehaviorAction**: Calling other activities
- **ActivityParameterNodes**: Input/output parameters

Example UML activity logic that gets extracted:
```cpp
// From UML OpaqueAction:
auto timeStep = timeStep + 1;
auto aoa = slewRate*timeStep;
auto T2D = DesignT2D - DesignT2D*aoa*.4/15;
```

## Generating F Prime Code

### Prerequisites

1. **F Prime Toolchain**: Install F Prime development environment
   ```bash
   # Install F Prime tools
   pip install fprime-tools
   ```

2. **COMODO2**: Build and install COMODO2
   ```bash
   git clone <repository-url> comodo2
   cd comodo2
   ant clean build install
   ```

### Export Model from Cameo

1. Open your UML/SysML model in Cameo Systems Modeler
2. Go to **File** → **Export To** → **Eclipse UML2 XMI 5.x**
3. Save as `.uml` file (e.g., `MyModel.uml`)

### Generate F Prime Components

Run COMODO2 with the FPRIME target:

```bash
comodo -i MyModel.uml -o ./generated -m "MyComponent" -t FPRIME -g ALL -a -d
```

**Parameters:**
- `-i`: Input UML model file
- `-o`: Output directory for generated code
- `-m`: Module/component name to generate
- `-t FPRIME`: Use F Prime target platform
- `-g ALL`: Generate all files (use first time)
- `-a`: Avoid fully qualified names
- `-d`: Enable debug output

### Generated Files

The generation process creates:

```
generated/
├── MyComponent/
│   ├── MyComponentComponent.xml     # F Prime component definition
│   ├── MyComponentImpl.hpp          # Implementation header
│   ├── MyComponentImpl.cpp          # Implementation with UML logic
│   ├── CMakeLists.txt               # Build integration
│   └── generated/
│       ├── MyComponent.fpp          # F Prime protocol file
│       ├── MyComponentComponentBase.hpp  # Generated base class
│       └── MyComponentComponentBase.cpp  # Generated base class
```

## Testing F Prime Functionality

COMODO2 includes comprehensive automated testing for F Prime generation. 

### Running Automated Tests

Execute all F Prime tests using Gradle:

```bash
# Run comprehensive F Prime feature tests
./gradlew testFPRIMEComprehensiveTests

# Run UML signal mapping tests  
./gradlew testFPRIMESignalMappingTests

# Run all automated tests
./gradlew testAll --continue
```

### Test Coverage

The automated test suite validates:

**Comprehensive F Prime Tests (10 test cases):**
1. Basic Activity Detection
2. Call Behavior Action Parsing  
3. Multiple Components with Same Name
4. Enhanced Activity Discovery
5. Activity Method Declaration Integration
6. Activity Implementation Integration
7. External Block Type Generation
8. UML Activity Logic Extraction
9. F Prime Component Structure
10. C++ Compilation Readiness

**UML Signal Mapping Tests (5 test cases):**
1. Signal Detection Analysis
2. UML Signal Infrastructure Verification
3. F Prime Command Integration
4. Command Handler Structure
5. State Machine Integration with Commands

### Manual Testing

To manually test generated F Prime components:

1. **Verify Generated Structure:**
   ```bash
   # Check that all required files are generated
   ls -la generated/MyComponent/
   
   # Verify F Prime XML is valid
   fpp-from-xml generated/MyComponent/MyComponentComponent.xml
   ```

2. **Test Activity Logic Extraction:**
   ```bash
   # Look for extracted mathematical formulas in implementation
   grep -A 5 -B 5 "auto.*=" generated/MyComponent/MyComponentImpl.cpp
   
   # Verify activity method signatures
   grep "execute_.*(" generated/MyComponent/MyComponentImpl.hpp
   ```

3. **Validate F Prime Integration:**
   ```bash
   # Check command handlers are generated
   grep "cmdHandler" generated/MyComponent/MyComponentImpl.cpp
   
   # Verify telemetry and event logging
   grep -E "(tlmWrite|log_)" generated/MyComponent/MyComponentImpl.cpp
   ```

### Troubleshooting

**Common Issues:**

1. **F Prime Toolchain Not Found:**
   ```bash
   # Ensure F Prime tools are in PATH
   which fpp-from-xml fpp-to-cpp
   
   # Install if missing
   pip install fprime-tools
   ```

2. **Missing ConfigCheck.hpp:**
   - This is expected when F Prime project context is not available
   - COMODO2 automatically falls back to mock base classes
   - Generated implementation files will still contain extracted UML logic

3. **Activity Logic Not Extracted:**
   - Ensure UML activities contain OpaqueActions with actual code
   - Verify model is exported in UML2 XMI 5.x format
   - Check COMODO2 debug output for parsing errors

4. **Build Integration Issues:**
   - Use generated CMakeLists.txt as starting point
   - Integrate with existing F Prime project structure
   - Ensure all external dependencies are included

For detailed test case examples, see:
- `test/TEST_CASES_UML_ACTIVITY_PARSING.md`
- `test/TEST_CASES_UML_SIGNAL_MAPPING.md`
  
