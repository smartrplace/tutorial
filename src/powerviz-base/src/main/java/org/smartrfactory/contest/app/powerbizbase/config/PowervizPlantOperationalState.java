package org.smartrfactory.contest.app.powerbizbase.config;

import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.model.prototypes.Data;

/** 
 * Each Analysis 
 */
public interface PowervizPlantOperationalState extends Data {
	/** Unique id*/
	StringResource id();

	/**state to which the analysis refers
	 */
	PowervizPlantOperationalState libraryState();
	
	/**Contains a schedule of InterpolationMode.STEPS with gaps where the state is not
	 * active. The value when active indicates the total energy consumed during the state
	 */
	FloatResource stateActiveTimes();
	
	/**If true the state represents a program that runs for a limited time (the duration is
	 * not necessarily exactly fixed, but typically does not vary too much. If false the state
	 * can persiste for an arbitraty time like on and off
	 */
	BooleanResource isProgram();
	
	/**powerSignature for the state of the device. If a library device this is the
	 * signature used for detection*/
	FloatResource powerSignature();
	TemperatureResource typicalTemperatureOnSurface();

	@Override
	/** Human readable name of the state*/
	StringResource name();
	
	/**List of states that can be considered "parent states" - for most specific state
	 * "on" or a sub-state of this is parent
	 */
	ResourceList<PowervizPlantOperationalState> occursDuringStates();
	
	/** When activity of different states are indicated in a graph use this level to indicate the state
	 * is available (so this value should be unique among all states of a plant)
	 */
	FloatResource levelToPlot();
}
