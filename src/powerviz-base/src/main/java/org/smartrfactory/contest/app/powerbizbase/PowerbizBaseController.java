package org.smartrfactory.contest.app.powerbizbase;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;
import org.ogema.model.metering.ElectricityMeter;
import org.ogema.tools.resource.util.ResourceUtils;
import org.smartrfactory.contest.app.powerbizbase.config.PowervizBaseConfig;
import org.smartrfactory.contest.app.powerbizbase.config.PowervizPlant;
import org.smartrfactory.contest.app.powerbizbase.config.PowervizPlantOperationalState;
import org.smartrfactory.contest.app.powerbizbase.config.PowervizPlantType;
import org.smartrplace.util.multiresourcedemand.MultiResourceDemand;
import org.smartrplace.util.multiresourcedemand.MultiResourceDemandHelper;
import org.smartrplace.util.multiresourcedemand.MultiResourceDemandListener;
import org.smartrplace.util.multiresourcedemand.SingleResourceDemand;

import de.iwes.util.logconfig.LogHelper;
import de.iwes.util.resource.ValueResourceHelper;

// here the controller logic is implemented
public class PowerbizBaseController {

	public OgemaLogger log;
    public ApplicationManager appMan;
    private ResourcePatternAccess advAcc;

	public PowervizBaseConfig appConfigData;
	
    public PowerbizBaseController(ApplicationManager appMan) {
		this.appMan = appMan;
		this.log = appMan.getLogger();
		this.advAcc = appMan.getResourcePatternAccess();
		
        initConfigurationResource();
        initDemands();
        
        initLogging();
	}

    private void initLogging() {
    	/*MultiResourceDemand demand = new MultiResourceDemand(true,
    			new SingleResourceDemand(ElectricityMeter.class, "PopcornMachineMeter"));
    	MultiResourceDemandHelper.registerStartupDemand(demand, new MultiResourceDemandListener() {
			@Override
			public void demandComplete(MultiResourceDemand demand) {
				LogHelper.activateLogging(demand.get(ElectricityMeter.class).connection().powerSensor().reading());
			}
		}, appMan);
    	demand = new MultiResourceDemand(true,
    			new SingleResourceDemand(ElectricityMeter.class, "LenzeDemoMeter"));
    	MultiResourceDemandHelper.registerStartupDemand(demand, new MultiResourceDemandListener() {
			@Override
			public void demandComplete(MultiResourceDemand demand) {
				LogHelper.activateLogging(demand.get(ElectricityMeter.class).connection().powerSensor().reading());
			}
		}, appMan);*/
	}

