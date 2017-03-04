package org.smartrplace.contest.app.profiletaker.gui;

import java.util.ArrayList;
import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.model.metering.ElectricityMeter;
import org.smartrfactory.contest.app.powerbizbase.config.PowervizPlantOperationalState;
import org.smartrfactory.contest.app.powerbizbase.config.PowervizPlantType;
import org.smartrplace.contest.app.profiletaker.PowervisProfileTakerController;
import org.smartrplace.contest.app.profiletaker.config.PowervisProfileTakerProgramConfig;

import de.iwes.util.format.StringFormatHelper;
import de.iwes.util.resource.ResourceHelper;
import de.iwes.widgets.api.extended.mode.UpdateMode;
import de.iwes.widgets.api.extended.resource.DefaultResourceTemplate;
import de.iwes.widgets.api.widgets.WidgetPage;
import de.iwes.widgets.api.widgets.html.StaticTable;
import de.iwes.widgets.api.widgets.localisation.OgemaLocale;
import de.iwes.widgets.api.widgets.sessionmanagement.OgemaHttpRequest;
import de.iwes.widgets.html.form.button.Button;
import de.iwes.widgets.html.form.label.Header;
import de.iwes.widgets.html.form.label.HeaderData;
import de.iwes.widgets.html.form.textfield.TextField;
import de.iwes.widgets.html.schedulemanipulator.ScheduleManipulatorConfiguration;
import de.iwes.widgets.resource.widget.dropdown.ResourceDropdown;
import de.iwes.widgets.reswidget.scheduleviewer.ScheduleViewerBasic;
import de.iwes.widgets.reswidget.scheduleviewer.api.ScheduleViewerConfiguration;
import de.iwes.widgets.template.DisplayTemplate;

/**
 * An HTML page, generated from the Java code.
 */
public class MainPage {
	
	public final long UPDATE_RATE = 5*1000;
	private final WidgetPage<?> page; 
	
