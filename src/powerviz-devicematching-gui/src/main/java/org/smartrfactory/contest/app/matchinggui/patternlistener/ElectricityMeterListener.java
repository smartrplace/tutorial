package org.smartrfactory.contest.app.matchinggui.patternlistener;

import java.util.ArrayList;
import java.util.List;

import org.ogema.core.resourcemanager.pattern.PatternListener;

import org.smartrfactory.contest.app.matchinggui.PowervizDevicematchingGuiController;
import org.smartrfactory.contest.app.matchinggui.pattern.ElectricityMeterPattern;

/**
 * A pattern listener for the TemplatePattern. It is informed by the framework 
 * about new pattern matches and patterns that no longer match.
 */
public class ElectricityMeterListener implements PatternListener<ElectricityMeterPattern> {
	
	private final PowervizDevicematchingGuiController app;
	public final List<ElectricityMeterPattern> availablePatterns = new ArrayList<>();
	
 	public ElectricityMeterListener(PowervizDevicematchingGuiController controller) {
		this.app = controller;
	}
	
	@Override
	public void patternAvailable(ElectricityMeterPattern pattern) {
		availablePatterns.add(pattern);
		
		//TODO: work on pattern
		app.processInterdependies();
	}
	@Override
	public void patternUnavailable(ElectricityMeterPattern pattern) {
		// TODO process remove
		
		availablePatterns.remove(pattern);
		app.processInterdependies();
	}
	
	
}
