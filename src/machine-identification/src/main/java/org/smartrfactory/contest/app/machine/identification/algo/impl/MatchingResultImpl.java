package org.smartrfactory.contest.app.machine.identification.algo.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;

import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.smartrfactory.contest.app.machine.identification.algo.MatchingResult;
import org.smartrfactory.contest.app.machine.identification.algo.MatchingStatistics;

public class MatchingResultImpl implements MatchingResult {
	
	private final FloatResource target;
	private List<MatchingStatistics> results;
	
	public MatchingResultImpl(FloatResource target, final NavigableMap<MatchingStatistics, ReadOnlyTimeSeries> results) {
		List<MatchingStatistics> resultList = new ArrayList<>(results.size());
		for (MatchingStatistics ms : results.navigableKeySet()) {
			resultList.add(ms);
		}
		this.results = Collections.unmodifiableList(resultList);
		this.target = target;
	}

	@Override
	public FloatResource targetResource() {
		return target;
	}

	@Override
	public List<MatchingStatistics> getStatistics() {
		return results;
	}

}
