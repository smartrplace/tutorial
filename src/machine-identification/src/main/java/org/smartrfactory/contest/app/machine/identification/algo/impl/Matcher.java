package org.smartrfactory.contest.app.machine.identification.algo.impl;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.recordeddata.RecordedData;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.core.timeseries.TimeSeries;
import org.ogema.tools.resource.util.LoggingUtils;
import org.ogema.tools.resource.util.MultiTimeSeriesUtils;
import org.ogema.tools.timeseries.implementations.FloatTreeTimeSeries;
import org.ogema.tools.timeseries.iterator.api.MultiTimeSeriesIterator;
import org.ogema.tools.timeseries.iterator.api.SampledValueDataPoint;
import org.smartrfactory.contest.app.machine.identification.MachineIdentificationApp;
import org.smartrfactory.contest.app.machine.identification.algo.MatchingStatistics;

public class Matcher {
	
	private final static long WAIT_TIME_BETWEEN_ITERATIONS = 5000;
	private final static long MIN_DATA_REQUIRED = 10000;
	private final static long MAX_WAIT_TIME = 60000;
	final List<ReadOnlyTimeSeries> libraryStates;
	final FloatResource target;
	final RecordedData logAccess;
	final long startTime;
	// state variables
	private long started;
	int stepCnt = 0;
	private final NavigableMap<MatchingStatisticsImpl, ReadOnlyTimeSeries> results = new TreeMap<>();
	
	public Matcher(List<ReadOnlyTimeSeries> libraryStates, 
			FloatResource target,
			long startTime) {
		this.libraryStates = libraryStates;
		this.target = target;
		this.logAccess = LoggingUtils.getHistoricalData(target);
		this.startTime = startTime;
	}

	void run() {
		final long now = System.currentTimeMillis();
		if (started == 0)
			started = now;
		final int nrPoints = logAccess.size(startTime, now);
		if (stepCnt > 2 && nrPoints < stepCnt) {
			MachineIdentificationApp.logger.info("Too few data points for matching in target resource {}",logAccess.getPath());
			return;
		}
		boolean constant = islogDataConstant(logAccess.iterator(startTime, now));
		if (!constant) {
			for (ReadOnlyTimeSeries timeseries : libraryStates) {
				try {
					final MatchingStatisticsImpl eval = getMeanSquareDev(timeseries, target, startTime, now);
					if (eval != null) {
						MachineIdentificationApp.logger.debug("New deviation calculated for timeseries {}: deviation: {}",timeseries,eval.meanSquareDeviation());
						results.put(eval, timeseries);
					}
					
				} catch (Exception e) {
					MachineIdentificationApp.logger.error("Calculation of deviation failed for {}",timeseries,e);
					continue;
				}
			}
		}
		else 
			MachineIdentificationApp.logger.debug("Registered log data is constant, waiting at least another step");
		if (now - started > MIN_DATA_REQUIRED && results.size() >= 2) {
			final Iterator<MatchingStatisticsImpl> it = results.navigableKeySet().iterator();
			final MatchingStatisticsImpl best = it.next();
			final MatchingStatisticsImpl second = it.next();
			final float dev = best.meanSquareDeviation();
			final float avTarget = best.averageTarget;
			final float secondDev = second.meanSquareDeviation();
			MachineIdentificationApp.logger.info("Comparing best match (dev: {}) to second best (dev: {})",dev,secondDev);
			// TODO adapt
			if (dev / avTarget < 0.2 && (dev-secondDev) > (dev-avTarget)) {
				MachineIdentificationApp.logger.info("Best match found; timeseries {}",best.timeSeries);
				return;
			}
			
		}
		
		if (now - started > MAX_WAIT_TIME)
			return;
		try {
			Thread.sleep(WAIT_TIME_BETWEEN_ITERATIONS);
		} catch (InterruptedException e) {
			return;
		}
		stepCnt++;
		run();
	}
	
	// heuristics
	static boolean islogDataConstant(Iterator<SampledValue> it) {
		float lowest;
		float highest;
		float last;
		if (!it.hasNext())
			return true;
		SampledValue init = it.next();
		lowest = init.getValue().getFloatValue();
		highest = lowest;
		last =  lowest;
		while (it.hasNext()) {
			SampledValue sv = it.next();
			float current = sv.getValue().getFloatValue();
			if (current > 2*lowest && current > 50)
				return false;
			if (current < highest/2 && highest > 50)
				return false;
			if (current > 1.5 * lowest && current > 100)
				return false;
			if (current > 1.2 * lowest && current > 200)
				return false;
			if (current > 1.4 * last)
				return false;
			last = current;
			if (current < lowest)
				lowest = current;
			else if (current > highest)
				highest = current;
		}
		return true;
	}
	
	@SuppressWarnings("rawtypes")
	NavigableMap<MatchingStatistics, ReadOnlyTimeSeries> getResults() {
		return (NavigableMap) results;
	}
	
	// urgent TODO move signature to now before calculating!
	// TODO sliding window, correct for different intervals, etc. etc.
	private static MatchingStatisticsImpl getMeanSquareDev(ReadOnlyTimeSeries signature, FloatResource target, long startTime, long endTime) {
		final ReadOnlyTimeSeries logData = LoggingUtils.getHistoricalData(target);
		float sum1=0;
		float sum2=0;
		float sumSquares = 0;
		int cnt =0;
		long lastTimestamp = 0;
		final SampledValue first = signature.getNextValue(Long.MIN_VALUE);
		if (first == null)
			return null;
		final long t0 = first.getTimestamp();
		final long diff = startTime - t0;
		final TimeSeries newSignature = new FloatTreeTimeSeries();
		final Iterator<SampledValue> copyIt = signature.iterator(t0, t0 + (endTime-startTime));
		while (copyIt.hasNext()) {
			final SampledValue n = copyIt.next();
			newSignature.addValue(n.getTimestamp() + diff, n.getValue());
		}
		final MultiTimeSeriesIterator multiIt = MultiTimeSeriesUtils.getMultiIterator(Arrays.asList(newSignature.iterator(startTime, endTime) , logData.iterator(startTime, endTime)));
		while (multiIt.hasNext()) {
			final SampledValueDataPoint dataPoint = multiIt.next();
			Map<Integer,SampledValue> data = dataPoint.getElements();
			if (data.size() != 2)
				continue;
			cnt++;
			float val1 = dataPoint.getElement(0, InterpolationMode.LINEAR).getValue().getFloatValue();
			float val2 = dataPoint.getElement(1, InterpolationMode.LINEAR).getValue().getFloatValue();
			sum1 += val1;
			sum2 += val2;
			sumSquares += Math.pow(val1-val2,2);
			lastTimestamp = dataPoint.getTimestamp();
		}
		if (cnt ==0) {
			return null;
		}
		float av1 = sum1/cnt;
		float av2 = sum2/cnt;
		float avDev = (float) Math.sqrt(sumSquares);
		return new MatchingStatisticsImpl(signature, target, avDev, startTime, lastTimestamp, av2, av1);
	}
	
	
}
