package de.iwes.drivers.modbus.elmeter.model;

import org.ogema.model.communication.ModbusCommunicationInformation;
import org.ogema.model.metering.ElectricityMeter;

public interface ModbusMeter extends ElectricityMeter {

	ModbusCommunicationInformation modbus();
	
}
