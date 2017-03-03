package org.smartrfactory.contest.app.powerbizbase;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;

/**
 * Template OGEMA application class
 */
@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class PowerbizBaseApp implements Application {
	public static final String urlPath = "/com/example/app/powerbizbase";

    private OgemaLogger log;
    private ApplicationManager appMan;
    private PowerbizBaseController controller;

    /*
     * This is the entry point to the application.
     */
 	@Override
    public void start(ApplicationManager appManager) {

        // Remember framework references for later.
        appMan = appManager;
        log = appManager.getLogger();

        // 
        controller = new PowerbizBaseController(appMan);
     }

     /*
     * Callback called when the application is going to be stopped.
     */
    @Override
    public void stop(AppStopReason reason) {
    	if (controller != null)
    		controller.close();
        log.info("{} stopped", getClass().getName());
    }
}
