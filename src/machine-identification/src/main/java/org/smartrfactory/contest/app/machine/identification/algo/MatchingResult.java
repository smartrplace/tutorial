package org.smartrfactory.contest.app.machine.identification.algo;

import java.util.List;

import org.ogema.core.model.simple.FloatResource;


public interface MatchingResult {
	
	FloatResource targetResource();
	List<MatchingStatistics> getStatistics();
	
}
