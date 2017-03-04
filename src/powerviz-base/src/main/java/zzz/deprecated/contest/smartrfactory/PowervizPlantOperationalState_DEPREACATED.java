package zzz.deprecated.contest.smartrfactory;

import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.model.prototypes.Data;
import org.smartrfactory.contest.app.powerbizbase.config.PowervizPlantType;

/** 
 * Each Analysis 
 */
public interface PowervizPlantOperationalState_DEPREACATED extends Data {
	
	
	/** Plant types for which the state is used. If the resource is not active the state
	 * is relevant in principal to all plant types (like on/off)
	 * @return
	 */
	ResourceList<PowervizPlantType> relevantPlantTypes();
	
}
