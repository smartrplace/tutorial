package org.smartrfactory.contest.app.powerbizbase.config;

import org.ogema.core.model.simple.StringResource;
import org.ogema.model.prototypes.Data;

public interface PowervizPlantType extends Data {
	StringResource manufacturer();
	@Override
	/**Human readable name*/
	StringResource name();
	
	PowervizPlant libraryDevice();
}
