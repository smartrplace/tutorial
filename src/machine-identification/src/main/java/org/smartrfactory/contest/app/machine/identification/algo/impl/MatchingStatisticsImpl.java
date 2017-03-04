package org.smartrfactory.contest.app.machine.identification.algo.impl;

import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.smartrfactory.contest.app.machine.identification.algo.MatchingStatistics;

public class MatchingStatisticsImpl implements MatchingStatistics{
	
	final ReadOnlyTimeSeries timeSeries;
	final FloatResource target;
	final long startTime;
	final float avDev;
	final long endTime;
	final float averageTarget;
	final float averageSignature;
	
	public MatchingStatisticsImpl(ReadOnlyTimeSeries timeSeries,FloatResource target, float avDev, long startTime, long endTime,
			float avTarget, float avSignature) {
		// FIXME
		System.out.println("    New mathcing stats " + avDev + ", time series "  +timeSeries);
		this.timeSeries = timeSeries;
		this.target = target;
		this.startTime = startTime;
		this.avDev = avDev;
		this.endTime = endTime;
		this.averageTarget = avTarget;
		this.averageSignature = avSignature;
	}

	@Override
	public ReadOnlyTimeSeries timeSeries() {
		return timeSeries;
	}

	@Override
	public FloatResource target() {
		return target;
	}

	@Override
	public float meanSquareDeviation() {
		return avDev;
	}

	@Override
	public long startTime() {
		return startTime;
	}

	@Override
	public Long endtime() {
		return endtime();
	}

	@Override
	public int compareTo(MatchingStatistics o) {
		return (int) (avDev - o.meanSquareDeviation());
	}
	
	@Override
	public int hashCode() {
		return timeSeries.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof MatchingStatistics))
			return false;
		return timeSeries.equals(((MatchingStatistics) obj).timeSeries());
	}
	
	@Override
	public float getTargetAverageValue() {
		return averageTarget;
	}
	
	
	
}
