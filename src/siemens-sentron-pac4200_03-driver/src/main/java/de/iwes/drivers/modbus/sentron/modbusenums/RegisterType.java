package de.iwes.drivers.modbus.sentron.modbusenums;

import java.util.Arrays;

/**
 * Copied from IWES-modified Modbus driver
 * 
 * Modbus defines four different address areas called primary tables.
 */
public enum RegisterType {

	COILS, DISCRETE_INPUTS, INPUT_REGISTERS, HOLDING_REGISTERS;

	public static RegisterType getEnumfromString(String enumAsString) {
		RegisterType returnValue = null;
		if (enumAsString != null) {
			for (RegisterType value : RegisterType.values()) {
				if (enumAsString.toUpperCase().equals(value.toString())) {
					returnValue = RegisterType.valueOf(enumAsString
							.toUpperCase());
					break;
				}
			}
		}
		if (returnValue == null) {
			throw new RuntimeException(
					enumAsString
							+ " is not supported. Use one of the following supported primary tables: "
							+ Arrays.toString(RegisterType.values()));
		}
		return returnValue;
	}

}
