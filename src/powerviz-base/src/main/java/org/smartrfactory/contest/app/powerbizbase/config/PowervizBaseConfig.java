package org.smartrfactory.contest.app.powerbizbase.config;

import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.StringResource;
import org.ogema.model.prototypes.Configuration;

/** 
 * The global configuration resource type for this app.
 */
public interface PowervizBaseConfig extends Configuration {

	ResourceList<PowervizPlant> availablePlants();
	ResourceList<PowervizPlantType> knownTypes();
	ResourceList<PowervizPlantOperationalState> knownStates();
	
	StringResource factoryId();
}
