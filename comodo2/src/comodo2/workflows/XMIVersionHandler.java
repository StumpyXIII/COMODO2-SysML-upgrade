package comodo2.workflows;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * XMI Version Handler for Cameo 2024r3 compatibility
 * Detects and handles different XMI schema versions and namespace declarations
 */
public class XMIVersionHandler {
    
    // Known XMI version mappings
    private static final Map<String, String> XMI_VERSION_MAPPINGS = new HashMap<>();
    
    static {
        // Standard XMI versions
        XMI_VERSION_MAPPINGS.put("20131001", "XMI_2013");
        XMI_VERSION_MAPPINGS.put("20110701", "XMI_2011");
        XMI_VERSION_MAPPINGS.put("20100901", "XMI_2010");
        XMI_VERSION_MAPPINGS.put("20090901", "XMI_2009");
        
        // Legacy versions
        XMI_VERSION_MAPPINGS.put("2.0", "XMI_2_0");
        XMI_VERSION_MAPPINGS.put("2.1", "XMI_2_1");
        XMI_VERSION_MAPPINGS.put("2.4", "XMI_2_4");
        XMI_VERSION_MAPPINGS.put("2.5", "XMI_2_5");
    }
    
    /**
     * XMI Version Information
     */
    public static class XMIVersionInfo {
        private String xmiVersion;
        private String umlNamespace;
        private String schemaLocation;
        private Map<String, String> namespaceDeclarations;
        
        public XMIVersionInfo() {
            this.namespaceDeclarations = new HashMap<>();
        }
        
        // Getters and setters
        public String getXmiVersion() { return xmiVersion; }
        public void setXmiVersion(String xmiVersion) { this.xmiVersion = xmiVersion; }
        
        public String getUmlNamespace() { return umlNamespace; }
        public void setUmlNamespace(String umlNamespace) { this.umlNamespace = umlNamespace; }
        
        public String getSchemaLocation() { return schemaLocation; }
        public void setSchemaLocation(String schemaLocation) { this.schemaLocation = schemaLocation; }
        
        public Map<String, String> getNamespaceDeclarations() { return namespaceDeclarations; }
        public void setNamespaceDeclarations(Map<String, String> namespaceDeclarations) { 
            this.namespaceDeclarations = namespaceDeclarations; 
        }
        
        public boolean isCameoVersion() {
            return umlNamespace != null && (
                umlNamespace.contains("omg.org") ||
                umlNamespace.contains("magicdraw") ||
                umlNamespace.contains("nomagic")
            );
        }
        
        public boolean isEclipseUML2Version() {
            return umlNamespace != null && umlNamespace.contains("eclipse.org/uml2");
        }
        
        @Override
        public String toString() {
            return String.format("XMIVersionInfo[xmi=%s, uml=%s, cameo=%s, eclipse=%s]", 
                xmiVersion, umlNamespace, isCameoVersion(), isEclipseUML2Version());
        }
    }
    
    /**
     * Analyzes XMI version and namespace information from a UML resource
     */
    public static XMIVersionInfo analyzeXMIVersion(Resource resource) {
        XMIVersionInfo info = new XMIVersionInfo();
        
        if (resource == null || resource.getURI() == null) {
            return info;
        }
        
        try {
            URI resourceURI = resource.getURI();
            URIConverter converter = resource.getResourceSet() != null ? 
                resource.getResourceSet().getURIConverter() : 
                URIConverter.INSTANCE;
                
            try (InputStream inputStream = converter.createInputStream(resourceURI)) {
                parseXMIHeader(inputStream, info);
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not analyze XMI version for " + resource.getURI() + ": " + e.getMessage());
        }
        
        return info;
    }
    
    /**
     * Parses XMI header to extract version and namespace information
     */
    private static void parseXMIHeader(InputStream inputStream, XMIVersionInfo info) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            
            Node rootElement = doc.getDocumentElement();
            if (rootElement != null) {
                extractVersionInfo(rootElement, info);
                extractNamespaceDeclarations(rootElement, info);
            }
            
        } catch (Exception e) {
            System.err.println("Warning: Could not parse XMI header: " + e.getMessage());
        }
    }
    
    /**
     * Extracts XMI version information from root element
     */
    private static void extractVersionInfo(Node rootElement, XMIVersionInfo info) {
        NamedNodeMap attributes = rootElement.getAttributes();
        
        // Look for XMI version attribute
        Node xmiVersionAttr = attributes.getNamedItem("xmi:version");
        if (xmiVersionAttr != null) {
            info.setXmiVersion(xmiVersionAttr.getNodeValue());
        }
        
        // Look for schema location
        Node schemaLocationAttr = attributes.getNamedItem("xsi:schemaLocation");
        if (schemaLocationAttr != null) {
            info.setSchemaLocation(schemaLocationAttr.getNodeValue());
        }
    }
    
    /**
     * Extracts namespace declarations from root element
     */
    private static void extractNamespaceDeclarations(Node rootElement, XMIVersionInfo info) {
        NamedNodeMap attributes = rootElement.getAttributes();
        
        for (int i = 0; i < attributes.getLength(); i++) {
            Attr attr = (Attr) attributes.item(i);
            String attrName = attr.getName();
            String attrValue = attr.getValue();
            
            // Capture all namespace declarations
            if (attrName.startsWith("xmlns:")) {
                String prefix = attrName.substring(6); // Remove "xmlns:"
                info.getNamespaceDeclarations().put(prefix, attrValue);
                
                // Identify UML namespace
                if (attrValue.contains("UML") || prefix.equals("uml")) {
                    info.setUmlNamespace(attrValue);
                }
            } else if (attrName.equals("xmlns")) {
                info.getNamespaceDeclarations().put("", attrValue);
                if (attrValue.contains("UML")) {
                    info.setUmlNamespace(attrValue);
                }
            }
        }
    }
    
    /**
     * Maps XMI version to known version identifier
     */
    public static String mapXMIVersion(String xmiVersion) {
        if (xmiVersion == null) {
            return "UNKNOWN";
        }
        return XMI_VERSION_MAPPINGS.getOrDefault(xmiVersion, "UNKNOWN_" + xmiVersion);
    }
    
    /**
     * Determines if the XMI version is compatible with current parser
     */
    public static boolean isCompatibleVersion(XMIVersionInfo info) {
        if (info == null) {
            return false;
        }
        
        // Accept Eclipse UML2 versions
        if (info.isEclipseUML2Version()) {
            return true;
        }
        
        // Accept OMG/Cameo versions we know about
        if (info.isCameoVersion()) {
            String namespace = info.getUmlNamespace();
            return namespace.contains("20131001") || 
                   namespace.contains("20161101") || 
                   namespace.contains("20210201") ||
                   namespace.contains("omg.org/uml");
        }
        
        return false;
    }
    
    /**
     * Provides compatibility recommendations for unsupported versions
     */
    public static String getCompatibilityRecommendation(XMIVersionInfo info) {
        if (info == null || isCompatibleVersion(info)) {
            return "Version is compatible";
        }
        
        StringBuilder recommendation = new StringBuilder();
        recommendation.append("Detected potentially incompatible XMI version. ");
        
        if (info.isCameoVersion()) {
            recommendation.append("Try exporting from Cameo using 'File -> Export To -> Eclipse UML2 XMI 5.x'. ");
        } else {
            recommendation.append("Try using Eclipse UML2 XMI format. ");
        }
        
        recommendation.append("Current detection: ").append(info.toString());
        
        return recommendation.toString();
    }
}