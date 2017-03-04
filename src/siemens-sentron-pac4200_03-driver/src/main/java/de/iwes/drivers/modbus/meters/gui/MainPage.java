package de.iwes.drivers.modbus.meters.gui;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.model.communication.ModbusCommunicationInformation;

import de.iwes.drivers.modbus.elmeter.model.ModbusMeterPattern;
import de.iwes.drivers.modbus.elmeter.model.ModbusMeterPattern.SentronPattern;
import de.iwes.drivers.modbus.elmeter.model.ModbusMeterPattern.WeidmuellerPattern;
import de.iwes.drivers.modbus.meter.hl.ModbusMeterDriver;
import de.iwes.drivers.modbus.meter.hl.SentronCreator;
import de.iwes.drivers.modbus.meter.hl.WeidmuellerCreator;
import de.iwes.widgets.api.widgets.WidgetPage;
import de.iwes.widgets.api.widgets.dynamics.TriggeredAction;
import de.iwes.widgets.api.widgets.dynamics.TriggeringAction;
import de.iwes.widgets.api.widgets.html.StaticTable;
import de.iwes.widgets.api.widgets.localisation.OgemaLocale;
import de.iwes.widgets.api.widgets.sessionmanagement.OgemaHttpRequest;
import de.iwes.widgets.html.alert.Alert;
import de.iwes.widgets.html.complextable.DynamicTable;
import de.iwes.widgets.html.complextable.RowTemplate;
import de.iwes.widgets.html.form.button.Button;
import de.iwes.widgets.html.form.dropdown.TemplateDropdown;
import de.iwes.widgets.html.form.label.Header;
import de.iwes.widgets.html.form.label.HeaderData;
import de.iwes.widgets.html.form.label.Label;
import de.iwes.widgets.html.form.textfield.TextField;
import de.iwes.widgets.html.form.textfield.ValueInputField;
import de.iwes.widgets.template.DisplayTemplate;

public class MainPage {

	private final WidgetPage<?> page;
	private final Header header;
	private final Alert alert;
	private final TemplateDropdown<Class<? extends ModbusMeterPattern<?>>> typeSelector;
	private final Header newDeviceSubheader;
	private final TextField newResourceName;
	private final TextField newHostAddress;
	private final ValueInputField<Integer> newPort;
	private final Button newDeviceSubmit;
	private final Header existingDevicesHeader;
	private final DynamicTable<ModbusMeterPattern<?>> table;
	