	public MainPage(final WidgetPage<?> page, final ApplicationManager appMan,
			final PowervisProfileTakerController app) {
		this.page = page;

		Header header = new Header(page, "header", "Profile Taker");
		header.addDefaultStyle(HeaderData.CENTERED);

		//init all widgets
		final ResourceDropdown<PowervizPlantOperationalState> dropState =
				new ResourceDropdown<PowervizPlantOperationalState>(page, "dropState",
				false, PowervizPlantOperationalState.class, UpdateMode.MANUAL,
				appMan.getResourceAccess()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onGET(OgemaHttpRequest req) {
				List<PowervizPlantType> types = appMan.getResourceAccess().getResources(PowervizPlantType.class);
				List<PowervizPlantOperationalState> states = new ArrayList<>();
				for(PowervizPlantType t: types) {
					states.addAll(t.libraryDevice().stateAnalysisData().getAllElements());
				}
				update(states, req);
			}
		};
		final DisplayTemplate<PowervizPlantOperationalState> dropStateTempl =
				new DefaultResourceTemplate<PowervizPlantOperationalState>() {
			@Override
			public String getLabel(PowervizPlantOperationalState object, OgemaLocale locale) {
				PowervizPlantType type = ResourceHelper.getParentLevelsAbove(object,
						3, PowervizPlantType.class);
				String result;
				if(type == null) {
					result = "unknown_" + object.getName();
				} else {
					result = type.getName() + "." + object.getName();
				}
				return result;
			}
		};
		dropState.setTemplate(dropStateTempl);
		final ResourceDropdown<ElectricityMeter> dropMeter =
				new ResourceDropdown<ElectricityMeter>(page, "dropMeter",
				false, ElectricityMeter.class, UpdateMode.AUTO_ON_GET,
				appMan.getResourceAccess());
		ScheduleManipulatorConfiguration maipulatorConfig = new ScheduleManipulatorConfiguration(null, true, true);
		@SuppressWarnings("deprecation")
		ScheduleViewerConfiguration config = new ScheduleViewerConfiguration(false, false, false, true, maipulatorConfig, true, null, null, null, null, 24*60*60*1000L);
        config.showStandardIntervals = true;
		final ScheduleViewerBasic<ReadOnlyTimeSeries> viewer = new ScheduleViewerBasic<ReadOnlyTimeSeries>(page, "viewerWidget",
        		appMan, config, null) {
 			private static final long serialVersionUID = 1L;

			@Override
        	protected List<ReadOnlyTimeSeries> update(OgemaHttpRequest req) {
				ElectricityMeter meter = dropMeter.getSelectedItem(req);
				List<ReadOnlyTimeSeries> items = new ArrayList<>();
				if(meter == null) return items;
        		
        		items.add(meter.connection().powerSensor().reading().getHistoricalData());
        		//for(String path: schedulePaths)
        		//	addIfAvailable(path, items, appMan);
        		setSchedules(items, req);
        		selectSchedules(items, req);
        		return super.update(req);
        	}
        };
        viewer.setDefaultSchedules(null);
        
        Button takeSigButton = new Button(page, "takeSigButton", "Use this as signature") {
			private static final long serialVersionUID = 1L;
        	@Override
        	public void onPOSTComplete(String data, OgemaHttpRequest req) {
    			long startTime = viewer.getStartTime(req);
            	long endTime = viewer.getEndTime(req);
            	List<ReadOnlyTimeSeries> slist = viewer.getSelectedItems(req);
            	if(slist.isEmpty()) return;
            	ReadOnlyTimeSeries sched = slist.get(0);
            	PowervizPlantOperationalState state = dropState.getSelectedItem(req);
            	if(state == null) return;
System.out.println("Copying values from "+StringFormatHelper.getFullTimeDateInLocalTimeZone(startTime)+
		" to "+StringFormatHelper.getFullTimeDateInLocalTimeZone(endTime));
            	List<SampledValue> vals = sched.getValues(startTime, endTime);
            	state.powerSignature().program().deleteValues();
            	state.powerSignature().program().create().activate(false);
            	state.powerSignature().program().addValues(vals);
        	}
        };
        
        /*FileDownloader saveSignature = new FileDownloader(true, page, "saveSignature", "Save Signature to file", appMan) {

			@Override
			protected String getCustomFileName(String data, OgemaHttpRequest req) {
	           	String name;
				PowervizPlantOperationalState state = dropState.getSelectedItem(req);
            	if(state == null)  {
            		name = "signature";
            	} else {
            		name = ResourceUtils.getValidResourceName(state.getLocation())+".xml";
            	}
				return name;
			}

			@Override
			protected File getFileToDownload(String data, OgemaHttpRequest req) {
		//private void writeXmlFile(File parentDir, Resource res, String namePrefix) {
				PowervizPlantOperationalState state = dropState.getSelectedItem(req);
            	if(state == null)  {
            		return null;
            	}
				final Path temp = appMan.getDataFile("temp").toPath();
            	//final Path temp = Paths.get("data", "temp");
            	final Path base = temp.resolve("signatureSave.xml");
				File ownFile = base.toFile();
				try (PrintWriter out = new PrintWriter(ownFile)) {
					out.print(appMan.getSerializationManager(20, false, true).toXml(state));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
		//		}
				return ownFile;
			}
         };*/
        final TextField backupConfigName = new TextField(page, "backupConfigName");
        Button saveBackup = new Button(page, "saveBackup", "save Backup") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onPOSTComplete(String data, OgemaHttpRequest req) {
				PowervizPlantOperationalState state = dropState.getSelectedItem(req);
				List<ReadOnlyTimeSeries> items = new ArrayList<>();
				if(state == null) return;
				String name = backupConfigName.getValue(req);
				PowervisProfileTakerProgramConfig backup = app.appConfigData.availablePrograms().addDecorator(name,
						PowervisProfileTakerProgramConfig.class);
				backup.name().create();
				backup.name().setValue(name);
				backup.backupSignature().program().create();
				List<SampledValue> vals = state.powerSignature().program().getValues(0);
				backup.backupSignature().program().addValues(vals);
			}
        };
         
 		ScheduleViewerConfiguration configSignature = new ScheduleViewerConfiguration(true, true, false, false, maipulatorConfig, true, null, null, null, null, 24*60*60*1000L);
        final ScheduleViewerBasic<ReadOnlyTimeSeries> viewerSignature = new ScheduleViewerBasic<ReadOnlyTimeSeries>(page, "viewerWidgetSignature",
        		appMan, configSignature, null) {
 			private static final long serialVersionUID = 1L;

			@Override
        	protected List<ReadOnlyTimeSeries> update(OgemaHttpRequest req) {
				PowervizPlantOperationalState state = dropState.getSelectedItem(req);
				List<ReadOnlyTimeSeries> items = new ArrayList<>();
				if(state == null) return items;
        		
        		items.add(state.powerSignature().program());
        		//for(String path: schedulePaths)
        		//	addIfAvailable(path, items, appMan);
        		setSchedules(items, req);
        		selectSchedules(items, req);
        		return super.update(req);
        	}
        };
        viewerSignature.setDefaultSchedules(null);

        
        /**recommended for schedule viewer pages*/
        page.showOverlay(true);
		
		page.append(header);
		StaticTable table1 = new StaticTable(1, 5);
		page.append(table1);
		table1.setContent(0, 0, dropState);
		table1.setContent(0, 1, dropMeter);
		table1.setContent(0, 2, takeSigButton);
		table1.setContent(0, 3, saveBackup);
		table1.setContent(0, 4, backupConfigName);
		page.append(viewer);
		page.append(viewerSignature);
		
		dropMeter.registerDependentWidget(viewer);
		dropMeter.registerDependentWidget(viewer.getScheduleSelector());
		dropState.registerDependentWidget(viewerSignature);
		dropState.registerDependentWidget(viewerSignature.getScheduleSelector());
	}
	
	public WidgetPage<?> getPage() {
		return page;
	}
}
