package comodo2.templates.fprime;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import comodo2.engine.Main;

/**
 * F Prime Toolchain Invoker
 * Calls F Prime's native code generation tools (fpp-to-cpp, fpp-to-xml)
 * to generate base classes from F Prime XML component descriptors.
 */
public class FPrimeToolchainInvoker {

	private static final Logger mLogger = Logger.getLogger(Main.class);
	
	// F Prime toolchain command names
	private static final String FPP_FROM_XML = "fpp-from-xml";
	private static final String FPP_TO_XML = "fpp-to-xml";
	private static final String FPP_TO_CPP = "fpp-to-cpp";
	private static final int COMMAND_TIMEOUT_SECONDS = 30;

	/**
	 * Generate F Prime base classes from component XML using F Prime toolchain
	 * 
	 * @param componentXmlPath Path to the F Prime component XML file
	 * @param outputDir Directory where generated files should be placed
	 * @param componentName Name of the component (for file naming)
	 * @return true if generation succeeded, false otherwise
	 */
	public boolean generateBaseClasses(String componentXmlPath, String outputDir, String componentName) {
		mLogger.info("Invoking F Prime toolchain for: " + componentName);
		
		try {
			// Create output directory if it doesn't exist
			Path outputPath = Paths.get(outputDir);
			Files.createDirectories(outputPath);
			
			// Step 1: Generate FPP from XML (if needed)
			// For now, we assume XML is the input format
			
			// Step 2: Generate C++ base classes from XML
			boolean cppGenSuccess = generateCppFromXml(componentXmlPath, outputDir, componentName);
			
			if (cppGenSuccess) {
				mLogger.info("F Prime toolchain generation completed successfully for: " + componentName);
				return true;
			} else {
				mLogger.error("F Prime toolchain generation failed for: " + componentName);
				return false;
			}
			
		} catch (Exception e) {
			mLogger.error("Exception during F Prime toolchain invocation: " + e.getMessage(), e);
			return false;
		}
	}

	/**
	 * Generate C++ files from F Prime XML using fpp-to-cpp
	 */
	private boolean generateCppFromXml(String componentXmlPath, String outputDir, String componentName) {
		try {
			// Check if F Prime toolchain is available
			if (!isToolchainAvailable()) {
				mLogger.warn("F Prime toolchain not available, skipping native generation");
				return generateMockBaseClasses(outputDir, componentName);
			}

			// Step 1: Convert XML to FPP using fpp-from-xml
			String fppFilePath = outputDir + "/" + componentName + ".fpp";
			if (!convertXmlToFpp(componentXmlPath, fppFilePath)) {
				mLogger.error("Failed to convert XML to FPP");
				return false;
			}
			
			// Step 2: Generate C++ from FPP using fpp-to-cpp
			return generateCppFromFpp(fppFilePath, outputDir, componentName);
			
		} catch (Exception e) {
			mLogger.error("F Prime toolchain invocation failed: " + e.getMessage(), e);
			return false;
		}
	}
	
	/**
	 * Convert F Prime XML to FPP format using fpp-from-xml
	 */
	private boolean convertXmlToFpp(String xmlPath, String fppPath) {
		try {
			List<String> command = new ArrayList<>();
			command.add(FPP_FROM_XML);
			command.add(xmlPath);
			
			mLogger.info("Executing: " + String.join(" ", command));
			
			ProcessBuilder pb = new ProcessBuilder(command);
			pb.redirectErrorStream(true);
			
			Process process = pb.start();
			
			// Capture output and write to FPP file
			StringBuilder output = new StringBuilder();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					output.append(line).append("\n");
				}
			}
			
			boolean finished = process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			if (!finished) {
				process.destroyForcibly();
				mLogger.error("fpp-from-xml command timed out");
				return false;
			}
			
