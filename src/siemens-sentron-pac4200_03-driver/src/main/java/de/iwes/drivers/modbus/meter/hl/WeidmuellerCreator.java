package de.iwes.drivers.modbus.meter.hl;

import org.ogema.drivers.modbus.enums.DataType;
import org.ogema.drivers.modbus.enums.RegisterType;
import org.ogema.model.connections.ElectricityConnection;

import de.iwes.drivers.modbus.elmeter.model.ModbusMeterPattern.WeidmuellerPattern;

public class WeidmuellerCreator {
	
	public static void create(WeidmuellerPattern device) {
		ModbusMeterDriver.addCommunicationInfo(device.powerSensor.reading(), 
				device.modbus, 874, RegisterType.HOLDING_REGISTERS, DataType.FLOAT, 2, true, false);
		device.subPhaseConnection.create();
		if (device.subPhaseConnection.size() == 0) {
			for (int i=0;i<3;i++) {
				ElectricityConnection conn = device.subPhaseConnection.add();
				conn.powerSensor().reading().create();
				ModbusMeterDriver.addCommunicationInfo(conn.powerSensor().reading(), 
						device.modbus, 868 + 2*i, RegisterType.HOLDING_REGISTERS, DataType.FLOAT, 2, true, false);
				
			}
			
		}
		
		
	}
	

	

}
