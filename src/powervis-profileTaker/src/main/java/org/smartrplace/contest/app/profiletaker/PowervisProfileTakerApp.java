package org.smartrplace.contest.app.profiletaker;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.smartrplace.contest.app.profiletaker.gui.MainPage;
import org.apache.felix.scr.annotations.Reference;
import de.iwes.widgets.api.OgemaGuiService;
import de.iwes.widgets.api.widgets.WidgetApp;
import de.iwes.widgets.api.widgets.WidgetPage;

/**
 * Template OGEMA application class
 */
@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class PowervisProfileTakerApp implements Application {
	public static final String urlPath = "/com/example/app/powervisprofiletaker";

    private OgemaLogger log;
    private ApplicationManager appMan;
    private PowervisProfileTakerController controller;

	private WidgetApp widgetApp;

	@Reference
	private OgemaGuiService guiService;

	public MainPage mainPage;

    /*
     * This is the entry point to the application.
     */
 	@Override
    public void start(ApplicationManager appManager) {

        // Remember framework references for later.
        appMan = appManager;
        log = appManager.getLogger();

        // 
        controller = new PowervisProfileTakerController(appMan);
		
		//register a web page with dynamically generated HTML
		widgetApp = guiService.createWidgetApp(urlPath, appManager);
		WidgetPage<?> page = widgetApp.createStartPage();
		mainPage = new MainPage(page, appMan, controller);
     }

     /*
     * Callback called when the application is going to be stopped.
     */
    @Override
    public void stop(AppStopReason reason) {
    	if (widgetApp != null) widgetApp.close();
		if (controller != null)
    		controller.close();
        log.info("{} stopped", getClass().getName());
    }
}
