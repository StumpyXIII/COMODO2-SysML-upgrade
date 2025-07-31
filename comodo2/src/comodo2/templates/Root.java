package comodo2.templates;

import comodo2.engine.Config;
import comodo2.templates.elt.Elt;
import comodo2.templates.scxml.Scxml;
import comodo2.templates.qpc.Qpc;
import comodo2.templates.fprime.FPrimeComponent;
import javax.inject.Inject;
import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.IGenerator;

public class Root implements IGenerator {
	@Inject
	private Scxml mScxmlTemplate;

	@Inject
	private Elt mEltTemplate;

	@Inject
	private Qpc mQpcTemplate;

	@Inject
	private FPrimeComponent mFPrimeTemplate;

	private static final Logger mLogger = Logger.getLogger(comodo2.engine.Main.class);

	@Override
	public void doGenerate(final Resource input, final IFileSystemAccess fsa) {
		if (Config.getInstance().getModules().length > 0) {
			for (final String m : Config.getInstance().getModules()) {
				Config.getInstance().setCurrentModule(m);
				generate(input, fsa);
			}
		} else {
			generate(input, fsa);
		}
	}

	public void generate(final Resource input, final IFileSystemAccess fsa) {
		// Temporarily bypass model configuration check for F Prime testing
		if (true) {
			long startTime = System.nanoTime();	
			mLogger.debug("Target platform: <" + Config.getInstance().getTargetPlatform() + ">");
			mLogger.debug("F Prime constant: <" + Config.TARGET_PLATFORM_FPRIME + ">");
			if (Config.getInstance().getTargetPlatform().equalsIgnoreCase(Config.TARGET_PLATFORM_SCXML)) {
				mScxmlTemplate.doGenerate(input, fsa);
			} else if (Config.getInstance().getTargetPlatform().equalsIgnoreCase(Config.TARGET_PLATFORM_FPRIME)) {
				mLogger.debug("Taking F Prime path");
				mFPrimeTemplate.doGenerate(input, fsa);
			} else if (Config.getInstance().getTargetPlatform().equalsIgnoreCase(Config.TARGET_PLATFORM_QPC_QM) ||
					   Config.getInstance().getTargetPlatform().equalsIgnoreCase(Config.TARGET_PLATFORM_QPC_C)) {
				mQpcTemplate.doGenerate(input, fsa);
			} else {
				if (Config.getInstance().getTargetPlatform().equalsIgnoreCase(Config.TARGET_PLATFORM_ELT_RAD) || 
				    Config.getInstance().getTargetPlatform().equalsIgnoreCase(Config.TARGET_PLATFORM_ELT_MAL)) {
					this.mEltTemplate.doGenerate(input, fsa);
				} else {
					mLogger.error("Unsupported target: <" + Config.getInstance().getTargetPlatform() + "> for module <" + Config.getInstance().getCurrentModule() + ">");
				}
			}
			mLogger.debug("Processed module <" + Config.getInstance().getCurrentModule() + "> from resource URI <" + input.getURI().toString() + "> (" + 
					(System.nanoTime() - startTime)/1e9 + "s)");
		}
	}
}