	/*
     * This app uses a central configuration resource, which is accessed here
     */
    private void initConfigurationResource() {
		String configResourceDefaultName = PowervizBaseConfig.class.getSimpleName().substring(0, 1).toLowerCase()+PowervizBaseConfig.class.getSimpleName().substring(1);
		appConfigData = appMan.getResourceAccess().getResource(configResourceDefaultName);
		if (appConfigData != null) { // resource already exists (appears in case of non-clean start)
			appMan.getLogger().debug("{} started with previously-existing config resource", getClass().getName());
		}
		else {
			appConfigData = (PowervizBaseConfig) appMan.getResourceManagement().createResource(configResourceDefaultName, PowervizBaseConfig.class);
			appMan.getLogger().debug("{} started with new config resource", getClass().getName());
		}
		
		ElectricityMeter popMeter = appMan.getResourceManagement().createResource("PopSampleMeter",
				ElectricityMeter.class);
		popMeter.powerReading().create();
		popMeter.activate(true);
		ElectricityMeter antriebsDemoMeter = appMan.getResourceManagement().createResource("AntriebsDemoSampleMeter",
			ElectricityMeter.class);
		antriebsDemoMeter.powerReading().create();
		antriebsDemoMeter.activate(true);
			
		ValueResourceHelper.setIfNew(appConfigData.factoryId(), "SmartFactory OWL");
		appConfigData.availablePlants().create();
		//appConfigData.unIdentfiedMeters().create();
		//List<ElectricityMeter> meters = appMan.getResourceAccess().getResources(ElectricityMeter.class);
		//for(ElectricityMeter m: meters) {
		//	appConfigData.unIdentfiedMeters().add(m);
		//}
		appConfigData.knownTypes().create();
		//PowervizPlantOperationalState offState = getState("off");
		//PowervizPlantOperationalState onState = getState("on");
		//PowervizPlantOperationalState heatUpPopCState = getState("heatUpPop");
		
		PowervizPlantType stype1 = getType("PopcornMakerIOSB");
		ValueResourceHelper.setIfNew(stype1.manufacturer(), "SmartFactory OWL_somebody");
		ValueResourceHelper.setIfNew(stype1.libraryDevice().name(), "PopcornMakerIOSB_LibDev");
		stype1.libraryDevice().serialType().setAsReference(stype1);
		stype1.libraryDevice().stateAnalysisData().create();
		PowervizPlantOperationalState offLib = getAnalysis(stype1.libraryDevice(), "off");
		ValueResourceHelper.setIfNew(offLib.name(), "Aus/Low-Standby");
		ValueResourceHelper.setIfNew(offLib.levelToPlot(), 0);
		offLib.powerSignature().program().create();
		PowervizPlantOperationalState onLib = getAnalysis(stype1.libraryDevice(), "on");
		ValueResourceHelper.setIfNew(onLib.name(), "in Betrieb");
		ValueResourceHelper.setIfNew(onLib.levelToPlot(), 100);
		onLib.powerSignature().program().create();
		PowervizPlantOperationalState heatLib = getAnalysis(stype1.libraryDevice(), "heatUpPop");
		ValueResourceHelper.setIfNew(heatLib.name(), "HeatingUp PopcornMaker");
		ValueResourceHelper.setIfNew(heatLib.levelToPlot(), 200);
		heatLib.powerSignature().program().create();
		heatLib.occursDuringStates().create();
		heatLib.occursDuringStates().add(onLib);
		ValueResourceHelper.setIfNewCelsius(offLib.typicalTemperatureOnSurface(),
				19.5f);
		ValueResourceHelper.setIfNewCelsius(onLib.typicalTemperatureOnSurface(),
				20.5f);
		ValueResourceHelper.setIfNewCelsius(heatLib.typicalTemperatureOnSurface(),
				32.0f);
		offLib.powerSignature().program().create();
		onLib.powerSignature().program().create();
		heatLib.powerSignature().program().create();

		PowervizPlantType stype2 = getType("Hochregallager_XY");
		ValueResourceHelper.setIfNew(stype2.manufacturer(), "Hochregalkombinat Lemgo");
		ValueResourceHelper.setIfNew(stype2.libraryDevice().name(), "Hochregallager_XY_LibDev");
		stype2.libraryDevice().serialType().setAsReference(stype2);
		stype2.libraryDevice().stateAnalysisData().create();
		
		PowervizPlantOperationalState offLib2 = getAnalysis(stype2.libraryDevice(), "off");
		offLib2.powerSignature().program().create();
		ValueResourceHelper.setIfNew(offLib2.name(), "Aus/Low-Standby");
		ValueResourceHelper.setIfNew(offLib2.levelToPlot(), 0);
		
		PowervizPlantOperationalState onLib2 = getAnalysis(stype2.libraryDevice(), "on");
		ValueResourceHelper.setIfNew(onLib2.name(), "Energie-optimierter Betrieb");
		ValueResourceHelper.setIfNew(onLib2.levelToPlot(), 100);
		onLib2.powerSignature().program().create();

		PowervizPlantOperationalState onStdLib2 = getAnalysis(stype2.libraryDevice(), "onStd");
		ValueResourceHelper.setIfNew(onStdLib2.name(), "Standard Betrieb");
		ValueResourceHelper.setIfNew(onStdLib2.levelToPlot(), 200);
		onStdLib2.powerSignature().program().create();

		PowervizPlantOperationalState powerOn2 = getAnalysis(stype2.libraryDevice(), "powerOn");
		ValueResourceHelper.setIfNew(powerOn2.name(), "Einschalten");
		ValueResourceHelper.setIfNew(powerOn2.levelToPlot(), 50);
		powerOn2.powerSignature().program().create();
		
		PowervizPlant splant1 = getPlant("PopcornMaker_1");
		ValueResourceHelper.setIfNew(splant1.name(), "Popcorn-Maker (do not eat)");
		splant1.serialType().setAsReference(stype1);
		//ValueResourceHelper.setIfNew(splant1.setPlantToOperationState().stateControl(), false);
		//ValueResourceHelper.setIfNew(splant1.setPlantToOperationState().controllingApplication(),
		//		"powerviz-base");
		//splant1.stateToSetTo().setAsReference(onLib);
		splant1.meter().setAsReference(popMeter);
		splant1.stateAnalysisData().create();
		PowervizPlantOperationalState offAna1 = getAnalysis(splant1, "off");
		offAna1.stateActiveTimes().historicalData().create();
		PowervizPlantOperationalState onAna1 = getAnalysis(splant1, "on");
		onAna1.stateActiveTimes().historicalData().create();
		PowervizPlantOperationalState heatAna1 = getAnalysis(splant1, "heatUpPop");
		heatAna1.stateActiveTimes().historicalData().create();
		//ValueResourceHelper.setIfNew(splant1.energyConsumption().dayValue(), 0.25f*3600000);
		//ValueResourceHelper.setIfNew(splant1.energyConsumption().hourValue(), 1.5f*3600000);
		//ValueResourceHelper.setIfNew(splant1.energyConsumption().fifteenMinuteValue(), 10f*3600000);
		ValueResourceHelper.setIfNewCelsius(splant1.temperatureAtDeviceSurface().reading(), 20.0f);
		
		appConfigData.activate(true);
    }
    
