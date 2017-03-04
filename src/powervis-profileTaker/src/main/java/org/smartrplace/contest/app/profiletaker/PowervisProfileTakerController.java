package org.smartrplace.contest.app.profiletaker;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;

import org.smartrplace.contest.app.profiletaker.config.PowervisProfileTakerConfig;
import de.iwes.util.resource.ResourceHelper;
import org.smartrplace.contest.app.profiletaker.patternlistener.ElectricityMeterListener;
import org.smartrplace.contest.app.profiletaker.pattern.ElectricityMeterPattern;

// here the controller logic is implemented
public class PowervisProfileTakerController {

	public OgemaLogger log;
    public ApplicationManager appMan;
    private ResourcePatternAccess advAcc;

	public PowervisProfileTakerConfig appConfigData;
	
    public PowervisProfileTakerController(ApplicationManager appMan) {
		this.appMan = appMan;
		this.log = appMan.getLogger();
		this.advAcc = appMan.getResourcePatternAccess();
		
        initConfigurationResource();
        initDemands();
	}

	public ElectricityMeterListener electricityMeterListener;

    /*
     * This app uses a central configuration resource, which is accessed here
     */
    private void initConfigurationResource() {
		String configResourceDefaultName = PowervisProfileTakerConfig.class.getSimpleName().substring(0, 1).toLowerCase()+PowervisProfileTakerConfig.class.getSimpleName().substring(1);
		appConfigData = appMan.getResourceAccess().getResource(configResourceDefaultName);
		if (appConfigData != null) { // resource already exists (appears in case of non-clean start)
			appMan.getLogger().debug("{} started with previously-existing config resource", getClass().getName());
		}
		else {
			appConfigData = (PowervisProfileTakerConfig) appMan.getResourceManagement().createResource(configResourceDefaultName, PowervisProfileTakerConfig.class);
			appConfigData.sampleElement().create();
			appConfigData.sampleElement().setValue("Example");
			appConfigData.activate(true);
			appMan.getLogger().debug("{} started with new config resource", getClass().getName());
		}
    }
    
    /*
     * register ResourcePatternDemands. The listeners will be informed about new and disappearing
     * patterns in the OGEMA resource tree
     */
    public void initDemands() {
		electricityMeterListener = new ElectricityMeterListener(this);
		advAcc.addPatternDemand(ElectricityMeterPattern.class, electricityMeterListener, AccessPriority.PRIO_LOWEST);
    }

	public void close() {
		advAcc.removePatternDemand(ElectricityMeterPattern.class, electricityMeterListener);
    }

	/*
	 * if the app needs to consider dependencies between different pattern types,
	 * they can be processed here.
	 */
	public void processInterdependies() {
		// TODO Auto-generated method stub
		
	}
}
