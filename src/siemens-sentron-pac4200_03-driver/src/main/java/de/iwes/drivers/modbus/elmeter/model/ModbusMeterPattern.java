package de.iwes.drivers.modbus.elmeter.model;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.communication.ModbusCommunicationInformation;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.sensors.PowerSensor;

public class ModbusMeterPattern<M extends ModbusMeter> extends ResourcePattern<M> {
	
	public static class SentronPattern extends ModbusMeterPattern<SentronDevice> {
		
		public SentronPattern(Resource match) {
			super(match);
		}
		
	}
	
	public static class WeidmuellerPattern extends ModbusMeterPattern<SentronDevice> {
		
		public WeidmuellerPattern(Resource match) {
			super(match);
		}
		
	}

	public ModbusMeterPattern(Resource match) {
		super(match);
	}
	
	public final ModbusCommunicationInformation modbus = model.modbus();

	public final ElectricityConnection connection = model.connection();
	
	public final PowerSensor powerSensor = model.connection().powerSensor();
	
	/**
	 * Subphase sensors below
	 */
	@Existence(required=CreateMode.OPTIONAL)
	public final ResourceList<ElectricityConnection> subPhaseConnection = connection.subPhaseConnections();
	
}
