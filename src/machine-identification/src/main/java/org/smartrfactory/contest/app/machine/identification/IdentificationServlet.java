package org.smartrfactory.contest.app.machine.identification;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.smartrfactory.contest.app.machine.identification.algo.MatchingResult;
import org.smartrfactory.contest.app.machine.identification.algo.MatchingStatistics;
import org.smartrfactory.contest.app.powerbizbase.config.PowervizPlantOperationalState;
import org.smartrfactory.contest.app.powerbizbase.config.PowervizPlantType;

public class IdentificationServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private final MachineIdentificationApp app;

	public IdentificationServlet(final MachineIdentificationApp app) {
		this.app = app;
	}
	
	/**
	 * Trigger identification start
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		final StringBuilder sb = new StringBuilder();
		BufferedReader reader = req.getReader();
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append('\n');
			}
		} finally {
			reader.close();
		}
		final JSONObject json = new JSONObject(sb.toString());
		if (json.has("meterId") && json.has("startIdentification")) {
			final String meterId = json.getString("meterId");
			final String response = app.startIdentification(meterId);
			if (response != null) 
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
			else {
				MachineIdentificationApp.logger.info("Identification started for meter {}", meterId);
				resp.setStatus(HttpServletResponse.SC_OK);
			}
		}
		else {
			MachineIdentificationApp.logger.warn("Invalid request {}",json);
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters. Expecting 'meterId' and 'startIdentification'");
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		final String meter = req.getParameter("meterId");
		if (meter == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No meterId specified");
			return;
		}
		MeterController contorller = app.getController(meter);
		if (contorller == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Identification not started for meter " + meter);
			return;
		}
		final Future<MatchingResult> future = contorller.getResult();
		if (future == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Identification not started for meter " + meter);
			return;
		}
		final JSONObject object = new JSONObject();
		boolean done = future.isDone();
		object.put("done", done);
		if (done) {
			final MatchingResult result;
			try {
				 result = future.get();
			} catch (InterruptedException | ExecutionException e) {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,e.toString());
				return;
			}
			final List<MatchingStatistics> stats = result.getStatistics();
			if (stats == null) {
				object.put("msg", "Machine could not be determined (reason unknown)");
				resp.setContentType("application/json");
				resp.getWriter().write(object.toString());
				return;
			}
			final JSONObject matches = new JSONObject();
			int cnt = 0;
			for (MatchingStatistics stat : stats) {
				JSONObject ser  = serialize(stat);
				if (ser == null)
					continue;
				cnt++;
				matches.put(cnt+"", ser);
			}
			object.put("matches", matches);
			resp.setContentType("application/json");
			resp.getWriter().write(object.toString());
			if (MachineIdentificationApp.logger.isTraceEnabled()) {
				MachineIdentificationApp.logger.trace("GET request response successful");
				System.out.println(object.toString(4));
			}
			
		}
		
		
		
	}

	private final JSONObject serialize(MatchingStatistics stats) {
		JSONObject obj = new JSONObject();
		 Schedule s = (Schedule) stats.timeSeries();
		 final Resource state = s.getParent().getParent();
		 if (!(state instanceof PowervizPlantOperationalState))
			 return null;
		 PowervizPlantOperationalState stat = (PowervizPlantOperationalState) state;
		 PowervizPlantType type = stat.getParent().getParent().getParent();
		 final String typeName = (type.name().isActive() ? type.name().getValue() : type.getPath());
		 final String statName = (stat.name().isActive() ? stat.name().getValue() : stat.id().isActive() ? stat.id().getValue() : stat.getPath());
		 
		obj.put("device", typeName);
		obj.put("state", statName);
		final float dev = stats.meanSquareDeviation();
		final float av = stats.getTargetAverageValue();
		float prob = 1 - dev / av;
		if (prob > 1) {
			MachineIdentificationApp.logger.warn("Probability > 1 detected: " + prob);
			prob = 1;
		}
		if (prob < 0)
			prob = 0;
		obj.put("matchingprobability", prob);
		return obj;
	}

}
