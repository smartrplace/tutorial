package org.smartrfactory.contest.app.machine.identification.algo;

import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;

/**
 * Matching statistics for an individual time series and a specific target resource
 */
public interface MatchingStatistics extends Comparable<MatchingStatistics> {

	// reference to the library signature
	ReadOnlyTimeSeries timeSeries();
	// reference to the logged resource which shall be matches
	FloatResource target();

	/**
	 * 
	 */
	float meanSquareDeviation();
	long startTime();
	Long endtime(); // may be null
	
	
}
