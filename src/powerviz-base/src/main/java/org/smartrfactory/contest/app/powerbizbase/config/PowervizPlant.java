package org.smartrfactory.contest.app.powerbizbase.config;

import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.model.action.Action;
import org.ogema.model.alignedinterval.StatisticalAggregation;
import org.ogema.model.metering.ElectricityMeter;
import org.ogema.model.prototypes.Data;
import org.ogema.model.sensors.TemperatureSensor;

/** 
 * Put an instance of this resource type for each program into a ResourceList
 */
public interface PowervizPlant extends Data {
	/** Unique id*/
	StringResource id();
	
	@Override
	/** Human readable name of plant*/
	StringResource name();
	
	ElectricityMeter meter();
	/**Evaluation of energy consumption during the respective period in Joule*/
	StatisticalAggregation energyConsumption();
	
	PowervizPlantType serialType();
	
	ResourceList<PowervizPlantOperationalState> stateAnalysisData();
	/** Idea: State analysis based on {@link PowervizPlantOperationalState#levelToPlot()}*/
	FloatResource stateGraph();
	
	//Action setPlantToOperationState();
	//PowervizPlantOperationalState stateToSetTo();
	
	/**depending on device type the sensor should be installed at a position where most
	 * critical temperature can be determined*/
	TemperatureSensor temperatureAtDeviceSurface();
}