	public MainPage(final WidgetPage<?> page, final ApplicationManager am) {
		this.page = page;
		this.header = new Header(page, "header", true);
		header.setDefaultText("Modbus Meter Configuration");
		header.addDefaultStyle(HeaderData.CENTERED);
		header.setDefaultColor("blue");
		this.alert = new Alert(page, "alert", "");
		alert.setDefaultVisibility(false);
		
		typeSelector = new TemplateDropdown<>(page, "typeSelector");
		final List<Class<? extends ModbusMeterPattern<?>>> types = Arrays.<Class<? extends ModbusMeterPattern<?>>> asList(
				SentronPattern.class, WeidmuellerPattern.class);
		typeSelector.setDefaultItems(types);
		typeSelector.setTemplate(new DisplayTemplate<Class<? extends ModbusMeterPattern<?>>>() {
			
			@Override
			public String getLabel(Class<? extends ModbusMeterPattern<?>> object, OgemaLocale locale) {
				return object.getSimpleName();
			}
			
			@Override
			public String getId(Class<? extends ModbusMeterPattern<?>> object) {
				return object.getName();
			}
		});
		
		this.newDeviceSubheader = new Header(page, "newDeviceSubheader", true);
		newDeviceSubheader.setDefaultText("Create a new device");
		newDeviceSubheader.addDefaultStyle(HeaderData.CENTERED);
		newDeviceSubheader.setDefaultHeaderType(2);
		newDeviceSubheader.setDefaultColor("blue");
		
		this.newResourceName = new TextField(page, "newResourceName");
		this.newHostAddress = new TextField(page, "newHostAddress");
		this.newPort = new ValueInputField<Integer>(page, "newPort", Integer.class);
		newPort.setDefaultLowerBound(0);
		newPort.setDefaultUpperBound(65535);
		this.newDeviceSubmit = new Button(page, "newDeviceSubmit", "Create new device") {

			private static final long serialVersionUID = 1L;
			
			@Override
			public void onPOSTComplete(String data, OgemaHttpRequest req) {
				final Class<? extends ModbusMeterPattern<?>> type = typeSelector.getSelectedItem(req);
				final String path = newResourceName.getValue(req).trim();
				if (type == null || path.isEmpty()) {
					alert.showAlert("Please enter a valid resource path", false, req);
					return;
				}
				final String ip = newHostAddress.getValue(req).trim();
				if (ip.isEmpty()) {
					alert.showAlert("Please enter a valid host", false, req);
					return;
				}
				Integer port = newPort.getNumericalValue(req);
				if (port == null || port < 0) {
					alert.showAlert("Please enter a valid port", false, req);
					return;
				}
				
				final Resource r = am.getResourceAccess().getResource(path);
				if (r != null && r.exists()) {
					alert.showAlert("Resource "+ r.getLocation() + " already exists", false, req);
					return;
				}
				ModbusMeterPattern<?> meter = am.getResourcePatternAccess().createResource(path, type);
				meter.modbus.comAddress().host().<StringResource> create().setValue(ip);
				meter.modbus.comAddress().port().<IntegerResource> create().setValue(port);
				if (type == SentronPattern.class)
					SentronCreator.create((SentronPattern) meter);
				if (type == WeidmuellerPattern.class)
					WeidmuellerCreator.create((WeidmuellerPattern) meter);
				meter.model.activate(true);
				ModbusMeterDriver.logger.info("New Modbus meter device created: {}", meter.model);
				alert.showAlert("New Modbus meter created: " + meter.modbus, true, req);
			}
			
		};
		
		this.existingDevicesHeader = new Header(page, "existingDevicesHeader", true);
		existingDevicesHeader.setDefaultText("Existing devices");
		existingDevicesHeader.addDefaultStyle(HeaderData.CENTERED);
		existingDevicesHeader.setDefaultHeaderType(2);
		existingDevicesHeader.setDefaultColor("blue");
		
		table = new DynamicTable<ModbusMeterPattern<?>>(page, "table") {

			private static final long serialVersionUID = 1L;
			
			@SuppressWarnings("unchecked")
			@Override
			public void onGET(OgemaHttpRequest req) {
				final Class<? extends ModbusMeterPattern<?>> cl = typeSelector.getSelectedItem(req);
				if (cl == null) {
					updateRows(Collections.<ModbusMeterPattern<?>> emptyList(), req);
					return;
				}
				updateRows((List<ModbusMeterPattern<?>>) am.getResourcePatternAccess().getPatterns(cl, AccessPriority.PRIO_LOWEST), req);
			}
			
		};
		table.setRowTemplate(new RowTemplate<ModbusMeterPattern<?>>() {
			
			private final Map<String,Object> header = new LinkedHashMap<>();
			
			{
				header.put("path", "Device");
				header.put("type", "Device type");
				header.put("host", "IP address");
				header.put("port", "Port");
				header.put("power", "Consumption");
			}
			
			@Override
			public String getLineId(ModbusMeterPattern<?> object) {
				return object.model.getLocation();
			}
			
			@Override
			public Map<String, Object> getHeader() {
				return header;
			}
			
			@Override
			public Row addRow(final ModbusMeterPattern<?> object, final OgemaHttpRequest req) {
				final Row row = new Row();
				final String id = getLineId(object);
				final Label path = new Label(table, "path_" + id, req) {
					
					private static final long serialVersionUID = 1L;

					@Override
					public void onGET(OgemaHttpRequest req) {
						setText(object.model.getLocation(), req);
					}
					
				}; 
				row.addCell("path", path);
				final Label type = new Label(table, "devType_" + id, req) {
					
					private static final long serialVersionUID = 1L;

					@Override
					public void onGET(OgemaHttpRequest req) {
						setText(object.model.getResourceType().getSimpleName(),req);
					}
					
				};
				row.addCell("type", type);
				final Label host = new Label(table, "host_" + id, req) {
					
					private static final long serialVersionUID = 1L;

					@Override
					public void onGET(OgemaHttpRequest req) {
						final ModbusCommunicationInformation m = object.modbus;
						final String host;
						if (m.comAddress().host().isActive())
							host = m.comAddress().host().getValue();
						else
							host  ="";
						setText(host, req);
					}
					
				}; 
				row.addCell("host", host);
				final Label port = new Label(table, "port_" + id, req) {
					
					private static final long serialVersionUID = 1L;

					@Override
					public void onGET(OgemaHttpRequest req) {
						final ModbusCommunicationInformation m = object.modbus;
						final String port;
						if (m.comAddress().port().isActive())
							port = String.valueOf(m.comAddress().port().getValue());
						else
							port  ="";
						setText(port, req);
					}
					
				}; 
				row.addCell("port", port);
				final Label power = new Label(table, "power_" + id,req) {
					
					private static final long serialVersionUID = 1L;

					@Override
					public void onGET(OgemaHttpRequest req) {
						setText(String.format("%.2f W", object.powerSensor.reading().getValue()), req);
					}
					
				};
				power.setDefaultPollingInterval(5000);
				row.addCell("power", power);
				return row;
			}
		});
		buildPage();
		setDependencies();
	}
	
	private final void buildPage() {
		page.append(header).linebreak()
			.append(alert).linebreak();
		StaticTable tab = new StaticTable(1, 2, new int[]{2,3});
		int row = 0;
		tab.setContent(row, 0, "Select a device type").setContent(row++, 1, typeSelector);
		page.append(tab).linebreak().append(newDeviceSubheader);
		tab = new StaticTable(4, 2, new int[]{2,3});
		row = 0;
		tab.setContent(row, 0, "New device name").setContent(row++, 1, newResourceName)
			.setContent(row, 0, "New device IP address").setContent(row++, 1, newHostAddress)
			.setContent(row, 0, "New device port").setContent(row++, 1, newPort)
			.setContent(row++, 1, newDeviceSubmit);
		page.append(tab).linebreak().append(existingDevicesHeader).linebreak().append(table);
	}
	
	private final void setDependencies() {
		newDeviceSubmit.triggerAction(table, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
		newDeviceSubmit.triggerAction(alert, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
		typeSelector.triggerAction(table, TriggeringAction.POST_REQUEST, TriggeredAction.GET_REQUEST);
	}
	
}
