package org.smartrfactory.contest.app.matchinggui;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;

import org.smartrfactory.contest.app.matchinggui.config.PowervizDevicematchingGuiConfig;
import de.iwes.util.resource.ResourceHelper;
import org.smartrfactory.contest.app.matchinggui.patternlistener.ElectricityMeterListener;
import org.smartrfactory.contest.app.matchinggui.pattern.ElectricityMeterPattern;

// here the controller logic is implemented
public class PowervizDevicematchingGuiController {

	public OgemaLogger log;
    public ApplicationManager appMan;
    private ResourcePatternAccess advAcc;

	public PowervizDevicematchingGuiConfig appConfigData;
	
    public PowervizDevicematchingGuiController(ApplicationManager appMan) {
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
		String configResourceDefaultName = PowervizDevicematchingGuiConfig.class.getSimpleName().substring(0, 1).toLowerCase()+PowervizDevicematchingGuiConfig.class.getSimpleName().substring(1);
		appConfigData = appMan.getResourceAccess().getResource(configResourceDefaultName);
		if (appConfigData != null) { // resource already exists (appears in case of non-clean start)
			appMan.getLogger().debug("{} started with previously-existing config resource", getClass().getName());
		}
		else {
			appConfigData = (PowervizDevicematchingGuiConfig) appMan.getResourceManagement().createResource(configResourceDefaultName, PowervizDevicematchingGuiConfig.class);
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
