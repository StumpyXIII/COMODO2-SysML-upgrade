package comodo2.templates.fprime;

import comodo2.engine.Main;
import comodo2.queries.QClass;
import comodo2.queries.QStereotype;
import comodo2.utils.FilesHelper;
import javax.inject.Inject;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.IGenerator;

/**
 * Refactored F Prime Component Generator
 * 
 * Implements the new 3-stage pipeline:
 * 1. UML 5.x XMI → F Prime XML descriptors
 * 2. F Prime XML → F Prime Toolchain (fpp-to-cpp) → Auto-generated base classes
 * 3. UML Semantic Analysis → Implementation classes with rich UML-derived logic
 * 
 * This approach leverages F Prime's native toolchain for standards compliance
 * while preserving COMODO2's advanced UML semantic mapping capabilities.
 */
public class FPrimeComponent implements IGenerator {

	private static final Logger mLogger = Logger.getLogger(Main.class);

	@Inject
	private QClass mQClass;

	@Inject
	private QStereotype mQStereotype;

	@Inject
	private FilesHelper mFilesHelper;

	@Inject
	private FPrimeXMLGenerator mXmlGenerator;

	@Inject
	private FPrimeToolchainInvoker mToolchainInvoker;

	@Inject
	private FPrimeImplementation mImplementationGenerator;

	/**
	 * Main generation entry point - orchestrates the 3-stage pipeline
	 */
	@Override
	public void doGenerate(final Resource input, final IFileSystemAccess fsa) {
		mLogger.info("Starting F Prime generation with refactored XMI → XML → Toolchain → Implementation pipeline");
		
		final TreeIterator<EObject> allContents = input.getAllContents();
		while (allContents.hasNext()) {
			EObject e = allContents.next();
			if (e instanceof Class) {
				Class c = (Class) e;
				// Process UML Classes and SysML Blocks with cmdoComponent stereotype
				if (mQClass.isToBeGenerated((Element) c) && mQClass.hasStateMachines((Element) c)) {
					for (final StateMachine sm : mQClass.getStateMachines((Element) c)) {
						generateFPrimeComponentPipeline(c, sm, input, fsa);
					}
				}
			}
		}
		
		mLogger.info("F Prime generation pipeline completed");
	}

	/**
	 * Execute the complete 3-stage F Prime component generation pipeline
	 */
	private void generateFPrimeComponentPipeline(Class componentClass, StateMachine stateMachine, 
			Resource input, IFileSystemAccess fsa) {
		
		String componentName = componentClass.getName();
		String uniqueComponentName = componentName + "_" + stateMachine.getName();
		
		mLogger.info("Processing component: " + componentName + " (StateMachine: " + stateMachine.getName() + ")");
		
		try {
			// === STAGE 1: UML 5.x XMI → F Prime XML ===
			boolean xmlGenerated = generateFPrimeXML(componentClass, stateMachine, input, fsa, uniqueComponentName);
			if (!xmlGenerated) {
				mLogger.error("Failed to generate F Prime XML for: " + componentName);
				return;
			}
			
			// === STAGE 2: F Prime XML → Toolchain → Base Classes ===
			boolean baseClassesGenerated = generateFPrimeBaseClasses(uniqueComponentName, fsa);
			if (!baseClassesGenerated) {
				mLogger.warn("F Prime toolchain generation failed, continuing with mock base classes");
			}
			
			// === STAGE 3: UML Semantics → Implementation Logic ===
			boolean implementationGenerated = generateUMLImplementation(componentClass, stateMachine, input, fsa, uniqueComponentName);
			if (!implementationGenerated) {
				mLogger.error("Failed to generate UML implementation for: " + componentName);
				return;
			}
			
			// === STAGE 4: Build System Integration ===
			generateBuildIntegration(componentClass, uniqueComponentName, fsa);
			
			mLogger.info("Successfully generated F Prime component: " + uniqueComponentName);
			
		} catch (Exception e) {
			mLogger.error("Exception during F Prime component generation: " + e.getMessage(), e);
		}
	}

