package org.smartrfactory.contest.app.machine.identification;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.model.metering.ElectricityMeter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartrfactory.contest.app.machine.identification.algo.MatchingAlgorithm;
import org.smartrfactory.contest.app.machine.identification.algo.impl.MatchingAlgorithm1;

@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class MachineIdentificationApp implements Application {
	
	public final static Logger logger = LoggerFactory.getLogger(MachineIdentificationApp.class);
	private final static MatchingAlgorithm algo = new MatchingAlgorithm1();
	private static final String servletPath = "/org/smartrfactory/app/identificiation";
	// synchronized on itself
	private final Map<ElectricityMeter, MeterController> controllers = new HashMap<>();
	private ApplicationManager am;
	private ExecutorService exec;

    /*
     * This is the entry point to the application.
     */
 	@Override
    public void start(ApplicationManager appManager) {
 		this.am = appManager;
 		this.exec = Executors.newFixedThreadPool(3);
     }

     /*
     * Callback called when the application is going to be stopped.
     */
    @Override
    public void stop(AppStopReason reason) {
    	synchronized (controllers) {
    		controllers.clear();
    	}
    	this.am = null;
    }
    
    String startIdentification(String meterId) {
    	final ApplicationManager am = this.am;
    	if (am == null)
    		return "App not active";
    	Resource resource = am.getResourceAccess().getResource(meterId);
    	if (!(resource instanceof ElectricityMeter))
    		return "Invalid meter id";
    	final ElectricityMeter meter  = (ElectricityMeter) resource;
    	final MeterController controller = getOrCreateController(meter);
    	if (controller.isRunning()) 
    		return "Identification is already running";
    	return controller.start();
    }
    
    private MeterController getOrCreateController(ElectricityMeter meterIn) {
    	final ElectricityMeter meter = meterIn.getLocationResource();
    	synchronized (controllers) {
    		MeterController c = controllers.get(meter);
    		if (c == null) {
    			c = new MeterController(meter,am,exec, algo);
    			controllers.put(meter, c);
    		}
    		return c;
    	}
    }
    
}
