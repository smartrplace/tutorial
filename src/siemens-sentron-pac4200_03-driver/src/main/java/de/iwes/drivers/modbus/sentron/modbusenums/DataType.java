package de.iwes.drivers.modbus.sentron.modbusenums;

/**
 * Copied from IWES-modified Modbus driver
 */
public enum DataType {

	BOOLEAN(2), SHORT(2), INT(4), FLOAT(4), DOUBLE(8), LONG(8), STRING(0), BYTEARRAY(0);

	private int size;

	private DataType(int size) {
		this.setSize(size);
	}

	public static DataType getEnumFromString(String enumAsString) {

		DataType returnValue = valueOf(enumAsString);

		if (returnValue == null) {
			throw new RuntimeException(
					enumAsString
							+ " is not supported. Use one of the following supported datatypes: "
							+ getSupportedDatatypes());
		}

		return returnValue;

	}

	private static String getSupportedDatatypes() {

		String supported = "";

		for (DataType type : DataType.values()) {
			supported += type.toString() + ", ";
		}

		return supported;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

}