	/**
	 * STAGE 1: Generate F Prime XML descriptors from UML 5.x XMI
	 */
	private boolean generateFPrimeXML(Class componentClass, StateMachine stateMachine, 
			Resource input, IFileSystemAccess fsa, String uniqueComponentName) {
		
		mLogger.info("STAGE 1: Generating F Prime XML from UML XMI for: " + uniqueComponentName);
		
		try {
			// Generate component XML descriptor
			String componentXml = mXmlGenerator.generateComponentXML(componentClass, stateMachine, input);
			String xmlPath = mFilesHelper.toFPrimeXmlFilePath(uniqueComponentName);
			fsa.generateFile(xmlPath, componentXml);
			
			mLogger.info("Generated F Prime XML: " + xmlPath);
			
			// Optionally generate topology XML for multi-component systems
			// String topologyXml = mXmlGenerator.generateTopologyXML(input, uniqueComponentName + "Topology");
			// String topologyPath = mFilesHelper.toFPrimeTopologyXmlFilePath(uniqueComponentName);
			// fsa.generateFile(topologyPath, topologyXml);
			
			return true;
			
		} catch (Exception e) {
			mLogger.error("Failed to generate F Prime XML: " + e.getMessage(), e);
			return false;
		}
	}

	/**
	 * STAGE 2: Invoke F Prime toolchain to generate base classes
	 */
	private boolean generateFPrimeBaseClasses(String uniqueComponentName, IFileSystemAccess fsa) {
		mLogger.info("STAGE 2: Invoking F Prime toolchain for: " + uniqueComponentName);
		
		try {
			// Get absolute paths for toolchain invocation
			String xmlPath = mFilesHelper.toAbsolutePath(mFilesHelper.toFPrimeXmlFilePath(uniqueComponentName));
			String outputDir = mFilesHelper.toAbsolutePath(mFilesHelper.toFPrimeGeneratedDir());
			
			// Invoke F Prime toolchain (fpp-to-cpp)
			boolean success = mToolchainInvoker.generateBaseClasses(xmlPath, outputDir, uniqueComponentName);
			
			if (success) {
				// Validate that expected files were generated
				boolean validated = mToolchainInvoker.validateGeneratedFiles(outputDir, uniqueComponentName);
				if (validated) {
					mLogger.info("F Prime toolchain generation successful for: " + uniqueComponentName);
					return true;
				} else {
					mLogger.warn("F Prime toolchain completed but generated files validation failed");
					return false;
				}
			} else {
				mLogger.warn("F Prime toolchain invocation failed, using mock base classes");
				return false;  // Will use mock base classes
			}
			
		} catch (Exception e) {
			mLogger.error("Exception during F Prime toolchain invocation: " + e.getMessage(), e);
			return false;
		}
	}

	/**
	 * STAGE 3: Generate UML-derived implementation logic
	 */
	private boolean generateUMLImplementation(Class componentClass, StateMachine stateMachine, 
			Resource input, IFileSystemAccess fsa, String uniqueComponentName) {
		
		mLogger.info("STAGE 3: Generating UML-derived implementation for: " + uniqueComponentName);
		
		try {
			// Generate implementation header with UML-derived logic
			String implHeader = mImplementationGenerator.generateImplementationHeader(componentClass, stateMachine, input);
			String headerPath = mFilesHelper.toFPrimeImplHeaderFilePath(uniqueComponentName);
			fsa.generateFile(headerPath, implHeader);
			
			// Generate implementation source with rich UML semantics
			String implSource = mImplementationGenerator.generateImplementationSource(componentClass, stateMachine, input);
			String sourcePath = mFilesHelper.toFPrimeImplSourceFilePath(uniqueComponentName);
			fsa.generateFile(sourcePath, implSource);
			
			mLogger.info("Generated UML implementation files:");
			mLogger.info("  Header: " + headerPath);
			mLogger.info("  Source: " + sourcePath);
			
			return true;
			
		} catch (Exception e) {
			mLogger.error("Failed to generate UML implementation: " + e.getMessage(), e);
			return false;
		}
	}

