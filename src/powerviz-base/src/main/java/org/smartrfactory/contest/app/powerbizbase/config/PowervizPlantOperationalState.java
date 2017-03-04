package org.smartrfactory.contest.app.powerbizbase.config;

import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.model.prototypes.Data;

/** 
 * Each Analysis 
 */
public interface PowervizPlantOperationalState extends Data {
	/** Unique id*/
	StringResource id();
	
	/** When activity of different states are indicated in a graph use this level to indicate the state
	 * is available (so this value should be unique among all states of a plant)
	 */
	FloatResource levelToPlot();
	
	@Override
	/** Human readable name of the state*/
	StringResource name();
	
	/** Plant types for which the state is used. If the resource is not active the state
	 * is relevant in principal to all plant types (like on/off)
	 * @return
	 */
	ResourceList<PowervizPlantType> relevantPlantTypes();
	
	/**List of states that can be considered "parent states" - for most specific state
	 * "on" or a sub-state of this is parent
	 */
	ResourceList<PowervizPlantOperationalState> occursDuringStates();
}
