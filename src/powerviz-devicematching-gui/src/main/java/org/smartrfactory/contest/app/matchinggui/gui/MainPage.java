package org.smartrfactory.contest.app.matchinggui.gui;

import org.ogema.core.application.ApplicationManager;
import org.ogema.model.metering.ElectricityMeter;
import org.smartrfactory.contest.app.powerbizbase.config.PowervizPlant;
import org.smartrplace.util.directresourcegui.TableReceiverTemplate;
import org.smartrplace.util.directresourcegui.ValueReceiverHelper;

import de.iwes.widgets.api.widgets.WidgetPage;
import de.iwes.widgets.api.widgets.sessionmanagement.OgemaHttpRequest;
import de.iwes.widgets.html.complextable.RowTemplate;
import de.iwes.widgets.html.form.label.Header;
import de.iwes.widgets.html.form.label.HeaderData;
import de.iwes.widgets.resource.widget.table.ResourceTable;

/**
 * An HTML page, generated from the Java code.
 */
public class MainPage {
	
	public final long UPDATE_RATE = 5*1000;
	private final WidgetPage<?> page; 
	
	ResourceTable<ElectricityMeter> meterTable;
	
	public MainPage(final WidgetPage<?> page, final ApplicationManager appMan) {
		this.page = page;

		Header header = new Header(page, "header", "Template Page");
		header.addDefaultStyle(HeaderData.CENTERED);

		RowTemplate<ElectricityMeter> rowTemplate =
			new TableReceiverTemplate<ElectricityMeter>(
			new TableReceiverTemplate.TableProvider<ElectricityMeter>() {

				@Override
				public ResourceTable<ElectricityMeter> getTable(OgemaHttpRequest req) {
					return meterTable;
				}
				
			}, ElectricityMeter.class, appMan) {

			@Override
			protected Row addRow(ElectricityMeter meter,
					ValueReceiverHelper<ElectricityMeter> vh, String id, OgemaHttpRequest req) {
				Row row = new Row();
				vh.stringLabel("Name", id, meter.getLocation(), row);
				String text;
				PowervizPlant device = meter.getSubResource("device", PowervizPlant.class);
				if(device.exists()) {
					text = device.getName();
				} else {
					text = "n/a";
				}
				vh.stringLabel("Device", id, text, row);
				vh.floatLabel("Power (W)", id, meter.connection().powerSensor().reading(),
						row, null);
				return row;
			}
		};
		meterTable = new ResourceTable<ElectricityMeter>(
				page, "meterTable", rowTemplate );
		//init all widgets

		page.append(header);
		page.append(meterTable);
		/*StaticTable table1 = new StaticTable(1, 2);
		page.append(table1);
		table1.setContent(0, 0, roomName);
		table1.setContent(0, 1, "Measurement value :");*/
	}
	
	public WidgetPage<?> getPage() {
		return page;
	}
}
