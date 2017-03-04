package org.smartrplace.contest.app.profiletaker.config;

import org.ogema.core.model.ResourceList;
import org.ogema.model.prototypes.Configuration;

/** 
 * The global configuration resource type for this app.
 */
public interface PowervisProfileTakerConfig extends Configuration {

	ResourceList<PowervisProfileTakerProgramConfig> availablePrograms();
	
}
