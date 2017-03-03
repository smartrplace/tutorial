package org.smartrfactory.contest.app.powerbizbase.config;

import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.StringResource;
import org.ogema.model.prototypes.Configuration;

/** 
 * The global configuration resource type for this app.
 */
public interface PowerbizBaseConfig extends Configuration {

	ResourceList<PowerbizBaseProgramConfig> availablePrograms();
	
	StringResource sampleElement();
	
	// TODO add global settings

}
