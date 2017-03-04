package org.smartrfactory.contest.app.machine.identification;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.felix.service.command.Descriptor;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.Schedule;
import org.smartrfactory.contest.app.machine.identification.algo.MatchingResult;
import org.smartrfactory.contest.app.machine.identification.algo.MatchingStatistics;
import org.smartrfactory.contest.app.powerbizbase.config.PowervizPlantOperationalState;
import org.smartrfactory.contest.app.powerbizbase.config.PowervizPlantType;

public class ShellCommands {
	
	private final MachineIdentificationApp app;
	
	public ShellCommands(final MachineIdentificationApp app) {
		this.app = app;
	}
	
	@Descriptor("Start device recognition for the specified meter")
	public void startIdentification(String meterId) {
		final String response = app.startIdentification(meterId);
		if (response != null) 
			System.out.println("Identification could not be started: " + response);
		else
			System.out.println("Identification started for meter " + meterId);
	}
	
	@Descriptor("Retrieve the latest device identification results for the specified meter")
	public void identificationResults(String meterID) throws InterruptedException, ExecutionException {
		MeterController controller = app.getController(meterID);
		if (controller == null) {
			System.out.println("No identification results for meter " + meterID + " available.");
			return;
		}
		final Future<MatchingResult> result = controller.getResult();
		if (result == null) {
			System.out.println("Identification for meter " + meterID + " has not been started yet.");
			return;
		}
		else if (!result.isDone()) {
			System.out.println("Identification for meter " + meterID + " still running");
			return;
		}
		final List<MatchingStatistics> resList = result.get().getStatistics();
		if (resList == null) {
			System.out.println("Identification failed " + meterID);
			return;
		}
		System.out.println("Matching results:");
		int cnt = 0;
		for (MatchingStatistics m : resList) {
			try {
				 Schedule s = (Schedule) m.timeSeries();
				 final Resource state = s.getParent().getParent();
				 if (!(state instanceof PowervizPlantOperationalState))
					 continue;
				 PowervizPlantOperationalState stat = (PowervizPlantOperationalState) state;
				 PowervizPlantType type = stat.getParent().getParent().getParent();
				 cnt++;
				 final String typeName = (type.name().isActive() ? type.name().getValue() : type.getPath());
				 final String statName = (stat.name().isActive() ? stat.name().getValue() : stat.id().isActive() ? stat.id().getValue() : stat.getPath());
				 System.out.println("Matching state " + cnt + ": device type " + typeName + ", state " + statName + ", av. deviation "  + m.meanSquareDeviation());
			} catch (Exception e) {
				System.err.println("Error: " + e);;
			}
		}
	}
	 
}
