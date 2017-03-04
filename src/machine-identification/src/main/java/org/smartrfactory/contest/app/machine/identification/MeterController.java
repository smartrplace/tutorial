package org.smartrfactory.contest.app.machine.identification;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.model.metering.ElectricityMeter;
import org.smartrfactory.contest.app.machine.identification.algo.MatchingAlgorithm;
import org.smartrfactory.contest.app.machine.identification.algo.MatchingResult;
import org.smartrfactory.contest.app.machine.identification.algo.MatchingStatistics;
import org.smartrfactory.contest.app.machine.identification.algo.impl.MatchingResultImpl;
import org.smartrfactory.contest.app.powerbizbase.config.PowervizBaseConfig;
import org.smartrfactory.contest.app.powerbizbase.config.PowervizPlantOperationalState;
import org.smartrfactory.contest.app.powerbizbase.config.PowervizPlantType;

public class MeterController implements Callable<MatchingResult> {
	
	private final ElectricityMeter meter;
	private volatile boolean running;
	private final ApplicationManager am;
	private final ExecutorService exec;
	private volatile Future<MatchingResult> lastResult;
	private final MatchingAlgorithm algo;
	
	public MeterController(ElectricityMeter meter, ApplicationManager am, ExecutorService exec, MatchingAlgorithm algo) {
		this.meter = meter;
		this.am = am;
		this.exec = exec;
		this.algo = algo;
	}

	public boolean isRunning() {
		return running;
	}
	
	public String start() {
		synchronized (this) {
			if (running)
				return "Already running";
			running = true;
		}
		try {
			lastResult = exec.submit(this);
		} catch (Exception e) {
			running = false;
			return "Something went wrong: " + e;
		}
		return null;
	}
	
	@Override
	public MatchingResult call() throws Exception {
		try {
			final List<PowervizBaseConfig> base = am.getResourceAccess().getResources(PowervizBaseConfig.class);
			if (base.isEmpty()) {
				MachineIdentificationApp.logger.warn("PowerVis base resource not found, identification not possible");
				return null;
			}
			final PowervizBaseConfig config = base.get(0);
			final List<PowervizPlantOperationalState> states = getAllStates(config);
			final List<ReadOnlyTimeSeries> schedules = new ArrayList<>(states.size());
			for (PowervizPlantOperationalState state: states) {
				schedules.add(state.powerSignature().program());
			}
			final NavigableMap<MatchingStatistics, ReadOnlyTimeSeries> results = 
					algo.getMatches(schedules, meter.connection().powerSensor().reading(), System.currentTimeMillis());
			if (results == null || results.isEmpty()) 
				return null;
			return new MatchingResultImpl(meter.connection().powerSensor().reading(), results);
		} finally {
			running = false;
		}
	}
	
	private static List<PowervizPlantOperationalState> getAllStates(PowervizBaseConfig config) {
		final List<PowervizPlantType> types = config.knownTypes().getAllElements();
		final List<PowervizPlantOperationalState> states = new ArrayList<>();
		for (PowervizPlantType t: types) {
			states.addAll(t.libraryDevice().stateAnalysisData().getAllElements());
		}
		return states;
	}
	
	
	
}
