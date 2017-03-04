package org.smartrplace.contest.app.profiletaker.patternlistener;

import java.util.ArrayList;
import java.util.List;

import org.ogema.core.resourcemanager.pattern.PatternListener;

import org.smartrplace.contest.app.profiletaker.PowervisProfileTakerController;
import org.smartrplace.contest.app.profiletaker.pattern.ElectricityMeterPattern;

import de.iwes.util.logconfig.LogHelper;

/**
 * A pattern listener for the TemplatePattern. It is informed by the framework 
 * about new pattern matches and patterns that no longer match.
 */
public class ElectricityMeterListener implements PatternListener<ElectricityMeterPattern> {
	
	private final PowervisProfileTakerController app;
	public final List<ElectricityMeterPattern> availablePatterns = new ArrayList<>();
	
 	public ElectricityMeterListener(PowervisProfileTakerController controller) {
		this.app = controller;
	}
	
	@Override
	public void patternAvailable(ElectricityMeterPattern pattern) {
		availablePatterns.add(pattern);
		LogHelper.activateLogging(pattern.model.connection().powerSensor().reading());
		
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
