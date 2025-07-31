package comodo2.queries;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import com.google.common.collect.Iterables;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Stereotype;

/**
 * Enhanced Stereotype Query with Cameo 2024r3 compatibility
 * Supports modern Cameo stereotypes and legacy MagicDraw stereotypes
 */
public class QStereotype {
	
	// Known Cameo/MagicDraw stereotype variations
	private static final Set<String> CAMEO_COMPONENT_VARIATIONS = new HashSet<>(Arrays.asList(
		"cmdoComponent", "Component", "SysML::Blocks::Block", "Block"
	));

	private static final Set<String> SYSML_BLOCK_VARIATIONS = new HashSet<>(Arrays.asList(
		"SysML::Blocks::Block", "Block", "SysML.Blocks.Block", "SysMLBlock"
	));
	
	private static final Set<String> CAMEO_INTERFACE_VARIATIONS = new HashSet<>(Arrays.asList(
		"cmdoInterface", "Interface", "SysML::Ports and Flows::InterfaceBlock", "InterfaceBlock"
	));
	
	private static final Set<String> CAMEO_MODULE_VARIATIONS = new HashSet<>(Arrays.asList(
		"cmdoModule", "Module", "Package"
	));
	
	private static final Set<String> CAMEO_STRUCTURE_VARIATIONS = new HashSet<>(Arrays.asList(
		"cmdoStructure", "Structure", "ValueType", "DataType"
	));
	
	private static final Set<String> CAMEO_ENUMERATION_VARIATIONS = new HashSet<>(Arrays.asList(
		"cmdoEnumeration", "Enumeration", "SysML::Blocks::ValueType"
	));
	public boolean isComodoInterface(final Element e) {
		return this.hasAnyStereotype(e, CAMEO_INTERFACE_VARIATIONS);
	}

	public boolean isComodoComponent(final Element e) {
		return this.hasAnyStereotype(e, CAMEO_COMPONENT_VARIATIONS);
	}

	public boolean isComodoModule(final Element e) {
		return this.hasAnyStereotype(e, CAMEO_MODULE_VARIATIONS);
	}

	public boolean isComodoStructure(final Element e) {
		return this.hasAnyStereotype(e, CAMEO_STRUCTURE_VARIATIONS);
	}

	public boolean isComodoEnumeration(final Element e) {
		return this.hasAnyStereotype(e, CAMEO_ENUMERATION_VARIATIONS);
	}

	public boolean isComodoUnion(final Element e) {
		return this.hasStereotype(e, "cmdoUnion");
	}

	public boolean isComodoException(final Element e) {
		return this.hasStereotype(e, "cmdoException");
	}

	public boolean isComodoCommand(final Element e) {
		return this.hasStereotype(e, "cmdoCommand");
	}

	public boolean isComodoInternal(final Element e) {
		return this.hasStereotype(e, "cmdoInternal");
	}

	/**
	 * Enhanced stereotype checking with Cameo 2024r3 compatibility
	 * Checks for stereotype variations and qualified names
	 */
	public boolean hasStereotype(final Element e, final String stereotypeName) {
		if ((e == null)) {
			return false;
		}
		if (e.getAppliedStereotypes() == null) {
			return false;
		}
		
		for (Stereotype s : Iterables.<Stereotype>filter(e.getAppliedStereotypes(), Stereotype.class)) {
			if (matchesStereotype(s, stereotypeName)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if element has any of the given stereotype variations
	 */
	public boolean hasAnyStereotype(final Element e, final Set<String> stereotypeNames) {
		if ((e == null) || stereotypeNames == null || stereotypeNames.isEmpty()) {
			return false;
		}
		if (e.getAppliedStereotypes() == null) {
			return false;
		}
		
		for (Stereotype s : Iterables.<Stereotype>filter(e.getAppliedStereotypes(), Stereotype.class)) {
			for (String stereotypeName : stereotypeNames) {
				if (matchesStereotype(s, stereotypeName)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Checks if element has any of the given stereotype variations (List version)
	 */
	public boolean hasAnyStereotype(final Element e, final List<String> stereotypeNames) {
		if ((e == null) || stereotypeNames == null || stereotypeNames.isEmpty()) {
			return false;
		}
		if (e.getAppliedStereotypes() == null) {
			return false;
		}
		
		for (Stereotype s : Iterables.<Stereotype>filter(e.getAppliedStereotypes(), Stereotype.class)) {
			for (String stereotypeName : stereotypeNames) {
				if (matchesStereotype(s, stereotypeName)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Enhanced stereotype matching that handles qualified names and variations
	 */
	private boolean matchesStereotype(Stereotype stereotype, String targetName) {
		if (stereotype == null || targetName == null) {
			return false;
		}
		
		String stereotypeName = stereotype.getName();
		if (stereotypeName == null) {
			return false;
		}
		
		// Direct name match
		if (stereotypeName.equals(targetName)) {
			return true;
		}
		
		// Check qualified name match (e.g., "SysML::Blocks::Block")
		String qualifiedName = stereotype.getQualifiedName();
		if (qualifiedName != null && qualifiedName.equals(targetName)) {
			return true;
		}
		
		// Check if target name ends with the stereotype name (for qualified names)
		if (targetName.contains("::") && targetName.endsWith("::" + stereotypeName)) {
			return true;
		}
		
		// Check profile-qualified name
		try {
			Profile profile = stereotype.getProfile();
			if (profile != null && profile.getName() != null) {
				String profileQualifiedName = profile.getName() + "::" + stereotypeName;
				if (profileQualifiedName.equals(targetName)) {
					return true;
				}
			}
		} catch (Exception e) {
			// Ignore exceptions in profile access
		}
		
		return false;
	}
	
	/**
	 * Check if element is a SysML Block
	 */
	public boolean isSysMLBlock(final Element element) {
		return hasAnyStereotype(element, SYSML_BLOCK_VARIATIONS);
	}

	/**
	 * Utility method to get all applied stereotype names for debugging
	 */
	public String getAppliedStereotypeNames(final Element e) {
		if (e == null || e.getAppliedStereotypes() == null) {
			return "none";
		}
		
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Stereotype s : Iterables.<Stereotype>filter(e.getAppliedStereotypes(), Stereotype.class)) {
			if (!first) {
				sb.append(", ");
			}
			sb.append(s.getName());
			if (s.getQualifiedName() != null && !s.getQualifiedName().equals(s.getName())) {
				sb.append(" (").append(s.getQualifiedName()).append(")");
			}
			first = false;
		}
		return sb.toString();
	}
}