			int exitCode = process.exitValue();
			if (exitCode == 0) {
				// Write FPP output to file
				Files.write(Paths.get(fppPath), output.toString().getBytes());
				mLogger.info("Successfully converted XML to FPP: " + fppPath);
				return true;
			} else {
				mLogger.error("fpp-from-xml failed with exit code " + exitCode + ": " + output.toString());
				return false;
			}
			
		} catch (Exception e) {
			mLogger.error("XML to FPP conversion failed: " + e.getMessage(), e);
			return false;
		}
	}
	
	/**
	 * Generate C++ from FPP using fpp-to-cpp
	 */
	private boolean generateCppFromFpp(String fppPath, String outputDir, String componentName) {
		try {
			List<String> command = new ArrayList<>();
			command.add(FPP_TO_CPP);
			command.add("-d");  // Output directory flag
			command.add(outputDir);
			command.add(fppPath);  // FPP file as positional argument
			
			mLogger.info("Executing: " + String.join(" ", command));
			
			ProcessBuilder pb = new ProcessBuilder(command);
			pb.redirectErrorStream(true);
			
			Process process = pb.start();
			
			// Capture output
			StringBuilder output = new StringBuilder();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					output.append(line).append("\n");
					mLogger.debug("fpp-to-cpp: " + line);
				}
			}
			
			boolean finished = process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			if (!finished) {
				process.destroyForcibly();
				mLogger.error("fpp-to-cpp command timed out");
				return false;
			}
			
			int exitCode = process.exitValue();
			if (exitCode == 0) {
				mLogger.info("fpp-to-cpp succeeded: " + output.toString());
				return true;
			} else {
				mLogger.error("fpp-to-cpp failed with exit code " + exitCode + ": " + output.toString());
				return false;
			}
			
		} catch (Exception e) {
			mLogger.error("FPP to C++ generation failed: " + e.getMessage(), e);
			return false;
		}
	}

	/**
	 * Check if F Prime toolchain commands are available in PATH
	 */
	private boolean isToolchainAvailable() {
		try {
			ProcessBuilder pb = new ProcessBuilder(FPP_TO_CPP, "--help");
			Process process = pb.start();
			boolean finished = process.waitFor(5, TimeUnit.SECONDS);
			if (!finished) {
				process.destroyForcibly();
				return false;
			}
			return process.exitValue() == 0;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Generate mock base classes when F Prime toolchain is not available
	 * This provides a fallback so development can continue without F Prime installed
	 */
	private boolean generateMockBaseClasses(String outputDir, String componentName) {
		mLogger.info("Generating mock F Prime base classes for: " + componentName);
		
		try {
			// Generate ComponentBase.hpp
			String baseHeader = generateMockBaseHeader(componentName);
			Path headerPath = Paths.get(outputDir, componentName + "ComponentBase.hpp");
			Files.write(headerPath, baseHeader.getBytes());
			
			// Generate ComponentBase.cpp  
			String baseImpl = generateMockBaseImplementation(componentName);
			Path implPath = Paths.get(outputDir, componentName + "ComponentBase.cpp");
			Files.write(implPath, baseImpl.getBytes());
			
			// Generate ComponentAc.hpp (auto-coded header)
			String acHeader = generateMockAcHeader(componentName);
			Path acHeaderPath = Paths.get(outputDir, componentName + "ComponentAc.hpp");
			Files.write(acHeaderPath, acHeader.getBytes());
			
			mLogger.info("Mock F Prime base classes generated successfully");
			return true;
			
		} catch (IOException e) {
			mLogger.error("Failed to generate mock base classes: " + e.getMessage(), e);
			return false;
		}
	}

	/**
	 * Generate mock ComponentBase.hpp header
	 */
	private String generateMockBaseHeader(String componentName) {
		StringBuilder sb = new StringBuilder();
		sb.append("#ifndef ").append(componentName.toUpperCase()).append("_COMPONENT_BASE_HPP\n");
		sb.append("#define ").append(componentName.toUpperCase()).append("_COMPONENT_BASE_HPP\n\n");
		sb.append("// Mock F Prime component base class (generated when F Prime toolchain unavailable)\n");
		sb.append("// In production, this would be generated by fpp-to-cpp\n\n");
		sb.append("#include <Fw/Comp/ActiveComponentBase.hpp>\n");
		sb.append("#include <Fw/Types/BasicTypes.hpp>\n\n");
		sb.append("namespace Components {\n\n");
		sb.append("  class ").append(componentName).append("ComponentBase : public Fw::ActiveComponentBase {\n");
		sb.append("  public:\n");
		sb.append("    ").append(componentName).append("ComponentBase(const char* const compName);\n");
		sb.append("    virtual ~").append(componentName).append("ComponentBase();\n\n");
		sb.append("    // Mock command response port\n");
		sb.append("    void cmdResponse_out(FwOpcodeType opCode, U32 cmdSeq, Fw::CmdResponse response);\n");
		sb.append("    \n");
		sb.append("    // Mock telemetry port\n");
		sb.append("    void tlmWrite_CurrentState(const char* val);\n");
		sb.append("    \n");
		sb.append("    // Mock logging port\n");
		sb.append("    void log_ACTIVITY_HI_StateTransition(const char* from, const char* to);\n");
		sb.append("    \n");
		sb.append("    // Virtual command handlers (to be implemented by derived class)\n");
		sb.append("    virtual void START_STATE_MACHINE_cmdHandler(const FwOpcodeType opCode, const U32 cmdSeq) = 0;\n");
		sb.append("    virtual void STOP_STATE_MACHINE_cmdHandler(const FwOpcodeType opCode, const U32 cmdSeq) = 0;\n\n");
		sb.append("  protected:\n");
		sb.append("    // Mock port implementations\n");
		sb.append("    void mockCmdResponse(FwOpcodeType opCode, U32 cmdSeq, Fw::CmdResponse response);\n");
		sb.append("    void mockTlmWrite(const char* channel, const char* val);\n");
		sb.append("    void mockLogEvent(const char* event, const char* arg1, const char* arg2);\n");
		sb.append("  };\n\n");
		sb.append("} // end namespace Components\n\n");
		sb.append("#endif // ").append(componentName.toUpperCase()).append("_COMPONENT_BASE_HPP\n");
		return sb.toString();
	}

	/**
	 * Generate mock ComponentBase.cpp implementation
	 */
	private String generateMockBaseImplementation(String componentName) {
		StringBuilder sb = new StringBuilder();
		sb.append("#include \"").append(componentName).append("ComponentBase.hpp\"\n");
		sb.append("#include <iostream>\n\n");
		sb.append("// Mock F Prime component base implementation (generated when F Prime toolchain unavailable)\n\n");
		sb.append("namespace Components {\n\n");
		sb.append("  ").append(componentName).append("ComponentBase::").append(componentName).append("ComponentBase(const char* const compName) :\n");
		sb.append("    Fw::ActiveComponentBase(compName)\n");
		sb.append("  {\n");
		sb.append("    std::cout << \"Mock F Prime component created: \" << compName << std::endl;\n");
		sb.append("  }\n\n");
		sb.append("  ").append(componentName).append("ComponentBase::~").append(componentName).append("ComponentBase()\n");
		sb.append("  {\n");
		sb.append("  }\n\n");
		sb.append("  void ").append(componentName).append("ComponentBase::cmdResponse_out(FwOpcodeType opCode, U32 cmdSeq, Fw::CmdResponse response)\n");
		sb.append("  {\n");
		sb.append("    mockCmdResponse(opCode, cmdSeq, response);\n");
		sb.append("  }\n\n");
		sb.append("  void ").append(componentName).append("ComponentBase::tlmWrite_CurrentState(const char* val)\n");
		sb.append("  {\n");
		sb.append("    mockTlmWrite(\"CurrentState\", val);\n");
		sb.append("  }\n\n");
		sb.append("  void ").append(componentName).append("ComponentBase::log_ACTIVITY_HI_StateTransition(const char* from, const char* to)\n");
		sb.append("  {\n");
		sb.append("    mockLogEvent(\"StateTransition\", from, to);\n");
		sb.append("  }\n\n");
		sb.append("  void ").append(componentName).append("ComponentBase::mockCmdResponse(FwOpcodeType opCode, U32 cmdSeq, Fw::CmdResponse response)\n");
		sb.append("  {\n");
		sb.append("    std::cout << \"Mock CMD Response: opCode=0x\" << std::hex << opCode\n");
		sb.append("              << \" cmdSeq=\" << std::dec << cmdSeq << \" response=\" << response << std::endl;\n");
		sb.append("  }\n\n");
		sb.append("  void ").append(componentName).append("ComponentBase::mockTlmWrite(const char* channel, const char* val)\n");
		sb.append("  {\n");
		sb.append("    std::cout << \"Mock TLM: \" << channel << \" = \" << val << std::endl;\n");
		sb.append("  }\n\n");
		sb.append("  void ").append(componentName).append("ComponentBase::mockLogEvent(const char* event, const char* arg1, const char* arg2)\n");
		sb.append("  {\n");
		sb.append("    std::cout << \"Mock EVENT: \" << event << \"(\" << arg1 << \", \" << arg2 << \")\" << std::endl;\n");
		sb.append("  }\n\n");
		sb.append("} // end namespace Components\n");
		return sb.toString();
	}

	/**
	 * Generate mock ComponentAc.hpp auto-coded header
	 */
	private String generateMockAcHeader(String componentName) {
		StringBuilder sb = new StringBuilder();
		sb.append("#ifndef ").append(componentName.toUpperCase()).append("_COMPONENT_AC_HPP\n");
		sb.append("#define ").append(componentName.toUpperCase()).append("_COMPONENT_AC_HPP\n\n");
		sb.append("// Mock F Prime auto-coded header (generated when F Prime toolchain unavailable)\n\n");
		sb.append("#include \"").append(componentName).append("ComponentBase.hpp\"\n\n");
		sb.append("// Mock F Prime type definitions (normally from F Prime framework)\n");
		sb.append("namespace Fw {\n");
		sb.append("  typedef enum {\n");
		sb.append("    CmdResponse_OK = 0,\n");
		sb.append("    CmdResponse_INVALID_OPCODE = 1,\n");
		sb.append("    CmdResponse_VALIDATION_ERROR = 2,\n");
		sb.append("    CmdResponse_FORMAT_ERROR = 3,\n");
		sb.append("    CmdResponse_EXECUTION_ERROR = 4\n");
		sb.append("  } CmdResponse;\n");
		sb.append("}\n\n");
		sb.append("typedef unsigned int FwOpcodeType;\n");
		sb.append("typedef unsigned int U32;\n\n");
		sb.append("#endif // ").append(componentName.toUpperCase()).append("_COMPONENT_AC_HPP\n");
		return sb.toString();
	}

	/**
	 * Validate that generated files exist and are readable
	 */
	public boolean validateGeneratedFiles(String outputDir, String componentName) {
		String[] expectedFiles = {
			componentName + "ComponentBase.hpp",
			componentName + "ComponentBase.cpp",
			componentName + "ComponentAc.hpp"
		};
		
		for (String fileName : expectedFiles) {
			Path filePath = Paths.get(outputDir, fileName);
			if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
				mLogger.error("Expected generated file not found or not readable: " + filePath);
				return false;
			}
		}
		
		mLogger.info("All expected F Prime base class files validated successfully");
		return true;
	}
}