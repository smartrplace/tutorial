package org.smartrfactory.contest.app.machine.identification.algo;

import java.util.List;
import java.util.NavigableMap;

import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;

public interface MatchingAlgorithm {

	/**
	 * This calculation may take time.
	 * @param libraryStates
	 * @param loggedResource
	 * @return
	 */
	NavigableMap<MatchingStatistics, ReadOnlyTimeSeries> getMatches(List<ReadOnlyTimeSeries> libraryStates, FloatResource loggedResource, long startTime); 
	
	
}
