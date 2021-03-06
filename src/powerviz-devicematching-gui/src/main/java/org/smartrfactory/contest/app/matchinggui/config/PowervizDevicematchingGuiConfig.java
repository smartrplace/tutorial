package org.smartrfactory.contest.app.matchinggui.config;

import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.StringResource;
import org.ogema.model.prototypes.Configuration;

/** 
 * The global configuration resource type for this app.
 */
public interface PowervizDevicematchingGuiConfig extends Configuration {

	ResourceList<PowervizDevicematchingGuiProgramConfig> availablePrograms();
	
	StringResource sampleElement();
	
	// TODO add global settings

}
