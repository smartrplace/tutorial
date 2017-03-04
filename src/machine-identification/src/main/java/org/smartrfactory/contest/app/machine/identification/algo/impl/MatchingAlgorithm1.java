package org.smartrfactory.contest.app.machine.identification.algo.impl;

import java.util.List;
import java.util.NavigableMap;

import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.recordeddata.RecordedData;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.tools.resource.util.LoggingUtils;
import org.smartrfactory.contest.app.machine.identification.MachineIdentificationApp;
import org.smartrfactory.contest.app.machine.identification.algo.MatchingAlgorithm;
import org.smartrfactory.contest.app.machine.identification.algo.MatchingStatistics;

public class MatchingAlgorithm1 implements MatchingAlgorithm {

	@Override
	public NavigableMap<MatchingStatistics, ReadOnlyTimeSeries> getMatches(
			List<ReadOnlyTimeSeries> libraryStates, 
			FloatResource loggedResource,
			long startTime) {
		final RecordedData logAccess = LoggingUtils.getHistoricalData(loggedResource);
		if (logAccess == null) {
			MachineIdentificationApp.logger.error("Logging not enabled for target resource {}", loggedResource );
			return null;
		}
		final Matcher matcher = new Matcher(libraryStates, loggedResource, startTime);
		matcher.run();
		return matcher.getResults();
	}
	
	

	
	
}