	/**
	 * STAGE 4: Generate build system integration (CMakeLists.txt)
	 */
	private void generateBuildIntegration(Class componentClass, String uniqueComponentName, IFileSystemAccess fsa) {
		mLogger.info("STAGE 4: Generating build integration for: " + uniqueComponentName);
		
		try {
			String cmakeContent = generateCMakeFile(componentClass, uniqueComponentName);
			String cmakePath = mFilesHelper.toFPrimeCMakeFilePath(uniqueComponentName);
			fsa.generateFile(cmakePath, cmakeContent);
			
			mLogger.info("Generated CMake integration: " + cmakePath);
			
		} catch (Exception e) {
			mLogger.error("Failed to generate build integration: " + e.getMessage(), e);
		}
	}

	/**
	 * Generate enhanced CMakeLists.txt for F Prime component with new pipeline structure
	 */
	private String generateCMakeFile(Class componentClass, String uniqueComponentName) {
		StringBuilder cmake = new StringBuilder();
		
		cmake.append("# Auto-generated CMakeLists.txt for F Prime component\n");
		cmake.append("# Generated from UML 5.x XMI: ").append(componentClass.getName()).append("\n");
		cmake.append("# Pipeline: XMI → XML → F Prime Toolchain → UML Implementation\n");
		cmake.append("\n");
		
		cmake.append("# Component XML descriptor (input to F Prime toolchain)\n");
		cmake.append("set(COMPONENT_XML \"${CMAKE_CURRENT_LIST_DIR}/").append(uniqueComponentName).append("Component.xml\")\n");
		cmake.append("\n");
		
		cmake.append("# F Prime generated base classes (from toolchain)\n");
		cmake.append("set(FPRIME_GENERATED_DIR \"${CMAKE_CURRENT_LIST_DIR}/generated\")\n");
		cmake.append("\n");
		
		cmake.append("# UML-derived implementation files (our semantic logic)\n");
		cmake.append("set(IMPLEMENTATION_FILES\n");
		cmake.append("  \"${CMAKE_CURRENT_LIST_DIR}/").append(uniqueComponentName).append("ComponentImpl.hpp\"\n");
		cmake.append("  \"${CMAKE_CURRENT_LIST_DIR}/").append(uniqueComponentName).append("ComponentImpl.cpp\"\n");
		cmake.append(")\n");
		cmake.append("\n");
		
		cmake.append("# F Prime base class files (generated by toolchain or mocked)\n");
		cmake.append("set(FPRIME_BASE_FILES\n");
		cmake.append("  \"${FPRIME_GENERATED_DIR}/").append(uniqueComponentName).append("ComponentBase.hpp\"\n");
		cmake.append("  \"${FPRIME_GENERATED_DIR}/").append(uniqueComponentName).append("ComponentBase.cpp\"\n");
		cmake.append("  \"${FPRIME_GENERATED_DIR}/").append(uniqueComponentName).append("ComponentAc.hpp\"\n");
		cmake.append(")\n");
		cmake.append("\n");
		
		cmake.append("# Register F Prime module\n");
		cmake.append("register_fprime_module()\n");
		cmake.append("\n");
		
		cmake.append("# Component dependencies\n");
		cmake.append("set(MOD_DEPS\n");
		cmake.append("  Fw/Cmd\n");
		cmake.append("  Fw/Comp\n");
		cmake.append("  Fw/Log\n");
		cmake.append("  Fw/Tlm\n");
		cmake.append("  Fw/Types\n");
		cmake.append("  Svc/Sched\n");
		cmake.append(")\n");
		cmake.append("\n");
		
		cmake.append("# Include F Prime generated directory\n");
		cmake.append("include_directories(${FPRIME_GENERATED_DIR})\n");
		cmake.append("\n");
		
		cmake.append("# Build the component\n");
		cmake.append("set(SOURCE_FILES ${IMPLEMENTATION_FILES} ${FPRIME_BASE_FILES})\n");
		cmake.append("register_fprime_implementation()\n");
		cmake.append("\n");
		
		cmake.append("# Custom targets for F Prime toolchain integration\n");
		cmake.append("add_custom_target(generate_").append(uniqueComponentName.toLowerCase()).append("_base\n");
		cmake.append("  COMMAND fpp-to-cpp --input ${COMPONENT_XML} --output-dir ${FPRIME_GENERATED_DIR}\n");
		cmake.append("  DEPENDS ${COMPONENT_XML}\n");
		cmake.append("  COMMENT \"Generating F Prime base classes from XML\"\n");
		cmake.append(")\n");
		
		return cmake.toString();
	}
}