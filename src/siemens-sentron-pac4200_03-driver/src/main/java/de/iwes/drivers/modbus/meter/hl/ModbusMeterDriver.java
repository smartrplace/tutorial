package de.iwes.drivers.modbus.meter.hl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.drivers.modbus.enums.DataType;
import org.ogema.drivers.modbus.enums.RegisterType;
import org.ogema.model.communication.ModbusAddress;
import org.ogema.model.communication.ModbusCommunicationInformation;
import org.ogema.tools.resource.util.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iwes.drivers.modbus.meters.gui.MainPage;
import de.iwes.widgets.api.OgemaGuiService;
import de.iwes.widgets.api.widgets.WidgetApp;
import de.iwes.widgets.api.widgets.WidgetPage;

@Component(immediate=true)
@Service(Application.class)
public class ModbusMeterDriver implements Application {
	
	public final static Logger logger = LoggerFactory.getLogger(ModbusMeterDriver.class);
	private WidgetApp wApp;
	
	@Reference
	private OgemaGuiService widgetService;

	@Override
	public void start(ApplicationManager appManager) {
		wApp = widgetService.createWidgetApp("/org/smartrplace/drivers/sentron", appManager);
		final WidgetPage<?> page = wApp.createStartPage();
		new MainPage(page, appManager);
	}

	@Override
	public void stop(AppStopReason reason) {
		if (wApp != null)
			wApp.close();
		wApp = null;
	}
	
	public static void addCommunicationInfo(
			Resource resource, 
			ModbusCommunicationInformation commInfo, 
			int register,
			RegisterType registerType,
			DataType dataType,
			int nrRegisters,
			boolean read,
			boolean write) {
		addCommunicationInfo(resource, commInfo, register, registerType, dataType, nrRegisters, read, write, 1F);
	}
	
	public static void addCommunicationInfo(
			Resource resource, 
			ModbusCommunicationInformation commInfo, 
			int register,
			RegisterType registerType,
			DataType dataType,
			int nrRegisters,
			boolean read,
			boolean write,
			float factor) {
		ModbusCommunicationInformation comm = resource.getSubResource("communicationInfo", ModbusCommunicationInformation.class).create();
		ModbusAddress address = comm.comAddress().create();
		address.host().<StringResource> create().setValue(commInfo.comAddress().host().getValue());
		address.port().<IntegerResource> create().setValue(commInfo.comAddress().port().getValue());
		address.count().<IntegerResource> create().setValue(nrRegisters);
		address.dataType().<StringResource> create().setValue(dataType.name());
		address.registerType().<StringResource> create().setValue(registerType.name());
		address.register().<IntegerResource> create().setValue(register);
		address.unitId().<IntegerResource> create().setValue(1);
		address.littleEndianRegisterOrder().<BooleanResource> create().setValue(false);
		comm.offset().<FloatResource> create().setValue(0);
		comm.factor().<FloatResource> create().setValue(factor);
		address.readable().<BooleanResource> create().setValue(read);
		address.writeable().<BooleanResource> create().setValue(write);
		if (resource instanceof SingleValueResource)
			LoggingUtils.activateLogging((SingleValueResource) resource, -2);
	}
	
}