    public PowervizPlantOperationalState getAnalysis(PowervizPlant plant, String id) {
		for(PowervizPlantOperationalState ana: plant.stateAnalysisData().getAllElements()) {
			if(ana.id().getValue().equals(id)) {
				return ana;
			}
		}
    	PowervizPlantOperationalState result = plant.stateAnalysisData().addDecorator(id,
				PowervizPlantOperationalState.class);
    	result.id().create();
		result.id().setValue(id);
    	return result;
    }
    
    public PowervizPlantType getType(String name) {
 		for(PowervizPlantType t: appConfigData.knownTypes().getAllElements()) {
 			if(t.name().getValue().equals(name)) return t;
    	}
    	return appConfigData.knownTypes().addDecorator(
    			ResourceUtils.getValidResourceName(name),
    			PowervizPlantType.class); 		
    }
    public PowervizPlant getPlant(String id) {
 		for(PowervizPlant t: appConfigData.availablePlants().getAllElements()) {
 			if(t.id().getValue().equals(id)) return t;
    	}
    	return appConfigData.availablePlants().addDecorator(
    			ResourceUtils.getValidResourceName(id),
    			PowervizPlant.class); 		
    }
    /*public PowervizPlantOperationalState getState(String id) {
 		for(PowervizPlantOperationalState t: appConfigData.knownStates().getAllElements()) {
 			if(t.id().getValue().equals(id)) return t;
    	}
    	PowervizPlantOperationalState result = appConfigData.knownStates().addDecorator(
    			ResourceUtils.getValidResourceName(id),
    			PowervizPlantOperationalState.class);
    	result.id().create();
    	result.id().setValue(id);
    	return result;
    }*/
    
    /*
     * register ResourcePatternDemands. The listeners will be informed about new and disappearing
     * patterns in the OGEMA resource tree
     */
    public void initDemands() {
    }

	public void close() {
    }

	/*
	 * if the app needs to consider dependencies between different pattern types,
	 * they can be processed here.
	 */
	public void processInterdependies() {
		// TODO Auto-generated method stub
		
	}
}
