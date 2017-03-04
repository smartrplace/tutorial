package org.smartrfactory.contest.app.machine.identification;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.AbsoluteSchedule;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.recordeddata.RecordedData;
import org.ogema.core.recordeddata.ReductionMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.core.tools.SerializationManager;
import org.smartrfactory.contest.app.machine.identification.algo.MatchingResult;
import org.smartrfactory.contest.app.machine.identification.algo.MatchingStatistics;
import org.smartrfactory.contest.app.powerbizbase.config.PowervizBaseConfig;
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
		final String signatures = req.getParameter("signatures");
		if (signatures != null) {
			JSONObject signaturesObj = getSignatures();
			resp.setContentType("application/json");
			resp.getWriter().write(signaturesObj.toString());
			if (MachineIdentificationApp.logger.isTraceEnabled()) {
				MachineIdentificationApp.logger.trace("Replying to signatures request");
				System.out.println(signaturesObj.toString(4));
			}
			return;
		}
		
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
		long start = System.currentTimeMillis()-2*60*60*1000;
		long end = System.currentTimeMillis();
		try {
			String startStr = req.getParameter("start");
			start = Long.parseLong(startStr);
		} catch (Exception e) {}
		try {
			String endStr = req.getParameter("end");
			end = Long.parseLong(endStr);
		} catch (Exception e) {}
		RecordedData rd = contorller.getLogdata();
		if (rd != null) {
			final SerializationManager sman  = app.am.getSerializationManager();
			final StringWriter writer = new StringWriter();
			sman.writeJson(writer,  contorller.getReading(), rd, start, end, 2000, ReductionMode.NONE);
			object.put("values", writer.toString());
		}
		else {
			MachineIdentificationApp.logger.warn("Logdata not found for " + meter);
			object.put("msg", "Logdata not found for " + meter);
		}
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
			start = Long.MAX_VALUE;
			end = Long.MIN_VALUE;
			for (MatchingStatistics stat : stats) {
				JSONObject ser  = serialize(stat);
				if (ser == null)
					continue;
				cnt++;
				matches.put(cnt+"", ser);
				if (ser.has("start")) {
					long localstart = ser.getLong("start");
					if (localstart < start)
						start = localstart;
				}
				if (ser.has("end")) {
					long localstart = ser.getLong("end");
					if (localstart > end)
						end = localstart;
				}
			}
			if (start != Long.MAX_VALUE) 
				object.put("start", start);
			else
				object.put("start", System.currentTimeMillis() - 2*60*60*1000); // simply guess!
			if (end != Long.MIN_VALUE)
				object.put("end", end);
			else
				object.put("end", System.currentTimeMillis());
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
		 final String typeName = getDeviceName(stat);
		 final String statName = (stat.name().isActive() ? stat.name().getValue() : stat.id().isActive() ? stat.id().getValue() : stat.getPath());
		 
		 if (typeName != null)
			 obj.put("device", typeName);
		 else
			 obj.put("device", "unknown");
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
		long start = stats.startTime();
		Long end = stats.endtime();
		obj.put("start", start);
		if (end != null)
			obj.put("end", end);
		return obj;
	}
	
	private JSONObject getSignatures() {
		final JSONObject result = new JSONObject();
		final List<PowervizBaseConfig> base = app.am.getResourceAccess().getResources(PowervizBaseConfig.class);
		if (base.isEmpty()) {
			MachineIdentificationApp.logger.warn("PowerVis base resource not found, identification not possible");
			return result;
		}
		final PowervizBaseConfig config = base.get(0);
		final List<PowervizPlantOperationalState> states = MeterController.getAllStates(config);
		for (PowervizPlantOperationalState state: states) {
			final JSONObject obj = new JSONObject();
			AbsoluteSchedule schedule = state.powerSignature().program().getLocationResource();
			if (!schedule.isActive() || schedule.isEmpty())
				continue;
			final JSONObject scheduleObj;
			final SerializationManager sman = app.am.getSerializationManager(1, false, true);
			try {
				scheduleObj = new JSONObject(sman.toJson(schedule));
			} catch (Exception e) {
				MachineIdentificationApp.logger.error("Could not convert schedule to json",e);
				continue;
			}
			final String stateName = state.name().isActive() ? state.name().getValue() : state.id().isActive() ? state.id().getValue() : state.getPath();
			final String devName = getDeviceName(state);
			if (devName != null) 
				obj.put("device", devName);
			else
				obj.put("device", "unknown");
			obj.put("state", stateName);
			obj.put("timeSeries", scheduleObj);
			result.put(state.getPath(), obj);
		}
		return result;
	}
	
	private static String getDeviceName(PowervizPlantOperationalState state) {
		Resource parent = state;
		for (int i=0;i<3;i++) {
			parent = parent.getParent();
			if (parent == null)
				return null;
		}
         PowervizPlantType type = (PowervizPlantType) parent;
		 final String typeName = (type.name().isActive() ? type.name().getValue() : type.getPath());
		 return typeName;
	}

}
