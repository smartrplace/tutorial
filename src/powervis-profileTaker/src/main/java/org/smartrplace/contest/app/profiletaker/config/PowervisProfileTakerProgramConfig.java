package org.smartrplace.contest.app.profiletaker.config;

import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.model.prototypes.Configuration;

/** 
 * Put an instance of this resource type for each program into a ResourceList
 */
public interface PowervisProfileTakerProgramConfig extends Configuration {

	StringResource profileId();
	
	FloatResource backupSignature();
	
	// TODO add required setting resources for programs

}
